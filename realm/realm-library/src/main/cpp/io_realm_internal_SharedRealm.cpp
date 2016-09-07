#include <object-store/src/object_store.hpp>
#include <object-store/src/object_schema.hpp>
#include "io_realm_internal_SharedRealm.h"

#include "shared_realm.hpp"
#include "util.hpp"

using namespace realm;

static_assert(SchemaMode::Automatic ==
                      static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_AUTOMATIC), "");
static_assert(SchemaMode::ReadOnly ==
                      static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_READONLY), "");
static_assert(SchemaMode::ResetFile ==
                      static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_RESET_FILE), "");
static_assert(SchemaMode::Additive ==
                      static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_ADDITIVE), "");
static_assert(SchemaMode::Manual ==
              static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_MANUAL), "");

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeCreateConfig(JNIEnv *env, jclass, jstring realm_path, jbyteArray key,
        jbyte schema_mode, jboolean in_memory, jboolean cache, jboolean disable_format_upgrade,
        jboolean auto_change_notification)
{
    TR_ENTER()

    try {
        JStringAccessor path(env, realm_path); // throws
        JniByteArray key_array(env, key);
        Realm::Config *config = new Realm::Config();
        config->path = path;
        config->encryption_key = key_array;
        config->schema_mode = SchemaMode(static_cast<uint8_t >(schema_mode));
        config->in_memory = in_memory;
        config->cache = cache;
        config->disable_format_upgrade = disable_format_upgrade;
        config->automatic_change_notifications = auto_change_notification;
        return reinterpret_cast<jlong>(config);
    } CATCH_STD()

    return static_cast<jlong>(NULL);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCloseConfig(JNIEnv *, jclass, jlong config_ptr)
{
    TR_ENTER_PTR(config_ptr)

    auto config = reinterpret_cast<realm::Realm::Config*>(config_ptr);
    delete config;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeGetSharedRealm(JNIEnv *env, jclass, jlong config_ptr)
{
    TR_ENTER_PTR(config_ptr)

    auto config = reinterpret_cast<realm::Realm::Config*>(config_ptr);
    try {
        auto shared_realm = Realm::get_shared_realm(*config);
        return reinterpret_cast<jlong>(new SharedRealm(std::move(shared_realm)));
    } CATCH_STD()
    return static_cast<jlong>(NULL);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCloseSharedRealm(JNIEnv *, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto ptr = reinterpret_cast<SharedRealm*>(shared_realm_ptr);
    delete ptr;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeBeginTransaction(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->begin_transaction();
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCommitTransaction(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->commit_transaction();
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeCancelTransaction(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->cancel_transaction();
    } CATCH_STD()
}


JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeIsInTransaction(JNIEnv *, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return static_cast<jboolean>(shared_realm->is_in_transaction());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeReadGroup(JNIEnv *env, jclass , jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return reinterpret_cast<jlong>(&shared_realm->read_group());
    } CATCH_STD()

    return static_cast<jlong>(NULL);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeGetVersion(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jlong>(ObjectStore::get_schema_version(shared_realm->read_group()));
    } CATCH_STD()

    // FIXME: Use constant value
    return -1;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeIsEmpty(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jboolean>(ObjectStore::is_empty(shared_realm->read_group()));
    } CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRefresh__J(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->refresh();
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRefresh__JJJ(JNIEnv *env, jclass, jlong shared_realm_ptr, jlong version,
        jlong index)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    SharedGroup::VersionID version_id(static_cast<SharedGroup::version_type>(version),
                                     static_cast<uint32_t>(index));
    try {
        using rf = realm::_impl::RealmFriend;
        auto& shared_group = rf::get_shared_group(*shared_realm);
        LangBindHelper::advance_read(shared_group, version_id);
    } CATCH_STD()
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_SharedRealm_nativeGetVersionID(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        using rf = realm::_impl::RealmFriend;
        SharedGroup::VersionID version_id = rf::get_shared_group(*shared_realm).get_version_of_current_transaction();

        jlong version_array[2];
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

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeUpdateSchema(JNIEnv *env, jclass, jlong shared_realm_ptr, jobject /*dynamic_realm*/,
                                                      jlong schema_ptr, jlong schema_version, jobject migration_object) {

    TR_ENTER_PTR(shared_realm_ptr)
    TR("schema %p", VOID_PTR(schema_ptr))
    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    auto schema = reinterpret_cast<Schema*>(schema_ptr);
    auto version = static_cast<uint64_t>(schema_version);
    try {
        if (migration_object == NULL) {
            std::vector<SchemaChange> required_changes = ObjectStore::schema_from_group(shared_realm->read_group()).compare(*schema);
            shared_realm->update_schema(std::move(*schema), version, nullptr);
        }
        else {
            jclass realm_migration_class = env->GetObjectClass(migration_object); // will return io.realm.RealmMigration
            jmethodID realm_migration_method = env->GetMethodID(realm_migration_class, "migrate", "(Lio/realm/DynamicRealm;JJ)V");
            if (realm_migration_method == nullptr) {
                throw std::runtime_error("Cannot find method 'migrate' of class 'io.realm.RealmMigration'.");
            }

            jclass io_realm_dynamicrealm = env->FindClass("io/realm/DynamicRealm");
            if (io_realm_dynamicrealm == nullptr) {
                throw std::runtime_error("Cannot find io.realm.DynamicRealm");
            }
            jmethodID fromSharedRealm = env->GetStaticMethodID(io_realm_dynamicrealm, "fromSharedRealm", "(J)Lio/realm/DynamicRealm;");
            if (fromSharedRealm == nullptr) {
                throw std::runtime_error("Cannot find fromSharedRealm");
            }

            Realm::MigrationFunction migration_function = [=](SharedRealm, SharedRealm realm, Schema &) {
                TR_ERR("migration_function");

                auto &config = realm->config();
                jlong schema_version = jlong(config.schema_version);
                jobject dynamic_realm = env->CallStaticObjectMethod(io_realm_dynamicrealm, fromSharedRealm,
                                                              reinterpret_cast<jlong>(realm.get()));
                if (dynamic_realm == nullptr) {
                    throw std::runtime_error("Cannot get an instance of DynamicRealm.");
                }
                env->CallVoidMethod(migration_object, realm_migration_method, dynamic_realm, schema_version);
            };
            shared_realm->update_schema(std::move(*schema), version, std::move(migration_function));
        }
    } CATCH_STD()

    return 0;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeIsClosed(JNIEnv *, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return static_cast<jboolean>(shared_realm->is_closed());
}


JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeGetTable(JNIEnv *env, jclass, jlong shared_realm_ptr, jstring table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    try {
        JStringAccessor name(env, table_name); // throws
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        if (!shared_realm->read_group().has_table(name) && !shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Class " << name << " doesn't exist and the shared Realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return static_cast<jlong>(NULL);
        }
        Table* pTable = LangBindHelper::get_or_add_table(shared_realm->read_group(), name);
        return reinterpret_cast<jlong>(pTable);
    } CATCH_STD()

    return static_cast<jlong>(NULL);
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_SharedRealm_nativeGetTableName(JNIEnv *env, jclass, jlong shared_realm_ptr, jint index)
{

    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return to_jstring(env, shared_realm->read_group().get_table_name(static_cast<size_t>(index)));
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeHasTable(JNIEnv *env, jclass, jlong shared_realm_ptr, jstring table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor name(env, table_name);
        return static_cast<jboolean>(shared_realm->read_group().has_table(name));
    } CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRenameTable(JNIEnv *env, jclass, jlong shared_realm_ptr,
        jstring old_table_name, jstring new_table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor old_name(env, old_table_name);
        if (!shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Class " << old_name << " cannot be renamed when the realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return;
        }
        JStringAccessor new_name(env, new_table_name);
        shared_realm->read_group().rename_table(old_name, new_name);
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRemoveTable(JNIEnv *env, jclass, jlong shared_realm_ptr, jstring table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor name(env, table_name);
        if (!shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Class " << name << " cannot be removed when the realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return;
        }
        shared_realm->read_group().remove_table(name);
    } CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeSize(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jlong>(shared_realm->read_group().size());
    } CATCH_STD()

    return 0;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeWriteCopy(JNIEnv *env, jclass, jlong shared_realm_ptr, jstring path,
        jbyteArray key)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor path_str(env, path);
        JniByteArray key_buffer(env, key);
        shared_realm->write_copy(path_str, key_buffer);
    } CATCH_STD()
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeWaitForChange(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        using rf = realm::_impl::RealmFriend;
        return static_cast<jboolean>(rf::get_shared_group(*shared_realm).wait_for_change());
    } CATCH_STD()

    return JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeStopWaitForChange(JNIEnv *env, jclass, jlong shared_realm_ptr)
{

    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        using rf = realm::_impl::RealmFriend;
        rf::get_shared_group(*shared_realm).wait_for_change_release();
    } CATCH_STD()
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeCompact(JNIEnv *env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jboolean>(shared_realm->compact());
    } CATCH_STD()

    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeSchema(JNIEnv *env, jclass /*type*/, jlong shared_realm_ptr) {
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        const Schema tmp = shared_realm->schema();
        Schema *schema = new Schema(std::move(tmp));
        return reinterpret_cast<jlong>(schema);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SharedRealm_nativeObjectSchema(JNIEnv *env, jclass, jlong shared_realm_ptr,
                                                      jstring className_) {
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor name(env, className_);
        ObjectSchema object_schema(shared_realm->read_group(), name);
        return reinterpret_cast<jlong>(new ObjectSchema(std::move(object_schema)));
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeRenameField(JNIEnv *env, jclass /*type*/, jlong shared_realm_ptr,
                                                     jstring className_,
                                                     jstring oldName_, jstring newName_) {
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor className(env, className_);
        JStringAccessor oldName(env, oldName_);
        JStringAccessor newName(env, newName_);
        Schema schema = shared_realm->schema();
        ObjectStore::rename_property(shared_realm->read_group(), schema, className, oldName, newName);
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_SharedRealm_nativeHasPrimaryKey(JNIEnv *env, jclass /*type*/, jlong shared_realm_ptr,
                                                       jstring className_) {
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor className(env, className_);
        StringData name = ObjectStore::get_primary_key_for_object(shared_realm->read_group(), className);
        return static_cast<jboolean>(name != "");
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SharedRealm_nativeSetPrimaryKey(JNIEnv *env, jclass /*type*/, jlong shared_realm_ptr,
                                                       jstring className_, jstring fieldName_) {
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm *>(shared_realm_ptr));
        JStringAccessor className(env, className_);
        JStringAccessor fieldName(env, fieldName_);
        ObjectStore::set_primary_key_for_object(shared_realm->read_group(), className, fieldName);
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_SharedRealm_nativeGetPrimaryKey(JNIEnv *env, jclass /*type*/, jlong shared_realm_ptr,
                                                       jstring className_) {
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor className(env, className_);
        StringData name = ObjectStore::get_primary_key_for_object(shared_realm->read_group(), className);
        return to_jstring(env, name);
    }
    CATCH_STD()
    return nullptr;
}

