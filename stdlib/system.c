#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <time.h>

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

void recaf_writestr(int *str) {
    int len = str[0];
    for (int i = 1; i <= len; i++) {
        putchar(str[i]);
    }
}

void recaf_readln() {
    int ch;
    do {
        ch = getchar();
    } while (ch != '\n' && ch != EOF);
}

void recaf_readstr(int size, int *str) {
    int i = 0;

    while (i < size) {
        int ch = peekchar();
        if (ch == '\n' || ch == EOF) break;
        str[++i] = ch;
        getchar();
    }

    str[0] = i;
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
