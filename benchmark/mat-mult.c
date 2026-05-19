#include <stdio.h>
#include <stdint.h>

/*
    INPUT: the dimension of the array N,
    followed by N^2 entries that fill the first matrix
    (filling the first row left-to-right, then the second row, and so on),
    and then N^2 entries that fill the second matrix likewise.
*/

int32_t N;
int32_t a[1024][1024], b[1024][1024], c[1024][1024];

void MatMult(void) {
    int32_t i, j, k;
    for (i = 0; i <= N - 1; i++)
        for (k = 0; k <= N - 1; k++)
            for (j = 0; j <= N - 1; j++)
                c[i][j] = c[i][j] + a[i][k] * b[k][j];
}

void PrintMatrix(void) {
    int32_t i, j;
    for (i = 0; i <= N - 1; i++) {
        for (j = 0; j <= N - 2; j++)
            printf("%d ", c[i][j]);
        printf("%d\n", c[i][N - 1]);
    }
}

int main(void) {
    int32_t i, j;
    scanf("%d", &N);
    if (N <= 1024) {
        for (i = 0; i <= N - 1; i++)
            for (j = 0; j <= N - 1; j++)
                scanf("%d", &a[i][j]);
        for (i = 0; i <= N - 1; i++)
            for (j = 0; j <= N - 1; j++)
                scanf("%d", &b[i][j]);

        MatMult();
        PrintMatrix();
    }
    return 0;
}
