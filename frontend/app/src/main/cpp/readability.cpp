#include <jni.h>
#include <cctype>
#include <vector>

extern "C" JNIEXPORT jintArray JNICALL Java_com_snu_readability_data_viewer_PageSplitDataSource_splitPageNative(
        JNIEnv *env, jobject thiz, jstring content, jfloatArray charWidths, jfloat lineHeight,
        jfloat width,
        jfloat height, jfloat paragraphSpacing) {
    // precompute
    std::vector<float> widths;
    std::vector<int> indices;
    std::vector<bool> isWhitespaces;

    const jchar *contentChars = env->GetStringChars(content, 0);
    jfloat wordWidth = 0;
    jboolean lastWhitespace = false;

    jfloat *charWidthsPointer = env->GetFloatArrayElements(charWidths, 0);

    int l = env->GetStringLength(content);

    for (int i = 0; i < l; i++) {
        jchar c = contentChars[i];
        jboolean isWhitespace = isspace((int) c);
        if (!lastWhitespace && isWhitespace) {
            // end of word
            widths.push_back(wordWidth);
            indices.push_back(i);
            isWhitespaces.push_back(false);
        } else if (lastWhitespace && !isWhitespace) {
            // start of word
            wordWidth = charWidthsPointer[c];
        } else {
            wordWidth += charWidthsPointer[c];
        }

        if (isWhitespace) {
            widths.push_back(charWidthsPointer[c]);
            indices.push_back(i + 1);
            isWhitespaces.push_back(true);
        }

        lastWhitespace = isWhitespace;
    }

    if (!lastWhitespace) {
        // end of word
        widths.push_back(wordWidth);
        indices.push_back(l);
        isWhitespaces.push_back(false);
    }

    // align
    std::vector<int> pageRange;

    jfloat x = 0;
    jfloat y = lineHeight;

    int dataLen = widths.size();

    for (int i = 0; i < dataLen; i++) {
        jfloat wordWidth = widths[i];
        jboolean isWhitespace = isWhitespaces[i];
        jint index = indices[i];
        jint lastIndex = i == 0 ? 0 : indices[i - 1];

        if (x + wordWidth > width) {
            if (isWhitespace) {
                if (contentChars[lastIndex] == '\n') {
                    y += lineHeight * paragraphSpacing;
                } else {
                    y += lineHeight;
                }
                x = 0;
                if (y > height) {
                    pageRange.push_back(index);
                    y = lineHeight;
                }
            } else {
                if (wordWidth > width) {
                    jint wordPartialIndex = 0;
                    jint wordIndexMax = index - lastIndex;
                    while (wordPartialIndex < wordIndexMax) {
                        jfloat remainingWidth = width - x;
                        while (wordPartialIndex < wordIndexMax) {
                            if (remainingWidth -
                                charWidthsPointer[contentChars[lastIndex + wordPartialIndex]] < 0) {
                                break;
                            }
                            remainingWidth -= charWidthsPointer[contentChars[lastIndex +
                                                                             wordPartialIndex]];
                            wordPartialIndex++;
                        }
                        if (wordPartialIndex < wordIndexMax) {
                            x = 0;
                            y += lineHeight;
                            if (y > height) {
                                pageRange.push_back(lastIndex + wordPartialIndex);
                                y = 0;
                            }
                        }
                    }
                } else {
                    y += lineHeight;
                    x = wordWidth;
                    if (y > height) {
                        pageRange.push_back(lastIndex);
                        y = lineHeight;
                    }
                }
            }
        } else {
            x += wordWidth;

        }
    }

    if (pageRange.empty() || pageRange.back() != env->GetStringLength(content)) {
        pageRange.push_back(env->GetStringLength(content));
    }

    // cleanup
    env->ReleaseStringChars(content, contentChars);
    env->ReleaseFloatArrayElements(charWidths, charWidthsPointer, 0);

    jintArray result = env->NewIntArray(pageRange.size());
    env->SetIntArrayRegion(result, 0, pageRange.size(), pageRange.data());
    return result;
}

