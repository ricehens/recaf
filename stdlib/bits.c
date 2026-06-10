#include <stdint.h>

int64_t andq(int64_t x, int64_t y) {
    return (int64_t)((uint64_t)x & (uint64_t)y);
}

int32_t andl(int32_t x, int32_t y) {
    return (int32_t)((uint32_t)x & (uint32_t)y);
}

int64_t orq(int64_t x, int64_t y) {
    return (int64_t)((uint64_t)x | (uint64_t)y);
}

int32_t orl(int32_t x, int32_t y) {
    return (int32_t)((uint32_t)x | (uint32_t)y);
}

int64_t xorq(int64_t x, int64_t y) {
    return (int64_t)((uint64_t)x ^ (uint64_t)y);
}

int32_t xorl(int32_t x, int32_t y) {
    return (int32_t)((uint32_t)x ^ (uint32_t)y);
}

int64_t notq(int64_t x) {
    return (int64_t)(~(uint64_t)x);
}

int32_t notl(int32_t x) {
    return (int32_t)(~(uint32_t)x);
}

int64_t shlq(int64_t x, uint8_t y) {
    return (int64_t)((uint64_t)x << (y & 63));
}

int32_t shll(int32_t x, uint8_t y) {
    return (int32_t)((uint32_t)x << (y & 31));
}

int64_t shrq(int64_t x, uint8_t y) {
    return (int64_t)((uint64_t)x >> (y & 63));
}

int32_t shrl(int32_t x, uint8_t y) {
    return (int32_t)((uint32_t)x >> (y & 31));
}

int64_t sarq(int64_t x, uint8_t y) {
    return x >> (y & 63);
}

int32_t sarl(int32_t x, uint8_t y) {
    return x >> (y & 31);
}
