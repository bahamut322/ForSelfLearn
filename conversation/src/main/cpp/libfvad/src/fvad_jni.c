#include <jni.h>
#include "../include/fvad.h"

JNIEXPORT jlong JNICALL Java_com_sendi_fooddeliveryrobot_FvadWrapper_createFvad(JNIEnv *env, jobject obj) {
    Fvad *inst = fvad_new();
    return (jlong) inst;
}

JNIEXPORT void JNICALL Java_com_sendi_fooddeliveryrobot_FvadWrapper_destroyFvad(JNIEnv *env, jobject obj, jlong handle) {
Fvad *inst = (Fvad*) handle;
fvad_free(inst);
}

JNIEXPORT void JNICALL Java_com_sendi_fooddeliveryrobot_FvadWrapper_reset(JNIEnv *env, jobject obj, jlong handle) {
Fvad *inst = (Fvad*)handle;
fvad_reset(inst);
}

JNIEXPORT jint JNICALL Java_com_sendi_fooddeliveryrobot_FvadWrapper_setMode(JNIEnv *env, jobject obj, jlong handle, jint mode) {
Fvad *inst = (Fvad*) handle;
return fvad_set_mode(inst, mode);
}

JNIEXPORT jint JNICALL Java_com_sendi_fooddeliveryrobot_FvadWrapper_setSampleRate(JNIEnv *env, jobject obj, jlong handle, jint sampleRate) {
Fvad *inst = (Fvad*) handle;
return fvad_set_sample_rate(inst, sampleRate);
}

JNIEXPORT jint JNICALL Java_com_sendi_fooddeliveryrobot_FvadWrapper_process(JNIEnv *env, jobject obj, jlong handle, jshortArray frame, jint length) {
Fvad *inst = (Fvad*) handle;
const jshort *frameData = (*env)->GetShortArrayElements(env, frame, NULL);
int result = fvad_process(inst, frameData, length);
(*env)->ReleaseShortArrayElements(env, frame, frameData, JNI_ABORT);
return result;
}