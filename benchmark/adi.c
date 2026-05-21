#include <stdio.h>

#define N 400
#define TSTEPS 200
#define SCALE 0x100000L

long long u[N][N], v[N][N], p[N][N], q[N][N];

void init_array() {
    int i, j;
    for (i = 0; i < N; i++)
        for (j = 0; j < N; j++)
            u[i][j] = SCALE * (i + N - j) / N;
}

void print_array() {
    int i, j;
    for (i = 0; i < N; i++)
        for (j = 0; j < N; j++) {
            printf("%lld ", u[i][j]);
            if ((i * N + j) % 20 == 19)
                printf("\n");
        }
}

void kernel_adi() {
    int t, i, j;
    long long DX, DY, DT;
    long long B1, B2;
    long long mul1, mul2;
    long long a, b, c, d, e, f;

    DX = SCALE / N;
    DY = SCALE / N;
    DT = SCALE / TSTEPS;
    B1 = SCALE * 2L;
    B2 = SCALE;
    mul1 = SCALE * B1 * DT / (DX * DX);
    mul2 = SCALE * B2 * DT / (DY * DY);

    a = -mul1 / 2;
    b = SCALE + mul1;
    c = a;
    d = -mul2 / 2;
    e = SCALE + mul2;
    f = d;

    for (t = 1; t <= TSTEPS; t++) {
        // Column Sweep
        for (i = 1; i <= N - 2; i++) {
            v[0][i] = SCALE;
            p[i][0] = 0;
            q[i][0] = v[0][i];
            for (j = 1; j <= N - 2; j++) {
                p[i][j] = SCALE * (-c) / (a * p[i][j - 1] / SCALE + b);
                q[i][j] = SCALE * (-d * u[j][i - 1] / SCALE +
                  (SCALE + 2 * d) * u[j][i] / SCALE
                  - f * u[j][i + 1] / SCALE - a * q[i][j - 1] / SCALE)
                  / (a * p[i][j - 1] / SCALE + b);
            }

            v[N - 1][i] = SCALE;
            for (j = N - 2; j >= 1; j--)
                v[j][i] = p[i][j] * v[j + 1][i] / SCALE + q[i][j];
        }

        // Row Sweep
        for (i = 1; i <= N - 2; i++) {
            u[i][0] = SCALE;
            p[i][0] = 0;
            q[i][0] = u[i][0];
            for (j = 1; j <= N - 2; j++) {
                p[i][j] = SCALE * (-f) / (d * p[i][j - 1] / SCALE + e);
                q[i][j] = SCALE * (-a * v[i - 1][j] / SCALE +
                        (SCALE + 2 * a) * v[i][j] / SCALE
                        - c * v[i + 1][j] / SCALE - d * q[i][j - 1] / SCALE)
                    / (d * p[i][j - 1] / SCALE + e);
            }

            u[i][N - 1] = SCALE;
            for (j = N - 2; j >= 1; j--)
                u[i][j] = p[i][j] * u[i][j + 1] / SCALE + q[i][j];
        }
    }
}

int main() {
    init_array();
    kernel_adi();
    print_array();
    return 0;
}
