#include <stdio.h>
#include <stdint.h>
#include <math.h>

uint32_t fl_I2f(int32_t x) {
    union { float f; uint32_t u; } u = { .f = (float)x };
    return u.u;
}

uint32_t fl_Add(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b }, w;
    w.f = u.f + v.f;
    return w.u;
}

uint32_t fl_Sub(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b }, w;
    w.f = u.f - v.f;
    return w.u;
}

uint32_t fl_Mul(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b }, w;
    w.f = u.f * v.f;
    return w.u;
}

uint32_t fl_Div(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b }, w;
    w.f = u.f / v.f;
    return w.u;
}

int32_t fl_Gt(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b };
    return u.f > v.f ? 1 : 0;
}

int32_t fl_Lt(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b };
    return u.f < v.f ? 1 : 0;
}

int32_t fl_Ge(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b };
    return u.f >= v.f ? 1 : 0;
}

int32_t fl_Le(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b };
    return u.f <= v.f ? 1 : 0;
}

uint32_t fl_Pow(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b }, w;
    w.f = powf(u.f, v.f);
    return w.u;
}

uint32_t fl_Sqrt(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = sqrtf(u.f);
    return w.u;
}

uint32_t fl_Abs(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = fabsf(u.f);
    return w.u;
}

uint32_t fl_Exp(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = expf(u.f);
    return w.u;
}

uint32_t fl_Log(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = logf(u.f);
    return w.u;
}

uint32_t fl_Log10(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = log10f(u.f);
    return w.u;
}

uint32_t fl_Max(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b }, w;
    w.f = fmaxf(u.f, v.f);
    return w.u;
}

uint32_t fl_Min(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b }, w;
    w.f = fminf(u.f, v.f);
    return w.u;
}

int32_t fl_Floor(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x };
    return floorf(u.f);
}

int32_t fl_Ceil(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x };
    return ceilf(u.f);
}

int32_t fl_Round(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x };
    return roundf(u.f);
}

uint32_t fl_PI() {
    union { float f; uint32_t u; } u;
    u.f = 3.141592653589793;
    return u.u;
}

uint32_t fl_E() {
    union { float f; uint32_t u; } u;
    u.f = 2.718281828459045;
    return u.u;
}

uint32_t fl_Sin(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = sinf(u.f);
    return w.u;
}

uint32_t fl_Cos(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = cosf(u.f);
    return w.u;
}

uint32_t fl_Tan(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = tanf(u.f);
    return w.u;
}

uint32_t fl_Asin(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = asinf(u.f);
    return w.u;
}

uint32_t fl_Acos(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = asinf(u.f);
    return w.u;
}

uint32_t fl_Atan(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x }, w;
    w.f = atanf(u.f);
    return w.u;
}

uint32_t fl_Atan2(uint32_t a, uint32_t b) {
    union { float f; uint32_t u; } u = { .u = a }, v = { .u = b }, w;
    w.f = atan2f(u.f, v.f);
    return w.u;
}

void fl_Print(uint32_t x) {
    union { float f; uint32_t u; } u = { .u = x };
    printf("%f", u.f);
}

void fl_PrintPrecision(uint32_t x, int32_t precision) {
    union { float f; uint32_t u; } u = { .u = x };
    printf("%.*lf", precision, u.f);
}

