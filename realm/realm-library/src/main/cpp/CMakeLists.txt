###########################################################################
#
# Copyright 2017 Realm Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
###########################################################################
cmake_minimum_required(VERSION 3.6.0)

# loading dependencies properties
file(STRINGS "${CMAKE_SOURCE_DIR}/../../../../../dependencies.list" DEPENDENCIES)
foreach(LINE IN LISTS DEPENDENCIES)
    string(REGEX MATCHALL "([^=]+)" KEY_VALUE "${LINE}")
    list(LENGTH KEY_VALUE matches_count)
    if(matches_count STREQUAL 2)
        list(GET KEY_VALUE 0 KEY)
        list(GET KEY_VALUE 1 VALUE)
        set(DEP_${KEY} ${VALUE})
    endif()
endforeach()

FUNCTION(capitalizeFirstLetter var value)
    string(SUBSTRING ${value} 0 1 firstLetter)
    string(TOUPPER ${firstLetter} firstLetter)
    string(REGEX REPLACE "^.(.*)" "${firstLetter}\\1" value "${value}")
    set(${var} "${value}" PARENT_SCOPE)
ENDFUNCTION(capitalizeFirstLetter)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/CMake")

# find javah
find_package(Java COMPONENTS Development)
if (NOT Java_Development_FOUND)
    if (DEFINED ENV{JAVA_HOME} AND EXISTS "$ENV{JAVA_HOME}/bin/javah")
      set(Java_JAVAH_EXECUTABLE "$ENV{JAVA_HOME}/bin/javah")
    elseif (EXISTS "/usr/bin/javah")
      set(Java_JAVAH_EXECUTABLE "/usr/bin/javah")
    else()
      message(FATAL_ERROR "Cannot find javah")
    endif()
endif()
include (UseJava)

set(CMAKE_VERBOSE_MAKEFILE ON)
# Generate compile_commands.json
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# Initialize common compile & link flags.
set(REALM_LINKER_FLAGS "")
set(REALM_COMMON_CXX_FLAGS "-DREALM_PLATFORM_JAVA=1")

# Setup lcache
if(NDK_LCACHE)
    set(CMAKE_CXX_CREATE_SHARED_LIBRARY "${NDK_LCACHE} ${CMAKE_CXX_CREATE_SHARED_LIBRARY}")
endif()

# Set flag build_SYNC
if (REALM_FLAVOR STREQUAL base)
    set(build_SYNC OFF)
else()
    set(build_SYNC ON)
endif()

# Format strings used to represent build parameters: Variant and Type
string(TOLOWER ${CMAKE_BUILD_TYPE} build_type_FOLDER)
set(realmFlavorCap "")
set(buildTypeCap "")
capitalizeFirstLetter(realmFlavorCap "${REALM_FLAVOR}")
capitalizeFirstLetter(buildTypeCap "${CMAKE_BUILD_TYPE}")

# Generate JNI header files. Each build has its own JNI header in its build_dir/jni_include.
# WARNING: The classes_PATH is not part the public API offered by the Android Gradle Plugin
# so it might change without warning when upgrading the plugin.
file(DOWNLOAD "https://repo1.maven.org/maven2/org/mongodb/bson/${DEP_BSON_DEPENDENCY}/bson-${DEP_BSON_DEPENDENCY}.jar" "${PROJECT_BINARY_DIR}/bson-${DEP_BSON_DEPENDENCY}.jar")
set(bsonlib_PATH ${PROJECT_BINARY_DIR}/bson-${DEP_BSON_DEPENDENCY}.jar)
set(classes_PATH ${CMAKE_SOURCE_DIR}/../../../build/intermediates/javac/${REALM_FLAVOR}${buildTypeCap}/classes/)
set(classes_LIST
    io.realm.RealmQuery
    io.realm.internal.Table io.realm.internal.CheckedRow
    io.realm.internal.Util io.realm.internal.UncheckedRow
    io.realm.internal.TableQuery io.realm.internal.OsSharedRealm io.realm.internal.TestUtil
    io.realm.log.LogLevel io.realm.log.RealmLog io.realm.internal.Property io.realm.internal.OsSchemaInfo
    io.realm.internal.OsObjectSchemaInfo io.realm.internal.OsResults
    io.realm.internal.NativeObjectReference io.realm.internal.OsCollectionChangeSet
    io.realm.internal.OsObject io.realm.internal.OsRealmConfig io.realm.internal.OsList
    io.realm.internal.OsObjectStore
    io.realm.internal.core.DescriptorOrdering io.realm.internal.core.IncludeDescriptor
    io.realm.internal.objectstore.OsObjectBuilder
)
# /./ is the workaround for the problem that AS cannot find the jni headers.
# See https://github.com/googlesamples/android-ndk/issues/319
set(jni_headers_PATH /./${PROJECT_BINARY_DIR}/jni_include)
if (build_SYNC)
    list(APPEND classes_LIST
        io.realm.mongodb.App
        io.realm.mongodb.ApiKeyAuthImpl
        io.realm.mongodb.EmailPasswordAuthImpl
        io.realm.mongodb.FunctionsImpl
        io.realm.mongodb.sync.ClientResetRequiredError
        io.realm.mongodb.sync.Sync
        io.realm.mongodb.sync.SyncSession
        io.realm.mongodb.User
        io.realm.internal.objectstore.OsApp
        io.realm.internal.objectstore.OsAppCredentials
        io.realm.internal.objectstore.OsAsyncOpenTask
        io.realm.internal.objectstore.OsJavaNetworkTransport
        io.realm.internal.objectstore.OsMongoClient
        io.realm.internal.objectstore.OsMongoCollection
        io.realm.internal.objectstore.OsWatchStream
        io.realm.internal.objectstore.OsMongoDatabase
        io.realm.internal.objectstore.OsPush
        io.realm.internal.objectstore.OsSyncUser
        io.realm.mongodb.mongo.iterable.AggregateIterable
        io.realm.mongodb.mongo.iterable.FindIterable
    )
