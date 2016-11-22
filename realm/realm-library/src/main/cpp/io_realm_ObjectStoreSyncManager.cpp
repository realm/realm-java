//
// Created by Nabil HACHICHA on 22/11/2016.
//
#include <jni.h>
#include <jni_util/log.hpp>
#include "io_realm_ObjectStoreSyncManager.h"

JNIEXPORT jstring JNICALL Java_io_realm_ObjectStoreSyncManager_getCurrentUser
        (JNIEnv *, jclass) {
    TR_ENTER()
    return NULL;
}

/*
 * Class:     io_realm_ObjectStoreSyncManager
 * Method:    updateOrCreateUser
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_io_realm_ObjectStoreSyncManager_updateOrCreateUser
        (JNIEnv *, jclass, jstring identity, jstring jsonToken) {
    TR_ENTER()
    throw std::runtime_error("NOT IMPLEMENTED YET");
}

/*
 * Class:     io_realm_ObjectStoreSyncManager
 * Method:    logoutCurrentUser
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_io_realm_ObjectStoreSyncManager_logoutCurrentUser
        (JNIEnv *, jclass) {
    TR_ENTER()
    throw std::runtime_error("NOT IMPLEMENTED YET");
}

/*
 * Class:     io_realm_ObjectStoreSyncManager
 * Method:    configureMetaDataSystem
 * Signature: (Ljava/lang/String;[B)V
 */
JNIEXPORT void JNICALL Java_io_realm_ObjectStoreSyncManager_configureMetaDataSystem
        (JNIEnv *, jclass, jstring baseFile, jbyteArray aesKey) {
    TR_ENTER()
    SyncManager::shared().configure_file_system(rootDirectory.path.UTF8String, mode);
}
