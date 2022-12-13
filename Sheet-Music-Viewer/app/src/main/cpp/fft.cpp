//
// Created by ryan on 11/18/22.
//

#include <cmath>
#include "fft.h"

void fft(double* re, double* im, int N) {
    int i, j, k;

    int n2 = N >> 1;
    int nb = 0;

    while (N != (1 << nb)) {
        nb++;
    }

    for (i = 0, j = 0; i < N - 1; i++) {
        if (i < j) {
            std::swap(re[i], re[j]);
            std::swap(im[i], im[j]);
        }

        k = n2;
        while (k <= j) {
            j -= k;
            k >>= 1;
        }

        j += k;
    }

    int i1, l, l1, l2 = 1;
    double c1 = -1.0, c2 = 0.0, t1, t2, u1, u2, z;

    for (l = 0; l < nb; l++) {
        l1 = l2;
        l2 <<= 1;
        u1 = 1.0;
        u2 = 0.0;

        for (j = 0; j < l1; j++) {
            for (i = j; i < N; i += l2) {
                i1 = i + l1;
                t1 = u1 * re[i1] - u2 * im[i1];
                t2 = u1 * im[i1] + u2 * re[i1];
                re[i1] = re[i] - t1;
                im[i1] = im[i] - t2;
                re[i] += t1;
                im[i] += t2;
            }

            z = u1 * c1 - u2 * c2;
            u2 = u1 * c2 + u2 * c1;
            u1 = z;
        }

        c2 = -sqrt((1.0 - c1) / 2.0);

        c1 = sqrt((1.0 + c1) / 2.0);
    }
}