endif()
create_javah(TARGET jni_headers
    CLASSES ${classes_LIST}
    CLASSPATH ${classes_PATH} $ENV{ANDROID_HOME}/platforms/android-29/android.jar ${bsonlib_PATH}
    OUTPUT_DIR ${jni_headers_PATH}
    DEPENDS ${classes_PATH}
)

include(RealmCore)
use_realm_core(${build_SYNC} "${REALM_CORE_DIST_DIR}" "${CORE_SOURCE_PATH}")

# Download OpenSSL lib
# FIXME Read the openssl version from core when the core/sync release has that information.
set(openssl_VERSION "1.1.1b")
set(openssl_FILENAME "openssl.tgz")
set(openssl_URL "https://static.realm.io/downloads/openssl/${openssl_VERSION}/Android/${ANDROID_ABI}/${openssl_FILENAME}")

message(STATUS "Downloading OpenSSL...")
file(DOWNLOAD "${openssl_URL}" "${PROJECT_BINARY_DIR}/${openssl_FILENAME}")

message(STATUS "Uncompressing OpenSSL: ${PROJECT_BINARY_DIR}/${openssl_FILENAME}")
execute_process(COMMAND ${CMAKE_COMMAND} -E tar xfz "${openssl_FILENAME}" WORKING_DIRECTORY "${PROJECT_BINARY_DIR}")
message(STATUS "Importing OpenSSL...")
include(${PROJECT_BINARY_DIR}/lib/cmake/OpenSSL/OpenSSLConfig.cmake)
get_target_property(openssl_include_DIR OpenSSL::Crypto INTERFACE_INCLUDE_DIRECTORIES)
get_target_property(crypto_LIB OpenSSL::Crypto IMPORTED_LOCATION)
get_target_property(ssl_LIB OpenSSL::SSL IMPORTED_LOCATION)

# build application's shared lib
include_directories(
        ${CMAKE_SOURCE_DIR}
        ${jni_headers_PATH}
        ${CMAKE_SOURCE_DIR}/object-store/src
        ${CMAKE_SOURCE_DIR}/object-store/external/json)

# Hack the memmove bug on Samsung device.
if (ARMEABI OR ARMEABI_V7A)
    set(REALM_LINKER_FLAGS "${REALM_LINKER_FLAGS} -Wl,--wrap,memmove -Wl,--wrap,memcpy")
    set(REALM_COMMON_CXX_FLAGS "${REALM_COMMON_CXX_FLAGS} -DREALM_WRAP_MEMMOVE=1")
else()
    set(REALM_COMMON_CXX_FLAGS "${REALM_COMMON_CXX_FLAGS} -DREALM_WRAP_MEMMOVE=0")
endif()

