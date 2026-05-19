#include <stdint.h>
#include <stdio.h>

/*
    INPUT: the size of the Array N,
    followed by the elements of the Array, 
    all whitespace-separated
*/

const int32_t MAX_SIZE = 10000000;
int32_t a[10000001];
int32_t N;

void Swap(int32_t i, int32_t j) {
    int32_t t;
    t = a[i];
    a[i] = a[j];
    a[j] = t;
}

int32_t Partition(int32_t lo, int32_t hi) {
    int32_t pivot, i, j;
    pivot = a[lo];
    i = lo - 1;
    j = hi + 1;

    while (1) {
        do {
            i = i + 1;
        } while (a[i] < pivot);

        do {
            j = j - 1;
        } while (a[j] > pivot);

        if (i >= j) {
            return j;
            break;
        }

        Swap(i, j);
    }
}

void QuicksortRange(int32_t lo, int32_t hi) {
    int32_t p;
    if (lo < hi) {
        p = Partition(lo, hi);
        QuicksortRange(lo, p);
        QuicksortRange(p + 1, hi);
    }
}

void PrintArray(void) {
    int32_t i;
    printf("[");
    for (i = 1; i <= N - 1; i++)
        printf("%d, ", a[i]);
    printf("%d]\n", a[N]);
}

int main(void) {
    int32_t i;
    scanf("%d", &N);
    if (N > MAX_SIZE) return 0;

    for (i = 1; i <= N; i++)
        scanf("%d", &a[i]);

    QuicksortRange(1, N);
    PrintArray();
    return 0;
}
