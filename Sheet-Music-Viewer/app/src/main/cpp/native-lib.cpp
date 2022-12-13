#include <jni.h>
#include <string>
#include <aaudio/AAudio.h>
#include "fft.h"

const int BUFFER_SIZE = 1 << 14;
const double MIN_FREQ = 60;
const double MAX_FREQ = 700;
const double FREQ_PER_IND = 44100. * 2 / BUFFER_SIZE;

double abs(double real, double image) {
    return sqrt(real*real + image*image);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_chelate1118_sheet_music_tuner_TunerFFT_getBufferSize1(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz
) {
    return BUFFER_SIZE;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_chelate1118_sheet_music_tuner_TunerFFT_nativeFindFrequency(
        JNIEnv *env,
        __attribute__((unused))jobject thiz,
        jbyteArray buffer
) {
    jbyte *buf = env->GetByteArrayElements(buffer, nullptr);
    auto *re = (double*)malloc(sizeof(double) * BUFFER_SIZE);
    auto *im = (double*)malloc(sizeof(double) * BUFFER_SIZE);

    memset(im, 0, sizeof(im));

    for (int i = 0; i < BUFFER_SIZE; i++) {
        re[i] = buf[i];
    }

    fft(re, im, BUFFER_SIZE);

    double max_abs = 0.0;
    double max_index = -1;

    for (int i = (int)(MIN_FREQ / FREQ_PER_IND); i < MAX_FREQ / FREQ_PER_IND; i++) {
        double cur_abs = abs(re[i], im[i]);
        if (max_abs < cur_abs) {
            max_abs = cur_abs;
            max_index = i;
        }
    }

    return max_index * FREQ_PER_IND;
}