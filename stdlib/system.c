#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <time.h>

int recaf_peekchar(void) {
    int c = getchar();

    if (c != EOF) {
        ungetc(c, stdin);
    }

    return c;
}

int Eof(void) {
    return recaf_peekchar() == EOF;
}

int Eoln(void) {
    int c = recaf_peekchar();

    return c == '\n' || c == '\r' || c == EOF;
}

void Randomize(void) {
    srandom(time(NULL));
}

int Random(int range) {
    if (range <= 0) {
        return 0;
    }

    return (int) (random() % range);
}
