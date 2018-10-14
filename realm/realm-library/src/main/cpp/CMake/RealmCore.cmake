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
include(ExternalProject)

function(build_existing_realm_core core_source_path)
    if (CMAKE_BUILD_TYPE STREQUAL "Debug")
        set(debug_lib_suffix "-dbg")
        add_compile_options(-DREALM_DEBUG)
    else()
        add_compile_options(-DNDEBUG)
    endif()

    ExternalProject_Add(realm-core
        SOURCE_DIR ${core_source_path}
        PREFIX ${core_source_path}/build-android-${ANDROID_ABI}-${CMAKE_BUILD_TYPE}
        CMAKE_ARGS  -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
                    -DANDROID_ABI=${ANDROID_ABI}
                    -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
                    -DREALM_BUILD_LIB_ONLY=YES
                    -DREALM_ENABLE_ENCRYPTION=1
        INSTALL_COMMAND ""
        LOG_CONFIGURE 1
        LOG_BUILD 1
        )

    ExternalProject_Get_Property(realm-core SOURCE_DIR)
    ExternalProject_Get_Property(realm-core BINARY_DIR)

    # Create directories that are included in INTERFACE_INCLUDE_DIRECTORIES, as CMake requires they exist at
    # configure time, when they'd otherwise not be created until we download and extract core.
    file(MAKE_DIRECTORY "${BINARY_DIR}/src")

    set(core_lib_file "${BINARY_DIR}/src/realm/librealm${debug_lib_suffix}.a")
    add_library(lib_realm_core STATIC IMPORTED)
    set_target_properties(lib_realm_core PROPERTIES IMPORTED_LOCATION ${core_lib_file}
        IMPORTED_LINK_INTERFACE_LIBRARIES atomic
        INTERFACE_INCLUDE_DIRECTORIES "${SOURCE_DIR}/src;${BINARY_DIR}/src")

    ExternalProject_Add_Step(realm-core ensure-libraries
        DEPENDEES build
        BYPRODUCTS ${core_lib_file}
        )

    add_dependencies(lib_realm_core realm-core)
endfunction()

# Add the sync released as the library.
function(use_sync_release enable_sync sync_dist_path)
    # Link to core/sync debug lib for debug build if it is debug build and linking with debug core is enabled.
    if (CMAKE_BUILD_TYPE STREQUAL "Debug" AND ${ENABLE_DEBUG_CORE})
        set(debug_lib_suffix "-dbg")
        add_compile_options(-DREALM_DEBUG)
    else()
        add_compile_options(-DNDEBUG)
    endif()

    # Configure import realm core lib
    set(core_lib_path ${sync_dist_path}/librealm-android-${ANDROID_ABI}${debug_lib_suffix}.a)
    if (NOT EXISTS ${core_lib_path})
        if (ARMEABI)
            set(core_lib_path ${sync_dist_path}/librealm-android-arm${debug_lib_suffix}.a)
        elseif (ARMEABI_V7A)
            set(core_lib_path ${sync_dist_path}/librealm-android-arm-v7a${debug_lib_suffix}.a)
        elseif (ARM64_V8A)
            set(core_lib_path ${sync_dist_path}/librealm-android-arm64${debug_lib_suffix}.a)
        else()
            message(FATAL_ERROR "Cannot find core lib file: ${core_lib_path}")
        endif()
    endif()

    add_library(lib_realm_core STATIC IMPORTED)

    # -latomic is not set by default for mips and armv5.
    # See https://code.google.com/p/android/issues/detail?id=182094
    set_target_properties(lib_realm_core PROPERTIES IMPORTED_LOCATION ${core_lib_path}
        IMPORTED_LINK_INTERFACE_LIBRARIES atomic
        INTERFACE_INCLUDE_DIRECTORIES "${sync_dist_path}/include")

    if (enable_sync)
        # Sync static library
        set(sync_lib_path ${sync_dist_path}/librealm-sync-android-${ANDROID_ABI}${debug_lib_suffix}.a)
        # Workaround for old core's funny ABI nicknames
        if (NOT EXISTS ${sync_lib_path})
            if (ARMEABI)
                set(sync_lib_path ${sync_dist_path}/librealm-sync-android-arm${debug_lib_suffix}.a)
            elseif (ARMEABI_V7A)
                set(sync_lib_path ${sync_dist_path}/librealm-sync-android-arm-v7a${debug_lib_suffix}.a)
            elseif (ARM64_V8A)
                set(sync_lib_path ${sync_dist_path}/librealm-sync-android-arm64${debug_lib_suffix}.a)
            else()
                message(FATAL_ERROR "Cannot find sync lib file: ${sync_lib_path}")
            endif()
        endif()
        add_library(lib_realm_sync STATIC IMPORTED)
        set_target_properties(lib_realm_sync PROPERTIES IMPORTED_LOCATION ${sync_lib_path}
            IMPORTED_LINK_INTERFACE_LIBRARIES lib_realm_core)
    endif()

    set(REALM_CORE_INCLUDE_DIR "${sync_dist_path}/include")
endfunction()

# Add core/sync libraries. Set the core_source_path to build core from source.
# FIXME: Build from sync source is not supported yet.
function(use_realm_core enable_sync sync_dist_path core_source_path)
    if (core_source_path)
        build_existing_realm_core(${core_source_path})
    else()
        use_sync_release(${enable_sync} ${sync_dist_path})
    endif()
endfunction()
