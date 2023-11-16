#include <jni.h>

extern "C" JNIEXPORT void JNICALL Java_com_example_readability_Readability_breakTextByPage
  (JNIEnv *env, jobject thiz, jobject paint, jstring text, jfloat width, jfloat targetHeight, jobject callback) {
    // This function receives TextPaint paint and String text from Kotlin.
    // Then, it measures text with minikin library and line break by width.
    // when the text's height is over targetHeight, it calls callback function.

    // 1. Get paint's font
}