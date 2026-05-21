#include <stdio.h>

/*
    Finite field Gram-Schmidt: given n vectors of dimension d in F_p,
    finds an orthogonal basis of the subspace spanned by these n vectors.
    Here, p=1009.

    INPUT:
        line 1: space-separated d <= 1000, n <= 1000
        line 2..n+1: d space-separated residues modulo p

    OUTPUT:
        lines 1..n: d space-separated residues 0,..,p-1
        pairwise orthogonal, spanning the original subspace
        (some might be all-zeros if original vectors are not spanning)
*/

const int p = 1009;
int n, d;
int Mat[1000][1000];

int Pow(int a, int e) {
    int b;
    if (e == 0)
        return 1;
    else if (e % 2 == 1)
        return (a * Pow(a, e - 1)) % p;
    else {
        b = Pow(a, e / 2);
        return (b * b) % p;
    }
}

int Dot(int i, int j) {
    int k;
    int dot = 0;
    for (k = 0; k <= d - 1; k++)
        dot = (dot + Mat[i][k] * Mat[j][k]) % p;
    return dot;
}

void Add(int i, int j) {
    int k;
    for (k = 0; k <= d - 1; k++)
        Mat[j][k] = (Mat[j][k] + Mat[i][k]) % p;
}

void Swap(int i, int j) {
    int k, t;
    for (k = 0; k <= d - 1; k++) {
        t = Mat[i][k];
        Mat[i][k] = Mat[j][k];
        Mat[j][k] = t;
    }
}

void Orthogonalize(int i, int j) {
    int proj, k;
    proj = (Dot(i, j) * Pow(Dot(i, i), p - 2)) % p;
    for (k = 0; k <= d - 1; k++)
        Mat[j][k] = (Mat[j][k] - ((proj * Mat[i][k]) % p) + p) % p;
}

void ReadMatrix() {
    int k, i;
    for (k = 0; k <= n - 1; k++)
        for (i = 0; i <= d - 1; i++)
            scanf("%d", &Mat[k][i]);
}

void PrintMatrix() {
    int k, i;
    for (k = 0; k <= n - 1; k++) {
        for (i = 0; i <= d - 1; i++)
            printf("%d ", Mat[k][i]);
        printf("\n");
    }
}

void GramSchmidt() {
    int k, i, j;
    for (k = 0; k <= n - 1; k++) {
        i = k;
        while (i < n) {
            if (Dot(i, i) != 0)
                break;
            i = i + 1;
        }

        if (i < n)
            Swap(k, i);
        else {
            i = k;
            while (i < n) {
                j = i + 1;
                while (j < n) {
                    if (Dot(i, j) != 0)
                        break;
                    j = j + 1;
                }

                if (j < n)
                    break;
                i = i + 1;
            }

            if (i < n) {
                Add(j, i);
                Swap(k, i);
            }
        }

        if (Dot(k, k) != 0) {
            i = k + 1;
            while (i < n) {
                Orthogonalize(k, i);
                i = i + 1;
            }
        }
    }
}

int main() {
    int k, i, j;
    scanf("%d%d", &d, &n);
    ReadMatrix();
    GramSchmidt();
    PrintMatrix();
    return 0;
}
