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
    Realm::Config *config = new Realm::Config();
    config->path = path;
    if (key != nullptr) {
        config->encryption_key = jbytearray_to_vector(env, key);
    }
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
    delete RC(config_ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeGetSharedRealm
(JNIEnv *, jclass, jlong config_ptr) {
    TR_ENTER_PTR(config_ptr)
    SharedRealm shared_realm = Realm::get_shared_realm(*RC(config_ptr));
    return reinterpret_cast<jlong>(new SharedRealm(shared_realm));
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCloseSharedRealm
(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    delete SR(shared_realm_ptr);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeBeginTransaction
(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    (*SR(shared_realm_ptr))->begin_transaction();
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCommitTransaction
(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    (*SR(shared_realm_ptr))->commit_transaction();
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCancelTransaction
(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    (*SR(shared_realm_ptr))->cancel_transaction();
}


JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeIsInTransaction(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    return (*SR(shared_realm_ptr))->is_in_transaction();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeReadGroup
(JNIEnv *, jclass , jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    return reinterpret_cast<jlong>((*SR(shared_realm_ptr))->read_group());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeGetVersion
(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    return ObjectStore::get_schema_version((*SR(shared_realm_ptr))->read_group());
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeIsEmpty(JNIEnv, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    return ObjectStore::is_empty((*SR(shared_realm_ptr))->read_group());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeGetSharedGroup(JNIEnv *, jclass , jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    return reinterpret_cast<jlong>(&(*SR(shared_realm_ptr))->get_shared_group());
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRefresh__J(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    (*SR(shared_realm_ptr))->refresh();
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRefresh__JJJ(JNIEnv *, jclass, jlong shared_realm_ptr,
                                                      jlong version, jlong index) {
    TR_ENTER_PTR(shared_realm_ptr)

    SharedGroup::VersionID versionID(version, index);
    (*SR(shared_realm_ptr))->refresh(versionID);
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_SharedRealm_nativeGetVersionID(JNIEnv *env, jclass type, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)

    SharedGroup::VersionID version_id = (*SR(shared_realm_ptr))->get_shared_group().get_version_of_current_transaction();

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
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeIsClosed(JNIEnv *, jclass, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr)
    return static_cast<jboolean>((*SR(shared_realm_ptr))->is_closed());
}

