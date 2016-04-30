#include "io_realm_internal_BatchDeleter.h"
#include "util.hpp"

JNIEXPORT void JNICALL Java_io_realm_internal_BatchDeleter_deleteNativePointers
  (JNIEnv *env, jclass, jlongArray linkViews, jlongArray rows)
{
    if (linkViews) {
        JniLongArray longArray(env, linkViews);
        jsize len = longArray.len();
        for (jsize i = 0; i < len && longArray[i]; i++) {
            TR_ENTER_PTR(longArray[i])
            realm::LangBindHelper::unbind_linklist_ptr(LV(longArray[i]));
        }
    }
    if (rows) {
        JniLongArray longArray(env, rows);
        jsize len = longArray.len();
        for (jsize i = 0; i < len && longArray[i]; i++) {
            TR_ENTER_PTR(longArray[i])
            delete ROW(longArray[i]);
        }
    }
}
