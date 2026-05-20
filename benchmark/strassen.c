#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

/*
    INPUT: the dimension N (a power of 2 and <= 128),
    followed by N^2 entries
    for the first matrix (row-major order),
    then N^2 entries for the second matrix.
*/

typedef struct TMatrix *PMatrix;
typedef struct TMatrix {
    int32_t dim;
    int32_t arr[128][128];
} TMatrix;

PMatrix ReadMatrix(int32_t dim) {
    int32_t i, j;
    PMatrix ReadMatrix_result;
    ReadMatrix_result = malloc(sizeof(TMatrix));
    ReadMatrix_result->dim = dim;
    for (i = 0; i <= dim - 1; i++)
        for (j = 0; j <= dim - 1; j++)
            scanf("%d", &ReadMatrix_result->arr[i][j]);
    return ReadMatrix_result;
}

void PrintMatrix(PMatrix M) {
    int32_t i, j;
    for (i = 0; i <= M->dim - 1; i++) {
        for (j = 0; j <= M->dim - 2; j++)
            printf("%d ", M->arr[i][j]);
        printf("%d\n", M->arr[i][M->dim - 1]);
    }
}

PMatrix Add(PMatrix A, PMatrix B) {
    int32_t i, j;
    PMatrix Add_result;
    Add_result = malloc(sizeof(TMatrix));
    Add_result->dim = A->dim;
    for (i = 0; i <= Add_result->dim - 1; i++)
        for (j = 0; j <= Add_result->dim - 1; j++)
            Add_result->arr[i][j] = A->arr[i][j] + B->arr[i][j];
    return Add_result;
}

PMatrix Sub(PMatrix A, PMatrix B) {
    int32_t i, j;
    PMatrix Sub_result;
    Sub_result = malloc(sizeof(TMatrix));
    Sub_result->dim = A->dim;
    for (i = 0; i <= Sub_result->dim - 1; i++)
        for (j = 0; j <= Sub_result->dim - 1; j++)
            Sub_result->arr[i][j] = A->arr[i][j] - B->arr[i][j];
    return Sub_result;
}

PMatrix Quadrant(PMatrix M, int32_t x, int32_t y) {
    int32_t N, i, j;
    PMatrix Quadrant_result;
    Quadrant_result = malloc(sizeof(TMatrix));
    N = M->dim / 2;
    Quadrant_result->dim = N;
    for (i = 0; i <= Quadrant_result->dim - 1; i++)
        for (j = 0; j <= Quadrant_result->dim - 1; j++)
            Quadrant_result->arr[i][j] = M->arr[x * N + i][y * N + j];
    return Quadrant_result;
}

PMatrix Merge(PMatrix Q11, PMatrix Q12, PMatrix Q21, PMatrix Q22) {
    int32_t N, i, j;
    PMatrix Merge_result;
    Merge_result = malloc(sizeof(TMatrix));
    N = Q11->dim;
    Merge_result->dim = N * 2;
    for (i = 0; i <= N - 1; i++)
        for (j = 0; j <= N - 1; j++) {
            Merge_result->arr[i][j] = Q11->arr[i][j];
            Merge_result->arr[i][j + N] = Q12->arr[i][j];
            Merge_result->arr[i + N][j] = Q21->arr[i][j];
            Merge_result->arr[i + N][j + N] = Q22->arr[i][j];
        }
    return Merge_result;
}

PMatrix Strassen(PMatrix A, PMatrix B) {
    PMatrix A11, A12, A21, A22, B11, B12, B21, B22,
        M1, M2, M3, M4, M5, M6, M7,
        T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14,
        C11, C12, C21, C22, Strassen_result;

    if (A->dim == 1) {
        Strassen_result = malloc(sizeof(TMatrix));
        Strassen_result->dim = 1;
        Strassen_result->arr[0][0] = A->arr[0][0] * B->arr[0][0];
        return Strassen_result;
    }

    A11 = Quadrant(A, 0, 0);
    A12 = Quadrant(A, 0, 1);
    A21 = Quadrant(A, 1, 0);
    A22 = Quadrant(A, 1, 1);

    B11 = Quadrant(B, 0, 0);
    B12 = Quadrant(B, 0, 1);
    B21 = Quadrant(B, 1, 0);
    B22 = Quadrant(B, 1, 1);

    T1 = Add(A11, A22);
    T2 = Add(B11, B22);
    M1 = Strassen(T1, T2);
    free(T1); free(T2);

    T3 = Add(A21, A22);
    M2 = Strassen(T3, B11);
    free(T3);

    T4 = Sub(B12, B22);
    M3 = Strassen(A11, T4);
    free(T4);

    T5 = Sub(B21, B11);
    M4 = Strassen(A22, T5);
    free(T5);

    T6 = Add(A11, A12);
    M5 = Strassen(T6, B22);
    free(T6);

    T7 = Sub(A21, A11);
    T8 = Add(B11, B12);
    M6 = Strassen(T7, T8);
    free(T7); free(T8);

    T9 = Sub(A12, A22);
    T10 = Add(B21, B22);
    M7 = Strassen(T9, T10);
    free(T9); free(T10);

    free(A11); free(A12); free(A21); free(A22);
    free(B11); free(B12); free(B21); free(B22);

    T11 = Add(M1, M4);
    T12 = Sub(T11, M5);
    C11 = Add(T12, M7);
    free(T11); free(T12);

    T13 = Sub(M1, M2);
    T14 = Add(T13, M3);
    C22 = Add(T14, M6);
    free(T13); free(T14);

    C12 = Add(M3, M5);
    C21 = Add(M2, M4);

    free(M1); free(M2); free(M3); free(M4);
    free(M5); free(M6); free(M7);

    Strassen_result = Merge(C11, C12, C21, C22);
    free(C11); free(C12); free(C21); free(C22);
    return Strassen_result;
}

int main(void) {
    int32_t N;
    PMatrix A, B, C;
    scanf("%d", &N);
    A = ReadMatrix(N);
    B = ReadMatrix(N);
    C = Strassen(A, B);
    PrintMatrix(C);
    free(A);
    free(B);
    free(C);
    return 0;
}
