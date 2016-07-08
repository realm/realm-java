#include "io_realm_internal_NativeObjectReference.h"

typedef void (*FinalizeFunc)(jlong);

JNIEXPORT void JNICALL Java_io_realm_internal_NativeObjectReference_nativeCleanUp
(JNIEnv *, jclass, jlong finalizer_ptr, jlong native_ptr) {
    FinalizeFunc finalize_func = reinterpret_cast<FinalizeFunc>(finalizer_ptr);
    finalize_func(native_ptr);
}
