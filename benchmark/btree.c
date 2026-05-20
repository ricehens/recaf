#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>

typedef struct TNode *PNode;
typedef struct TNode {
    PNode left, right;
} TNode;

int32_t checksum(PNode node) {
    if (node->left == NULL) return 1;
    else return 1 + checksum(node->left) + checksum(node->right);
}

PNode make_tree(int32_t depth) {
    PNode make_tree_result = malloc(sizeof(TNode));
    if (depth == 0) {
        make_tree_result->left = NULL;
        make_tree_result->right = NULL;
    } else {
        make_tree_result->left = make_tree(depth - 1);
        make_tree_result->right = make_tree(depth - 1);
    }
    return make_tree_result;
}

void delete_tree(PNode node) {
    if (node->left != NULL) {
        delete_tree(node->left);
        delete_tree(node->right);
    }
    free(node);
}

const int32_t min_depth = 4;
const int32_t max_depth = 18;
const int32_t init_iter = 0x40000;

int main(void) {
    int32_t stretch_depth, check, depth, iter, i;
    PNode stretch_tree, long_lived_tree, current_tree;

    stretch_depth = max_depth + 1;
    stretch_tree = make_tree(stretch_depth);
    check = checksum(stretch_tree);
    delete_tree(stretch_tree);

    printf("stretch tree of depth %d\t check: %d\n", stretch_depth, check);

    long_lived_tree = make_tree(max_depth);
    depth = min_depth;
    iter = init_iter;
    while (depth <= max_depth) {
        check = 0;

        for (i = 1; i <= iter; i++) {
            current_tree = make_tree(depth);
            check = check + checksum(current_tree);
            delete_tree(current_tree);
        }
        printf("%d\t trees of depth %d\t check: %d\n", iter, depth, check);

        depth = depth + 2;
        iter = iter / 4;
    }

    printf("long lived tree of depth %d\t check: %d\n", max_depth, checksum(long_lived_tree));
    delete_tree(long_lived_tree);
    return 0;
}
