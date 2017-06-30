cmake_minimum_required(VERSION 3.6.0)

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
    io.realm.internal.LinkView io.realm.internal.Util io.realm.internal.UncheckedRow
    io.realm.internal.TableQuery io.realm.internal.SharedRealm io.realm.internal.TestUtil
    io.realm.log.LogLevel io.realm.log.RealmLog io.realm.internal.Property io.realm.internal.OsSchemaInfo
    io.realm.internal.OsObjectSchemaInfo io.realm.internal.Collection
    io.realm.internal.NativeObjectReference io.realm.internal.CollectionChangeSet
    io.realm.internal.OsObject
)
# /./ is the workaround for the problem that AS cannot find the jni headers.
# See https://github.com/googlesamples/android-ndk/issues/319
set(jni_headers_PATH /./${PROJECT_BINARY_DIR}/jni_include)
if (build_SYNC)
    list(APPEND classes_LIST
        io.realm.ClientResetRequiredError io.realm.RealmFileUserStore
        io.realm.SyncManager io.realm.SyncSession
    )
endif()
create_javah(TARGET jni_headers
    CLASSES ${classes_LIST}

    CLASSPATH ${classes_PATH}
    OUTPUT_DIR ${jni_headers_PATH}
    DEPENDS ${classes_PATH}
)

# TODO: Ideally the debug build should link with core's debug build. But core dbg lib has
# some compile options problems with arm, especially with macro REALM_DEBUG. Link to core
# dbg for debug build when that gets solved.
# We always link to the non-dbg version of core libs for now.
# This means only JNI part has debugging symbols with debug build.
# Debugging with core source code will also be done though anther approach -- compiling the core
# with cmake inside android project.
# Configure import realm core lib
set(core_lib_PATH ${REALM_CORE_DIST_DIR}/librealm-android-${ANDROID_ABI}.a)
# Workaround for old core's funny ABI nicknames
if (NOT EXISTS ${core_lib_PATH})
    if (ARMEABI)
        set(core_lib_PATH ${REALM_CORE_DIST_DIR}/librealm-android-arm.a)
    elseif (ARMEABI_V7A)
        set(core_lib_PATH ${REALM_CORE_DIST_DIR}/librealm-android-arm-v7a.a)
    elseif (ARM64_V8A)
        set(core_lib_PATH ${REALM_CORE_DIST_DIR}/librealm-android-arm64.a)
    else()
        message(FATAL_ERROR "Cannot find core lib file: ${core_lib_PATH}")
    endif()
endif()

add_library(lib_realm_core STATIC IMPORTED)

# -latomic is not set by default for mips and armv5.
# See https://code.google.com/p/android/issues/detail?id=182094
set_target_properties(lib_realm_core PROPERTIES IMPORTED_LOCATION ${core_lib_PATH}
                                                IMPORTED_LINK_INTERFACE_LIBRARIES atomic)

if (build_SYNC)
    # Sync static library
    set(sync_lib_PATH ${REALM_CORE_DIST_DIR}/librealm-sync-android-${ANDROID_ABI}.a)
    # Workaround for old core's funny ABI nicknames
    if (NOT EXISTS ${sync_lib_PATH})
        if (ARMEABI)
            set(sync_lib_PATH ${REALM_CORE_DIST_DIR}/librealm-sync-android-arm.a)
        elseif (ARMEABI_V7A)
            set(sync_lib_PATH ${REALM_CORE_DIST_DIR}/librealm-sync-android-arm-v7a.a)
        elseif (ARM64_V8A)
            set(sync_lib_PATH ${REALM_CORE_DIST_DIR}/librealm-sync-android-arm64.a)
        else()
            message(FATAL_ERROR "Cannot find sync lib file: ${sync_lib_PATH}")
        endif()
    endif()
    add_library(lib_realm_sync STATIC IMPORTED)
    set_target_properties(lib_realm_sync PROPERTIES IMPORTED_LOCATION ${sync_lib_PATH}
                                                    IMPORTED_LINK_INTERFACE_LIBRARIES lib_realm_core)
endif()

# build application's shared lib
include_directories(${REALM_CORE_DIST_DIR}/include
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
set(CMAKE_CXX_FLAGS_DEBUG "-ggdb -Og -DNDEBUG")
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
    target_link_libraries(realm-jni log android lib_realm_sync)
else()
    target_link_libraries(realm-jni log android lib_realm_core)
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
