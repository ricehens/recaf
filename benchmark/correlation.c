#include <math.h>
#include <stdint.h>
#include <stdio.h>

/*
    Computes the correlation matrix for M variables with N observations each.
    INPUT:
    Line 1: space-separated M and N, both <= 1000
    Line 2..M+1: N space-separated integers between 0 and 100,
    each line giving N observations for one variable
    OUTPUT:
    correlation matrix, expressed as M lines each containing
    M space-separated floating point numbers to two decimal places.
*/

int32_t A[1000][1000];
int32_t EV[1000];
int32_t Variance[1000];
int32_t Cov[1000][1000];

int32_t M, N, i, j, k;
double corr;

int main(void) {
    scanf("%d%d", &M, &N);

    if ((M > 1000) || (N > 1000))
        printf("M and N must be <= 1000\n");
    else {
        for (i = 0; i <= M - 1; i++)
            for (j = 0; j <= N - 1; j++)
                scanf("%d", &A[i][j]);

        for (i = 0; i <= M - 1; i++) {
            EV[i] = 0;
            for (j = 0; j <= N - 1; j++)
                EV[i] = EV[i] + A[i][j];
        }

        for (i = 0; i <= M - 1; i++) {
            Variance[i] = -EV[i] * EV[i];
            for (j = 0; j <= N - 1; j++)
                Variance[i] = Variance[i] + N * A[i][j] * A[i][j];
        }

        for (i = 0; i <= M - 1; i++)
            for (j = 0; j <= M - 1; j++) {
                Cov[i][j] = -EV[i] * EV[j];
                for (k = 0; k <= N - 1; k++)
                    Cov[i][j] = Cov[i][j] + N * A[i][k] * A[j][k];
            }

        for (i = 0; i <= M - 1; i++) {
            for (j = 0; j <= M - 1; j++) {
                corr = Cov[i][j] / sqrt((double)Variance[i] * Variance[j]);
                printf("%.2f ", corr);
            }
            printf("\n");
        }
    }
    return 0;
}
