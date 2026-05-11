#include <stdio.h>

static int peekchar(void) {
    int c = getchar();

    if (c != EOF) {
        ungetc(c, stdin);
    }

    return c;
}

int eof(void) {
    return peekchar() == EOF;
}

int eoln(void) {
    int c = peekchar();

    return c == '\n' || c == '\r' || c == EOF;
}

