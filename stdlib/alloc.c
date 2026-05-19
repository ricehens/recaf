#include <errno.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <unistd.h>

#define ALIGNMENT 16
#define SMALL_LIMIT (1024 * 1024)
#define CHUNK_SIZE (1024 * 1024)
#define BIN_COUNT (SMALL_LIMIT / ALIGNMENT)

static void *free_bins[BIN_COUNT];
static unsigned char *bump;
static unsigned char *bump_end;
static size_t page_size;

static size_t align_up(size_t value, size_t alignment) {
    return (value + alignment - 1) & ~(alignment - 1);
}

static size_t normalize_size(size_t size) {
    if (size < sizeof(void *)) {
        size = sizeof(void *);
    }
    if (size > SIZE_MAX - ALIGNMENT + 1) {
        fprintf(stderr, "recaf allocator: allocation size too large: %zu\n", size);
        abort();
    }
    return align_up(size, ALIGNMENT);
}

static size_t get_page_size(void) {
    if (page_size == 0) {
        long value = sysconf(_SC_PAGESIZE);
        page_size = value > 0 ? (size_t)value : 4096;
    }
    return page_size;
}

static void *map_pages(size_t size) {
    void *ptr = mmap(NULL, size, PROT_READ | PROT_WRITE,
                     MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (ptr == MAP_FAILED) {
        fprintf(stderr, "recaf allocator: mmap(%zu) failed: %d\n", size, errno);
        abort();
    }
    return ptr;
}

static void *allocate_small(size_t size) {
    size_t index = size / ALIGNMENT - 1;
    void *ptr = free_bins[index];
    if (ptr != NULL) {
        free_bins[index] = *(void **)ptr;
        return ptr;
    }

    if ((size_t)(bump_end - bump) < size) {
        size_t chunk_size = CHUNK_SIZE;
        if (chunk_size < size) {
            chunk_size = align_up(size, get_page_size());
        }

        bump = map_pages(chunk_size);
        bump_end = bump + chunk_size;
    }

    ptr = bump;
    bump += size;
    return ptr;
}

void *recaf_alloc(size_t size) {
    size = normalize_size(size);
    if (size <= SMALL_LIMIT) {
        return allocate_small(size);
    }

    size_t map_size = align_up(size, get_page_size());
    return map_pages(map_size);
}

void recaf_free(void *ptr, size_t size) {
    if (ptr == NULL) {
        return;
    }

    size = normalize_size(size);
    if (size <= SMALL_LIMIT) {
        size_t index = size / ALIGNMENT - 1;
        *(void **)ptr = free_bins[index];
        free_bins[index] = ptr;
        return;
    }

    size_t map_size = align_up(size, get_page_size());
    if (munmap(ptr, map_size) != 0) {
        fprintf(stderr, "recaf allocator: munmap(%p, %zu) failed: %d\n",
                ptr, map_size, errno);
        abort();
    }
}
