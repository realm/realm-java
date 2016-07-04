#include <object-store/src/object_store.hpp>
#include "io_realm_internal_SharedRealm.h"

#include "shared_realm.hpp"
#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeCreateConfig
(JNIEnv *env, jclass, jstring realm_path, jbyteArray key, jboolean read_only, jboolean in_memory,
 jboolean cache, jboolean disable_format_upgrade, jboolean auto_change_notification) {
    TR_ENTER()

    JStringAccessor path(env, realm_path);
    JniByteArray key_array(env, key);
    Realm::Config *config = new Realm::Config();
    config->path = path;
    config->encryption_key = key_array;
    config->read_only = read_only;
    config->in_memory = in_memory;
    config->cache = cache;
    config->disable_format_upgrade = disable_format_upgrade;
    config->automatic_change_notifications = auto_change_notification;
    return reinterpret_cast<jlong>(config);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCloseConfig
(JNIEnv *, jclass, jlong config_ptr) {
    TR_ENTER_PTR(config_ptr)

    auto config = reinterpret_cast<realm::Realm::Config*>(config_ptr);
    delete config;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeGetSharedRealm
(JNIEnv *env, jclass, jlong config_ptr) {
    TR_ENTER_PTR(config_ptr)

    auto config = reinterpret_cast<realm::Realm::Config*>(config_ptr);
    try {
        auto shared_realm = Realm::get_shared_realm(*config);
        return reinterpret_cast<jlong>(new SharedRealm(std::move(shared_realm)));
    } CATCH_STD()
    return NULL;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCloseSharedRealm
(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto ptr = reinterpret_cast<SharedRealm*>(shared_realm_ptr);
    delete ptr;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeBeginTransaction
(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->begin_transaction();
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCommitTransaction
(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->commit_transaction();
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCancelTransaction
(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->cancel_transaction();
    } CATCH_STD()

}


JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeIsInTransaction(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return static_cast<jboolean>(shared_realm->is_in_transaction());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeReadGroup
(JNIEnv *, jclass , jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return reinterpret_cast<jlong>(shared_realm->read_group());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeGetVersion
(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jlong>(ObjectStore::get_schema_version(shared_realm->read_group()));
    } CATCH_STD()

    // FIXME: Use constant value
    return -1;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeIsEmpty(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jboolean>(ObjectStore::is_empty(shared_realm->read_group()));
    } CATCH_STD()
    return static_cast<jboolean>(false);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRefresh__J(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->refresh();
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRefresh__JJJ(JNIEnv *env, jclass, jlong shared_realm_ptr,
                                                      jlong version, jlong index) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    SharedGroup::VersionID versionID(static_cast<SharedGroup::version_type>(version),
                                     static_cast<uint32_t>(index));
    try {
        shared_realm->refresh(versionID);
    } CATCH_STD()
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_SharedRealm_nativeGetVersionID(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        SharedGroup::VersionID version_id = shared_realm->get_version_of_current_transaction();

        jlong version_array [2];
        version_array[0] = static_cast<jlong>(version_id.version);
        version_array[1] = static_cast<jlong>(version_id.index);

        jlongArray version_data = env->NewLongArray(2);
        if (version_data == NULL) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to return versionID.");
            return NULL;
        }
        env->SetLongArrayRegion(version_data, 0, 2, version_array);

        return version_data;
    } CATCH_STD ()

    return NULL;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeIsClosed(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return static_cast<jboolean>(shared_realm->is_closed());
}


JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeGetTable(JNIEnv *env, jclass, jlong shared_realm_ptr,
                                                  jstring table_name) {
    TR_ENTER_PTR(shared_realm_ptr)

    try {
        JStringAccessor name(env, table_name); // throws
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        if (!shared_realm->read_group()->has_table(name) && !shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Table " << name << " doesn't exist and the shared realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return NULL;
        }
        Table* pTable = LangBindHelper::get_or_add_table(*shared_realm->read_group(), name);
        return reinterpret_cast<jlong>(pTable);
    } CATCH_STD()

    return NULL;
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_SharedRealm_nativeGetTableName(JNIEnv *env, jclass, jlong shared_realm_ptr,
                                                      jint index) {

    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return to_jstring(env, shared_realm->read_group()->get_table_name(S(index)));
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeHasTable(JNIEnv *env, jclass, jlong shared_realm_ptr,
                                                  jstring table_name) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor name(env, table_name);
        return static_cast<jboolean >(shared_realm->read_group()->has_table(name));
    } CATCH_STD()
    return static_cast<jboolean >(false);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRenameTable(JNIEnv *env, jclass, jlong shared_realm_ptr,
                                                     jstring old_table_name, jstring new_table_name) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor old_name(env, old_table_name);
        if (!shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Table " << old_name << " cannot be removed when the shared realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return;
        }
        JStringAccessor new_name(env, new_table_name);
        shared_realm->read_group()->rename_table(old_name, new_name);
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRemoveTable(JNIEnv *env, jclass, jlong shared_realm_ptr,
                                                     jstring table_name) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor name(env, table_name);
        if (!shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Table " << name << " cannot be removed when the shared realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return;
        }
        shared_realm->read_group()->remove_table(name);
    } CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeSize(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return shared_realm->read_group()->size();
    } CATCH_STD()

    return 0;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeWriteCopy(JNIEnv *env, jclass,
        jlong shared_realm_ptr, jstring path, jbyteArray key) {
    TR_ENTER_PTR(shared_realm_ptr);

    JStringAccessor path_str(env, path);
    JniByteArray key_buffer(env, key);
    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->write_copy(path_str, key_buffer);
    } CATCH_STD()
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeWaitForChange(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jboolean>(ObjectStore::wait_for_change(shared_realm));
    } CATCH_STD()

    return static_cast<jboolean>(false);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeStopWaitForChange(JNIEnv *env, jclass, jlong shared_realm_ptr) {

    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        ObjectStore::wait_for_change_release(shared_realm);
    } CATCH_STD()
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeCompact(JNIEnv *env, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jboolean>(shared_realm->compact());
    } CATCH_STD()

    return static_cast<jboolean>(false);
}
