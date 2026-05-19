#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "RimeJNIBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_secure_ime_rime_wrapper_RimeBridge_init(
    JNIEnv *env, jobject thiz, jstring shared_data_dir, jstring user_data_dir) {

    const char *shared_dir = env->GetStringUTFChars(shared_data_dir, nullptr);
    const char *user_dir = env->GetStringUTFChars(user_data_dir, nullptr);

    LOGI("RimeBridge init: shared=%s, user=%s", shared_dir, user_dir);

    // Stub: return a non-zero session ID to indicate success
    // Real implementation will call RimeApi->create_session()
    jlong session_id = 1;

    env->ReleaseStringUTFChars(shared_data_dir, shared_dir);
    env->ReleaseStringUTFChars(user_data_dir, user_dir);

    LOGI("RimeBridge init: session_id=%lld", session_id);
    return session_id;
}

JNIEXPORT jobjectArray JNICALL
Java_com_secure_ime_rime_wrapper_RimeBridge_query(
    JNIEnv *env, jobject thiz, jlong session, jstring pinyin) {

    const char *pinyin_str = env->GetStringUTFChars(pinyin, nullptr);
    LOGI("RimeBridge query: session=%lld, pinyin=%s", session, pinyin_str);

    // Stub: return empty array
    // Real implementation will call RimeApi->simulate_key_sequence() and get_context()
    jclass string_class = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(0, string_class, nullptr);

    env->ReleaseStringUTFChars(pinyin, pinyin_str);
    return result;
}

JNIEXPORT void JNICALL
Java_com_secure_ime_rime_wrapper_RimeBridge_commit(
    JNIEnv *env, jobject thiz, jlong session, jstring text) {

    const char *text_str = env->GetStringUTFChars(text, nullptr);
    LOGI("RimeBridge commit: session=%lld, text=%s", session, text_str);

    // Stub: log only
    // Real implementation will call RimeApi->commit_composition()

    env->ReleaseStringUTFChars(text, text_str);
}

JNIEXPORT void JNICALL
Java_com_secure_ime_rime_wrapper_RimeBridge_reset(
    JNIEnv *env, jobject thiz, jlong session) {

    LOGI("RimeBridge reset: session=%lld", session);

    // Stub: log only
    // Real implementation will call RimeApi->clear_composition()
}

JNIEXPORT void JNICALL
Java_com_secure_ime_rime_wrapper_RimeBridge_destroy(
    JNIEnv *env, jobject thiz, jlong session) {

    LOGI("RimeBridge destroy: session=%lld", session);

    // Stub: log only
    // Real implementation will call RimeApi->destroy_session()
}

} // extern "C"