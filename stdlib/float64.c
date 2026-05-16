#include <stdio.h>
#include <math.h>
#include <stdint.h>
#include <stdbool.h>

_Static_assert(sizeof(int64_t) == sizeof(double), "int64_t and double must be the same size");

typedef union {
    double f;
    int64_t i;
} fl_bits;

static int64_t f2i(double x) {
    return (fl_bits){ .f = x }.i;
}

static double i2f(int64_t x) {
    return (fl_bits){ .i = x }.f;
}

int64_t FFromInt(int64_t x) {
    return f2i((double) x);
}

int64_t FToInt(int64_t x) {
    return (int64_t) i2f(x);
}

int64_t FAdd(int64_t a, int64_t b) {
    return f2i(i2f(a) + i2f(b));
}

int64_t FSub(int64_t a, int64_t b) {
    return f2i(i2f(a) - i2f(b));
}

int64_t FMul(int64_t a, int64_t b) {
    return f2i(i2f(a) * i2f(b));
}

int64_t FDiv(int64_t a, int64_t b) {
    return f2i(i2f(a) / i2f(b));
}

bool FGt(int64_t a, int64_t b) {
    return i2f(a) > i2f(b);
}

bool FLt(int64_t a, int64_t b) {
    return i2f(a) < i2f(b);
}

bool FGeq(int64_t a, int64_t b) {
    return i2f(a) >= i2f(b);
}

bool FLeq(int64_t a, int64_t b) {
    return i2f(a) <= i2f(b);
}

int64_t FPow(int64_t a, int64_t b) {
    return f2i(pow(i2f(a), i2f(b)));
}

int64_t FSqrt(int64_t x) {
    return f2i(sqrt(i2f(x)));
}

int64_t FAbs(int64_t x) {
    return f2i(fabs(i2f(x)));
}

int64_t FExp(int64_t x) {
    return f2i(exp(i2f(x)));
}

int64_t FLog(int64_t x) {
    return f2i(log(i2f(x)));
}

int64_t FLog10(int64_t x) {
    return f2i(log10(i2f(x)));
}

int64_t FMax(int64_t a, int64_t b) {
    return f2i(fmax(i2f(a), i2f(b)));
}

int64_t FMin(int64_t a, int64_t b) {
    return f2i(fmin(i2f(a), i2f(b)));
}

int64_t FFloor(int64_t x) {
    return f2i(floor(i2f(x)));
}

int64_t FCeil(int64_t x) {
    return f2i(ceil(i2f(x)));
}

int64_t FRound(int64_t x) {
    return f2i(round(i2f(x)));
}

int64_t FPi(void) {
    return f2i(3.141592653589793);
}

int64_t FEuler(void) {
    return f2i(2.718281828459045);
}

int64_t FSin(int64_t x) {
    return f2i(sin(i2f(x)));
}

int64_t FCos(int64_t x) {
    return f2i(cos(i2f(x)));
}

int64_t FTan(int64_t x) {
    return f2i(tan(i2f(x)));
}

int64_t FAsin(int64_t x) {
    return f2i(asin(i2f(x)));
}

int64_t FAcos(int64_t x) {
    return f2i(acos(i2f(x)));
}

int64_t FAtan(int64_t x) {
    return f2i(atan(i2f(x)));
}

int64_t FAtan2(int64_t a, int b) {
    return f2i(atan2(i2f(a), i2f(b)));
}

void FPrint(int64_t x) {
    printf("%f", i2f(x));
}

void FPrintPrecision(int64_t x, int precision) {
    printf("%.*f", precision, i2f(x));
}

