#define _GNU_SOURCE
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* https://usaco.org/index.php?page=viewproblem2&cpid=746 */

#define p 1000000007
#define MAX 100
#define MAX_LINE 350

typedef struct TTrie *PTrie;
typedef struct TTrie {
    int idx;
    PTrie next[123];
} TTrie;

int cnt;
PTrie env;

typedef int64_t *PVec;
typedef int64_t (*PMat)[MAX + 1];

char buf[MAX_LINE + 2];
int buf_len;
int idx;

PTrie NewTrieNode(int value) {
    PTrie node;
    node = calloc(1, sizeof(TTrie));
    node->idx = value;
    return node;
}

int ParseLit() {
    int val;
    val = 0;
    while (idx <= buf_len && buf[idx] >= '0' && buf[idx] <= '9') {
        val = (val * 10) + (buf[idx] - '0');
        idx = idx + 1;
    }
    return val;
}

int ParseVar() {
    PTrie now;
    now = env;
    while (idx <= buf_len && buf[idx] >= 'a' && buf[idx] <= 'z') {
        if (now->next[(int)buf[idx]] == NULL)
            now->next[(int)buf[idx]] = NewTrieNode(-1);
        now = now->next[(int)buf[idx]];
        idx = idx + 1;
    }

    if (now->idx == -1) {
        now->idx = cnt;
        cnt = cnt + 1;
    }
    return now->idx;
}

PVec ParseExpr() {
    PVec ParseExpr_result;
    int i;
    PVec left, right;
    ParseExpr_result = calloc(MAX + 1, sizeof(int64_t));
    if (buf[idx] >= '0' && buf[idx] <= '9')
        ParseExpr_result[0] = ParseLit();
    else if (buf[idx] >= 'a' && buf[idx] <= 'z')
        ParseExpr_result[ParseVar()] = 1;
    else if (buf[idx] == '(') {
        idx = idx + 2;
        left = ParseExpr();
        idx = idx + 7;
        right = ParseExpr();
        idx = idx + 2;
        for (i = 0; i <= 100; i++)
            ParseExpr_result[i] = (left[i] + right[i]) % p;
        free(left);
        free(right);
    } else printf("fatal error\n");
    return ParseExpr_result;
}

PVec ElemVec(int i) {
    PVec ElemVec_result;
    ElemVec_result = calloc(MAX + 1, sizeof(int64_t));
    ElemVec_result[i] = 1;
    return ElemVec_result;
}

PMat MatMul(PMat a, PMat b) {
    PMat MatMul_result;
    int i, j, k;
    MatMul_result = calloc((MAX + 1) * (MAX + 1), sizeof(int64_t));
    for (i = 0; i <= 100; i++)
        for (k = 0; k <= 100; k++)
            for (j = 0; j <= 100; j++)
                MatMul_result[i][j] = (MatMul_result[i][j] + (a[i][k] * b[k][j]) % p) % p;
    return MatMul_result;
}

PMat MatMulDispose(PMat a, PMat b) {
    PMat MatMulDispose_result;
    MatMulDispose_result = MatMul(a, b);
    free(a);
    free(b);
    return MatMulDispose_result;
}

PMat MatPow(PMat m, int e) {
    if (e == 1) return m;
    else if (e % 2 == 0) return MatPow(MatMul(m, m), e / 2);
    else return MatMul(m, MatPow(m, e - 1));
}

PMat MatPowDispose(PMat m, int e) {
    PMat MatPowDispose_result;
    MatPowDispose_result = MatPow(m, e);
    free(m);
    return MatPowDispose_result;
}

PMat Assign(int dest, PVec src) {
    PMat Assign_result;
    int i, j;
    Assign_result = malloc((MAX + 1) * (MAX + 1) * sizeof(int64_t));
    for (i = 0; i <= 100; i++)
        for (j = 0; j <= 100; j++)
            if (i == dest) Assign_result[i][j] = src[j];
            else if (i == j) Assign_result[i][j] = 1;
            else Assign_result[i][j] = 0;
    free(src);
    return Assign_result;
}

PMat Eval() {
    PMat Eval_result;
    int i, dest, loop;
    char *line;
    size_t cap;
    ssize_t len;
    PVec src;
    PMat body;
    Eval_result = Assign(-1, NULL);
    line = NULL;
    cap = 0;

    while ((len = getline(&line, &cap, stdin)) >= 0) {
        if (len > MAX_LINE) len = MAX_LINE;
        buf_len = (int)len;
        for (i = 1; i <= buf_len; i++)
            buf[i] = line[i - 1];
        idx = 1;
        while (buf[idx] == ' ') idx = idx + 1;
        if (buf[idx] == '}') return Eval_result;
        else if (buf[idx] >= 'a' && buf[idx] <= 'z') {
            dest = ParseVar();
            idx = idx + 3;
            src = ParseExpr();
            Eval_result = MatMulDispose(Assign(dest, src), Eval_result);
        } else if (buf[idx] >= '0' && buf[idx] <= '9') {
            loop = ParseLit();
            body = Eval();
            Eval_result = MatMulDispose(MatPowDispose(body, loop), Eval_result);
        } else if (buf[idx] == 'R') {
            idx = idx + 7;
            Eval_result = MatMulDispose(Assign(0, ElemVec(ParseVar())), Eval_result);
            return Eval_result;
        } else printf("fatal error\n");
    }
    free(line);
    return Eval_result;
}

int main() {
    PMat prog;
    env = NewTrieNode(0);
    cnt = 1;

    prog = Eval();
    printf("%lld\n", (long long)prog[0][0]);

    /* prog and env disposed by OS */
    return 0;
}
