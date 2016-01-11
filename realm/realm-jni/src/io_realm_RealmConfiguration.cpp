#include "io_realm_RealmConfiguration.h"
#include "shared_realm.hpp"

JNIEXPORT jlong JNICALL Java_io_realm_RealmConfiguration_createConfigurationPointer
  (JNIEnv *env, jobject)
{
    // FIXME: can throw
    realm::Realm::Config* config = new realm::Realm::Config;
    return reinterpret_cast<jlong>(config);
}
