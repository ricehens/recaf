#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <time.h>
#include <unistd.h>
#include <sys/time.h>

static int peekchar(void) {
    int c = getchar();

    if (c != EOF) {
        ungetc(c, stdin);
    }

    return c;
}

int Eof(void) {
    return peekchar() == EOF;
}

int Eoln(void) {
    int c = peekchar();

    return c == '\n' || c == '\r' || c == EOF;
}

static uint32_t rng_state = 2463534242u;

static uint32_t next_random_u32(void) {
    uint32_t x = rng_state;

    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 5;

    rng_state = x;

    return x;
}

void Randomize(void) {
    struct timeval tv;
    gettimeofday(&tv, NULL);

    uint32_t seed =
        (uint32_t)time(NULL) ^
        (uint32_t)getpid() ^
        (uint32_t)tv.tv_sec ^
        (uint32_t)tv.tv_usec ^
        (uint32_t)(uintptr_t)&tv;

    if (seed == 0) {
        seed = 2463534242u;
    }

    rng_state = seed;

    for (int i = 0; i < 8; i++) {
        next_random_u32();
    }
}

int Random(int range) {
    if (range <= 0) {
        return 0;
    }

    return (int)(next_random_u32() % (uint32_t)range);
}
