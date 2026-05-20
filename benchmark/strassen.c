#include <stdio.h>
#include <stdlib.h>

/*
    INPUT: the dimension N (a power of 2 and <= 512),
    followed by N^2 entries
    for the first matrix (row-major order),
    then N^2 entries for the second matrix.
*/

typedef int TMatrix[512][512];
typedef TMatrix *PMatrix;

PMatrix ReadMatrix(int dim) {
    int i, j;
    PMatrix ReadMatrix_result;
    ReadMatrix_result = malloc(sizeof(TMatrix));
    for (i = 0; i <= dim - 1; i++)
        for (j = 0; j <= dim - 1; j++)
            scanf("%d", &(*ReadMatrix_result)[i][j]);
    return ReadMatrix_result;
}

void PrintMatrix(PMatrix M, int dim) {
    int i, j;
    for (i = 0; i <= dim - 1; i++) {
        for (j = 0; j <= dim - 2; j++)
            printf("%d ", (*M)[i][j]);
        printf("%d\n", (*M)[i][dim - 1]);
    }
}

PMatrix Add(PMatrix A, PMatrix B, int dim) {
    int i, j;
    PMatrix Add_result;
    Add_result = malloc(sizeof(TMatrix));
    for (i = 0; i <= dim - 1; i++)
        for (j = 0; j <= dim - 1; j++)
            (*Add_result)[i][j] = (*A)[i][j] + (*B)[i][j];
    return Add_result;
}

PMatrix Sub(PMatrix A, PMatrix B, int dim) {
    int i, j;
    PMatrix Sub_result;
    Sub_result = malloc(sizeof(TMatrix));
    for (i = 0; i <= dim - 1; i++)
        for (j = 0; j <= dim - 1; j++)
            (*Sub_result)[i][j] = (*A)[i][j] - (*B)[i][j];
    return Sub_result;
}

PMatrix Quadrant(PMatrix M, int x, int y, int dim) {
    int N, i, j;
    PMatrix Quadrant_result;
    Quadrant_result = malloc(sizeof(TMatrix));
    N = dim / 2;
    for (i = 0; i <= N - 1; i++)
        for (j = 0; j <= N - 1; j++)
            (*Quadrant_result)[i][j] = (*M)[x * N + i][y * N + j];
    return Quadrant_result;
}

PMatrix Merge(PMatrix Q11, PMatrix Q12, PMatrix Q21, PMatrix Q22, int dim) {
    int i, j;
    PMatrix Merge_result;
    Merge_result = malloc(sizeof(TMatrix));
    for (i = 0; i <= dim - 1; i++)
        for (j = 0; j <= dim - 1; j++) {
            (*Merge_result)[i][j] = (*Q11)[i][j];
            (*Merge_result)[i][j + dim] = (*Q12)[i][j];
            (*Merge_result)[i + dim][j] = (*Q21)[i][j];
            (*Merge_result)[i + dim][j + dim] = (*Q22)[i][j];
        }
    return Merge_result;
}

PMatrix Strassen(PMatrix A, PMatrix B, int dim) {
    int i, j, k;
    PMatrix A11, A12, A21, A22, B11, B12, B21, B22,
        M1, M2, M3, M4, M5, M6, M7,
        T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14,
        C11, C12, C21, C22, Strassen_result;

    if (dim <= 8) {
        Strassen_result = malloc(sizeof(TMatrix));

        for (i = 0; i <= dim - 1; i++)
            for (j = 0; j <= dim - 1; j++) {
                (*Strassen_result)[i][j] = 0;
                for (k = 0; k <= dim - 1; k++)
                    (*Strassen_result)[i][j] = (*Strassen_result)[i][j] + (*A)[i][k] * (*B)[k][j];
            }

        return Strassen_result;
    }

    A11 = Quadrant(A, 0, 0, dim);
    A12 = Quadrant(A, 0, 1, dim);
    A21 = Quadrant(A, 1, 0, dim);
    A22 = Quadrant(A, 1, 1, dim);

    B11 = Quadrant(B, 0, 0, dim);
    B12 = Quadrant(B, 0, 1, dim);
    B21 = Quadrant(B, 1, 0, dim);
    B22 = Quadrant(B, 1, 1, dim);

    T1 = Add(A11, A22, dim / 2);
    T2 = Add(B11, B22, dim / 2);
    M1 = Strassen(T1, T2, dim / 2);
    free(T1); free(T2);

    T3 = Add(A21, A22, dim / 2);
    M2 = Strassen(T3, B11, dim / 2);
    free(T3);

    T4 = Sub(B12, B22, dim / 2);
    M3 = Strassen(A11, T4, dim / 2);
    free(T4);

    T5 = Sub(B21, B11, dim / 2);
    M4 = Strassen(A22, T5, dim / 2);
    free(T5);

    T6 = Add(A11, A12, dim / 2);
    M5 = Strassen(T6, B22, dim / 2);
    free(T6);

    T7 = Sub(A21, A11, dim / 2);
    T8 = Add(B11, B12, dim / 2);
    M6 = Strassen(T7, T8, dim / 2);
    free(T7); free(T8);

    T9 = Sub(A12, A22, dim / 2);
    T10 = Add(B21, B22, dim / 2);
    M7 = Strassen(T9, T10, dim / 2);
    free(T9); free(T10);

    free(A11); free(A12); free(A21); free(A22);
    free(B11); free(B12); free(B21); free(B22);

    T11 = Add(M1, M4, dim / 2);
    T12 = Sub(T11, M5, dim / 2);
    C11 = Add(T12, M7, dim / 2);
    free(T11); free(T12);

    T13 = Sub(M1, M2, dim / 2);
    T14 = Add(T13, M3, dim / 2);
    C22 = Add(T14, M6, dim / 2);
    free(T13); free(T14);

    C12 = Add(M3, M5, dim / 2);
    C21 = Add(M2, M4, dim / 2);

    free(M1); free(M2); free(M3); free(M4);
    free(M5); free(M6); free(M7);

    Strassen_result = Merge(C11, C12, C21, C22, dim / 2);
    free(C11); free(C12); free(C21); free(C22);
    return Strassen_result;
}

int main(void) {
    int N;
    PMatrix A, B, C;
    scanf("%d", &N);
    A = ReadMatrix(N);
    B = ReadMatrix(N);
    C = Strassen(A, B, N);
    PrintMatrix(C, N);
    free(A);
    free(B);
    free(C);
    return 0;
}
