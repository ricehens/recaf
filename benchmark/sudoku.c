#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>

/*
    INPUT:
      First, an integer n <= 10 (so that the board is n^2 x n^2).
      Then n^4 numbers representing the sudoku board in row-major order.
    A value of 0 represents an empty cell.

    For example, for a 16x16 puzzle use n = 4.
*/

int Board[100][100];
bool RowUsed[100][101];
bool ColUsed[100][101];
bool BlockUsed[100][101];
int n, nn;
bool Solved;

void SolveSudoku(int r, int c) {
    int b, d;
    if (r >= nn) {
        Solved = true;
        return;
    }

    if (c >= nn) {
        SolveSudoku(r + 1, 0);
        return;
    }

    if (Board[r][c] != 0) {
        SolveSudoku(r, c + 1);
        return;
    }

    b = (r / n) * n + (c / n);
    for (d = 1; d <= nn; d++)
        if (!RowUsed[d][r] && !ColUsed[d][c]
                && !BlockUsed[d][b]) {
            Board[r][c] = d;
            RowUsed[d][r] = true;
            ColUsed[d][c] = true;
            BlockUsed[d][b] = true;
            SolveSudoku(r, c + 1);
            if (Solved) return;
            Board[r][c] = 0;
            RowUsed[d][r] = false;
            ColUsed[d][c] = false;
            BlockUsed[d][b] = false;
        }
}

void ReadBoard() {
    int i, j, d;
    int r, c, b;

    for (i = 0; i <= nn - 1; i++)
        for (j = 0; j <= nn - 1; j++) {
            scanf("%d", &d);
            Board[i][j] = d;
            if (d != 0) {
                r = i;
                c = j;
                b = (r / n) * n + (c / n);
                RowUsed[d][r] = true;
                ColUsed[d][c] = true;
                BlockUsed[d][b] = true;
            }
        }
}

void PrintBoard() {
    int i, j;
    for (i = 0; i <= nn - 1; i++) {
        for (j = 0; j <= nn - 2; j++)
            printf("%d ", Board[i][j]);
        printf("%d\n", Board[i][nn - 1]);
    }
}

int main() {
    scanf("%d", &n);
    nn = n * n;

    if (n <= 10) {
        Solved = false;
        ReadBoard();
        SolveSudoku(0, 0);
        PrintBoard();
    }
    return 0;
}