#FIXME uninitialized is reported by query_expression.hpp:1070
#      d.init(ValueBase::m_from_link_list, ValueBase::m_values, D{});
#FIXME maybe-uninitialized is reported by table_view.cpp:272:15:
#     'best.m_nanoseconds' was declared here
#     -Wno-missing-field-initializers disable in object store as well.
set(WARNING_CXX_FLAGS "-Werror -Wall -Wextra -pedantic -Wmissing-declarations \
    -Wempty-body -Wparentheses -Wunknown-pragmas -Wunreachable-code \
    -Wno-missing-field-initializers -Wno-unevaluated-expression -Wno-unreachable-code \
    -Wno-c99-extensions")
set(REALM_COMMON_CXX_FLAGS "${REALM_COMMON_CXX_FLAGS} -DREALM_ANDROID -DREALM_HAVE_CONFIG -DPIC -fdata-sections -pthread -frtti -fvisibility=hidden -fsigned-char -fno-stack-protector -std=c++17")
if (build_SYNC)
    set(REALM_COMMON_CXX_FLAGS "${REALM_COMMON_CXX_FLAGS} -DREALM_ENABLE_SYNC=1")
endif()
set(CMAKE_CXX_FLAGS_RELEASE "-DNDEBUG -Oz")
# -ggdb doesn't play well with -flto
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -glldb -g")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${REALM_COMMON_CXX_FLAGS} ${WARNING_CXX_FLAGS} ${ABI_CXX_FLAGS}")

# Set Linker flags flags
if (CMAKE_BUILD_TYPE STREQUAL "Release")
    set(REALM_LINKER_FLAGS "${REALM_LINKER_FLAGS} -Wl,-gc-sections")
endif()
if (build_SYNC)
    set(REALM_LINKER_FLAGS "${REALM_LINKER_FLAGS} -lz")
endif()
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${REALM_LINKER_FLAGS}")

# JNI source files
file(GLOB jni_SRC
    "*.cpp"
    "jni_util/*.cpp"
    "jni_impl/android_logger.cpp"
)
# Those source file are only needed for sync.
if (NOT build_SYNC)
    list(REMOVE_ITEM jni_SRC
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_mongodb_User.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_mongodb_FunctionsImpl.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_mongodb_EmailPasswordAuthImpl.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_mongodb_ApiKeyAuthImpl.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_mongodb_sync_ClientResetRequiredError.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_mongodb_sync_Sync.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_mongodb_sync_SyncSession.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsApp.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsAsyncOpenTask.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsAppCredentials.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsJavaNetworkTransport.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsMongoClient.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsMongoCollection.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsWatchStream.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsMongoDatabase.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsPush.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_objectstore_OsSyncUser.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_mongodb_mongo_iterable_AggregateIterable.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_mongodb_mongo_iterable_FindIterable.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/jni_util/bson_util.cpp
    )
endif()

# Object Store source files
file(GLOB objectstore_SRC
    "object-store/src/*.cpp"
    "object-store/src/impl/*.cpp"
    "object-store/src/impl/epoll/*.cpp"
    "object-store/src/util/*.cpp"
    "object-store/src/impl/epoll/*.cpp"
    "object-store/src/util/android/*.cpp")

# Sync needed Object Store files
if (build_SYNC)
    file(GLOB objectstore_sync_SRC
        "object-store/src/results.cpp"
        "object-store/src/impl/results_notifier.cpp"
        "object-store/src/sync/*.cpp"
        "object-store/src/util/bson/*.cpp"        
        "object-store/src/sync/impl/*.cpp")
endif()

add_library(realm-jni SHARED ${jni_SRC} ${objectstore_SRC} ${objectstore_sync_SRC})
add_dependencies(realm-jni jni_headers)

if (build_SYNC)
    target_link_libraries(realm-jni log android lib_realm_sync OpenSSL::SSL OpenSSL::Crypto)
else()
    target_link_libraries(realm-jni log android lib_realm_core OpenSSL::Crypto)
endif()

# Strip the release so files and backup the unstripped versions
if (CMAKE_BUILD_TYPE STREQUAL "Release")
    set(unstripped_SO_DIR
        "${CMAKE_SOURCE_DIR}/../../../build/outputs/jniLibs-unstripped/${REALM_FLAVOR}/${ANDROID_ABI}")
    add_custom_command(TARGET realm-jni
        POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E make_directory ${unstripped_SO_DIR}
        COMMAND ${CMAKE_COMMAND} -E copy $<TARGET_FILE:realm-jni> ${unstripped_SO_DIR}
        COMMAND ${CMAKE_STRIP} $<TARGET_FILE:realm-jni>)
endif()