extern "C" JNIEXPORT void JNICALL Java_com_snu_readability_data_viewer_PageSplitDataSource_drawPageNative(
        JNIEnv *env, jobject thiz, jstring content, jfloatArray charWidths, jfloat lineHeight,
        jfloat offset, jfloat width, jfloat paragraphSpacing, jobject textDrawer) {
    // retrive drawText method in TextDrawer
    jclass textDrawerClass = env->GetObjectClass(textDrawer);
    jmethodID drawTextMethod = env->GetMethodID(textDrawerClass, "drawText", "(IIFF)V");

    // precompute
    std::vector<float> widths;
    std::vector<int> indices;
    std::vector<bool> isWhitespaces;

    const jchar *contentChars = env->GetStringChars(content, 0);
    jfloat wordWidth = 0;
    jboolean lastWhitespace = false;

    jfloat *charWidthsPointer = env->GetFloatArrayElements(charWidths, 0);

    int l = env->GetStringLength(content);

    for (int i = 0; i < l; i++) {
        jchar c = contentChars[i];
        jboolean isWhitespace = isspace((int) c);
        if (!lastWhitespace && isWhitespace) {
            // end of word
            widths.push_back(wordWidth);
            indices.push_back(i);
            isWhitespaces.push_back(false);
        } else if (lastWhitespace && !isWhitespace) {
            // start of word
            wordWidth = charWidthsPointer[c];
        } else {
            wordWidth += charWidthsPointer[c];
        }

        if (isWhitespace) {
            widths.push_back(charWidthsPointer[c]);
            indices.push_back(i + 1);
            isWhitespaces.push_back(true);
        }

        lastWhitespace = isWhitespace;
    }

    if (!lastWhitespace) {
        // end of word
        widths.push_back(wordWidth);
        indices.push_back(l);
        isWhitespaces.push_back(false);
    }

    // align
    jfloat x = 0;
    jfloat y = 0;

    int dataLen = widths.size();

    for (int i = 0; i < dataLen; i++) {
        jfloat wordWidth = widths[i];
        jboolean isWhitespace = isWhitespaces[i];
        jint index = indices[i];
        jint lastIndex = i == 0 ? 0 : indices[i - 1];

        if (x + wordWidth > width) {
            if (isWhitespace) {
                if (contentChars[lastIndex] == '\n') {
                    y += lineHeight * paragraphSpacing;
                } else {
                    y += lineHeight;
                }
                x = 0;
            } else {
                if (wordWidth > width) {
                    jint wordPartialIndex = 0;
                    jint wordIndexMax = index - lastIndex;
                    while (wordPartialIndex < wordIndexMax) {
                        jfloat remainingWidth = width - x;
                        while (wordPartialIndex < wordIndexMax) {
                            if (remainingWidth -
                                charWidthsPointer[contentChars[lastIndex + wordPartialIndex]] < 0) {
                                break;
                            }
                            remainingWidth -= charWidthsPointer[contentChars[lastIndex +
                                                                             wordPartialIndex]];
                            wordPartialIndex++;
                        }
                        env->CallVoidMethod(textDrawer, drawTextMethod,
                                            lastIndex + wordPartialIndex, index, x, y + offset);
                        if (wordPartialIndex < wordIndexMax) {
                            x = 0;
                            y += lineHeight;
                        }
                    }
                } else {
                    y += lineHeight;
                    env->CallVoidMethod(textDrawer, drawTextMethod, lastIndex, index, 0.0f,
                                        y + offset);
                    x = wordWidth;
                }
            }
        } else {
            env->CallVoidMethod(textDrawer, drawTextMethod, lastIndex, index, x, y + offset);
            x += wordWidth;
        }
    }

    // cleanup
    env->ReleaseStringChars(content, contentChars);
    env->ReleaseFloatArrayElements(charWidths, charWidthsPointer, 0);
}
