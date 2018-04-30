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
set(REALM_COMMON_CXX_FLAGS "")

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

# Generate JNI header files. Each build has its own JNI header in its build_dir/jni_include.
string(TOLOWER ${CMAKE_BUILD_TYPE} build_type_FOLDER)
set(classes_PATH ${CMAKE_SOURCE_DIR}/../../../build/intermediates/classes/${REALM_FLAVOR}/${build_type_FOLDER}/)
set(classes_LIST
    io.realm.internal.Table io.realm.internal.CheckedRow
    io.realm.internal.Util io.realm.internal.UncheckedRow
    io.realm.internal.TableQuery io.realm.internal.OsSharedRealm io.realm.internal.TestUtil
    io.realm.log.LogLevel io.realm.log.RealmLog io.realm.internal.Property io.realm.internal.OsSchemaInfo
    io.realm.internal.OsObjectSchemaInfo io.realm.internal.OsResults
    io.realm.internal.NativeObjectReference io.realm.internal.OsCollectionChangeSet
    io.realm.internal.OsObject io.realm.internal.OsRealmConfig io.realm.internal.OsList
    io.realm.internal.OsObjectStore io.realm.internal.sync.OsSubscription
)
# /./ is the workaround for the problem that AS cannot find the jni headers.
# See https://github.com/googlesamples/android-ndk/issues/319
set(jni_headers_PATH /./${PROJECT_BINARY_DIR}/jni_include)
if (build_SYNC)
    list(APPEND classes_LIST
        io.realm.ClientResetRequiredError io.realm.RealmFileUserStore
        io.realm.SyncManager io.realm.SyncSession io.realm.SyncUser
    )
endif()
create_javah(TARGET jni_headers
    CLASSES ${classes_LIST}

    CLASSPATH ${classes_PATH}
    OUTPUT_DIR ${jni_headers_PATH}
    DEPENDS ${classes_PATH}
)

include(RealmCore)

use_realm_core(${build_SYNC} "${REALM_CORE_DIST_DIR}" "${CORE_SOURCE_PATH}")

# Download openssl lib
#string(TOLOWER "${CMAKE_BUILD_TYPE}" openssl_build_TYPE)
set(openssl_build_TYPE "release")
# FIXME Read the openssl version from core when the core/sync release has that information.
set(openssl_VERSION "1.0.2k")
set(openssl_BUILD_NUMBER "1")
set(openssl_FILENAME "openssl-${openssl_build_TYPE}-${openssl_VERSION}-${openssl_BUILD_NUMBER}-Android-${ANDROID_ABI}")
set(openssl_URL "http://static.realm.io/downloads/openssl/${openssl_VERSION}/Android/${ANDROID_ABI}/${openssl_FILENAME}.tar.gz")

message(STATUS "Downloading OpenSSL...")
file(DOWNLOAD "${openssl_URL}" "${PROJECT_BINARY_DIR}/${openssl_FILENAME}.tar.gz")

message(STATUS "Uncompressing OpenSSL...")
execute_process(COMMAND ${CMAKE_COMMAND} -E tar xfz "${PROJECT_BINARY_DIR}/${openssl_FILENAME}.tar.gz"
    WORKING_DIRECTORY "${PROJECT_BINARY_DIR}")

message(STATUS "Importing OpenSSL...")
include(${PROJECT_BINARY_DIR}/${openssl_FILENAME}/openssl.cmake)
get_target_property(openssl_include_DIR crypto INTERFACE_INCLUDE_DIRECTORIES)
get_target_property(crypto_LIB crypto IMPORTED_LOCATION)
get_target_property(ssl_LIB ssl IMPORTED_LOCATION)

# build application's shared lib
include_directories(
    ${CMAKE_SOURCE_DIR}
    ${jni_headers_PATH}
    ${CMAKE_SOURCE_DIR}/object-store/src)

set(ANDROID_STL "gnustl_static")
set(ANDROID_NO_UNDEFINED OFF)
set(ANDROID_SO_UNDEFINED ON)

if (ARMEABI)
    set(ABI_CXX_FLAGS "-mthumb")
elseif (ARMEABI_V7A)
    set(ABI_CXX_FLAGS "-mthumb -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16")
endif()

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
    -Wno-missing-field-initializers  -Wno-maybe-uninitialized -Wno-uninitialized")
set(REALM_COMMON_CXX_FLAGS "${REALM_COMMON_CXX_FLAGS} -DREALM_ANDROID -DREALM_HAVE_CONFIG -DPIC -pthread -fvisibility=hidden -std=c++14 -fsigned-char")
if (build_SYNC)
    set(REALM_COMMON_CXX_FLAGS "${REALM_COMMON_CXX_FLAGS} -DREALM_ENABLE_SYNC=1")
endif()
# There might be an issue with -Os of ndk gcc 4.9. It will hang the encryption related tests.
# And this issue doesn't seem to impact the core compiling.
set(CMAKE_CXX_FLAGS_RELEASE "-O2 -DNDEBUG")
#-ggdb doesn't play well with -flto
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -ggdb -Og")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${REALM_COMMON_CXX_FLAGS} ${WARNING_CXX_FLAGS} ${ABI_CXX_FLAGS}")

# Set link flags
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
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_ClientResetRequiredError.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_RealmFileUserStore.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_SyncManager.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_SyncSession.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/io_realm_internal_sync_OsSubscription.cpp
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
        "object-store/src/sync/impl/*.cpp")
endif()

add_library(realm-jni SHARED ${jni_SRC} ${objectstore_SRC} ${objectstore_sync_SRC})
add_dependencies(realm-jni jni_headers)

if (build_SYNC)
    target_link_libraries(realm-jni log android lib_realm_sync crypto ssl)
else()
    target_link_libraries(realm-jni log android lib_realm_core crypto)
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
