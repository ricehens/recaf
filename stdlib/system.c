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

    srandom(seed);
}

int Random(int range) {
    if (range <= 0) {
        return 0;
    }

    return (int) (random() % range);
}
