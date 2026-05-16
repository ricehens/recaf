#include <stdio.h>
#include <math.h>

_Static_assert(sizeof(int) == sizeof(float), "int and float must be the same size");

typedef union {
    float f;
    int i;
} fl_bits;

static int fl_from_float(float x) {
    return (fl_bits){ .f = x }.i;
}

static float fl_to_float(int x) {
    return (fl_bits){ .i = x }.f;
}

int fl_I2f(int x) {
    return fl_from_float((float)x);
}

int fl_Add(int a, int b) {
    return fl_from_float(fl_to_float(a) + fl_to_float(b));
}

int fl_Sub(int a, int b) {
    return fl_from_float(fl_to_float(a) - fl_to_float(b));
}

int fl_Mul(int a, int b) {
    return fl_from_float(fl_to_float(a) * fl_to_float(b));
}

int fl_Div(int a, int b) {
    return fl_from_float(fl_to_float(a) / fl_to_float(b));
}

int fl_Gt(int a, int b) {
    return fl_to_float(a) > fl_to_float(b) ? 1 : 0;
}

int fl_Lt(int a, int b) {
    return fl_to_float(a) < fl_to_float(b) ? 1 : 0;
}

int fl_Ge(int a, int b) {
    return fl_to_float(a) >= fl_to_float(b) ? 1 : 0;
}

int fl_Le(int a, int b) {
    return fl_to_float(a) <= fl_to_float(b) ? 1 : 0;
}

int fl_Pow(int a, int b) {
    return fl_from_float(powf(fl_to_float(a), fl_to_float(b)));
}

int fl_Sqrt(int x) {
    return fl_from_float(sqrtf(fl_to_float(x)));
}

int fl_Abs(int x) {
    return fl_from_float(fabsf(fl_to_float(x)));
}

int fl_Exp(int x) {
    return fl_from_float(expf(fl_to_float(x)));
}

int fl_Log(int x) {
    return fl_from_float(logf(fl_to_float(x)));
}

int fl_Log10(int x) {
    return fl_from_float(log10f(fl_to_float(x)));
}

int fl_Max(int a, int b) {
    return fl_from_float(fmaxf(fl_to_float(a), fl_to_float(b)));
}

int fl_Min(int a, int b) {
    return fl_from_float(fminf(fl_to_float(a), fl_to_float(b)));
}

int fl_Floor(int x) {
    return (int)floorf(fl_to_float(x));
}

int fl_Ceil(int x) {
    return (int)ceilf(fl_to_float(x));
}

int fl_Round(int x) {
    return (int)roundf(fl_to_float(x));
}

int fl_PI(void) {
    return fl_from_float(3.141592653589793f);
}

int fl_E(void) {
    return fl_from_float(2.718281828459045f);
}

int fl_Sin(int x) {
    return fl_from_float(sinf(fl_to_float(x)));
}

int fl_Cos(int x) {
    return fl_from_float(cosf(fl_to_float(x)));
}

int fl_Tan(int x) {
    return fl_from_float(tanf(fl_to_float(x)));
}

int fl_Asin(int x) {
    return fl_from_float(asinf(fl_to_float(x)));
}

int fl_Acos(int x) {
    return fl_from_float(acosf(fl_to_float(x)));
}

int fl_Atan(int x) {
    return fl_from_float(atanf(fl_to_float(x)));
}

int fl_Atan2(int a, int b) {
    return fl_from_float(atan2f(fl_to_float(a), fl_to_float(b)));
}

void fl_Print(int x) {
    printf("%f", fl_to_float(x));
}

void fl_PrintPrecision(int x, int precision) {
    printf("%.*f", precision, fl_to_float(x));
}
