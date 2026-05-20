#include <stdint.h>
#include <stdio.h>

/* https://www.cs.williams.edu/~heeringa/classes/cs135/s15/readings/spigot.pdf */

const int32_t n = 10000;
const int32_t len = 10 * n / 3;

int main(void) {
    int32_t i, j, k, q, x, nines, predigit;
    int32_t a[len + 1];

    for (j = 1; j <= len; j++) a[j] = 2;
    nines = 0; predigit = 0;
    for (j = 1; j <= n; j++) {
        q = 0;
        for (i = len; i >= 1; i--) {
            x = 10 * a[i] + q * i;
            a[i] = x % (2 * i - 1);
            q = x / (2 * i - 1);
        }
        a[1] = q % 10;
        q = q / 10;
        if (q == 9) nines = nines + 1;
        else if (q == 10) {
            printf("%d", predigit + 1);
            for (k = 1; k <= nines; k++) printf("0");
            predigit = 0;
            nines = 0;
        } else {
            printf("%d", predigit);
            predigit = q;
            if (nines != 0) {
                for (k = 1; k <= nines; k++) printf("9");
                nines = 0;
            }
        }
    }
    printf("%d", predigit);

    return 0;
}

