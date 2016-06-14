include(ExternalProject)

if(${CMAKE_GENERATOR} STREQUAL "Unix Makefiles")
    set(MAKE_EQUAL_MAKE "MAKE=$(MAKE)")
endif()

if(SANITIZE_ADDRESS)
  set(EXPORT_MAKEFLAGS export MAKEFLAGS='EXTRA_CFLAGS=-fsanitize=address EXTRA_LDFLAGS=-fsanitize=address')
else()
  set(EXPORT_MAKEFLAGS true)
endif()

if (${CMAKE_VERSION} VERSION_GREATER "3.4.0")
    set(USES_TERMINAL_BUILD USES_TERMINAL_BUILD 1)
endif()

function(use_realm_core version_or_path_to_source)
    if("${version_or_path_to_source}" MATCHES "^[0-9]+(\\.[0-9])+")
        if(APPLE)
            download_realm_core(${version_or_path_to_source})
        else()
            clone_and_build_realm_core("v${version_or_path_to_source}")
        endif()
    else()
        build_existing_realm_core(${version_or_path_to_source})
    endif()
    set(REALM_CORE_INCLUDE_DIR ${REALM_CORE_INCLUDE_DIR} PARENT_SCOPE)
endfunction()

function(download_realm_core core_version)
    set(core_url "https://static.realm.io/downloads/core/realm-core-${core_version}.tar.bz2")
    set(core_tarball_name "realm-core-${core_version}.tar.bz2")
    set(core_temp_tarball "/tmp/${core_tarball_name}")
    set(core_directory_parent "${CMAKE_CURRENT_SOURCE_DIR}${CMAKE_FILES_DIRECTORY}")
    set(core_directory "${core_directory_parent}/realm-core-${core_version}")
    set(core_tarball "${core_directory_parent}/${core_tarball_name}")

    if (NOT EXISTS ${core_tarball})
        if (NOT EXISTS ${core_temp_tarball})
            message("Downloading core ${core_version} from ${core_url}.")
            file(DOWNLOAD ${core_url} ${core_temp_tarball}.tmp SHOW_PROGRESS)
            file(RENAME ${core_temp_tarball}.tmp ${core_temp_tarball})
        endif()
        file(COPY ${core_temp_tarball} DESTINATION ${core_directory_parent})
    endif()

    set(core_library_debug ${core_directory}/librealm-dbg.a)
    set(core_library_release ${core_directory}/librealm.a)
    set(core_libraries ${core_library_debug} ${core_library_release})

    add_custom_command(
        COMMENT "Extracting ${core_tarball_name}"
        OUTPUT ${core_libraries}
        DEPENDS ${core_tarball}
        COMMAND ${CMAKE_COMMAND} -E tar xf ${core_tarball}
        COMMAND ${CMAKE_COMMAND} -E remove_directory ${core_directory}
        COMMAND ${CMAKE_COMMAND} -E rename core ${core_directory}
        COMMAND ${CMAKE_COMMAND} -E touch_nocreate ${core_libraries})

    add_custom_target(realm-core DEPENDS ${core_libraries})

    add_library(realm STATIC IMPORTED)
    add_dependencies(realm realm-core)
    set_property(TARGET realm PROPERTY IMPORTED_LOCATION_DEBUG ${core_library_debug})
    set_property(TARGET realm PROPERTY IMPORTED_LOCATION_COVERAGE ${core_library_debug})
    set_property(TARGET realm PROPERTY IMPORTED_LOCATION_RELEASE ${core_library_release})
    set_property(TARGET realm PROPERTY IMPORTED_LOCATION ${core_library_release})

    set(REALM_CORE_INCLUDE_DIR ${core_directory}/include PARENT_SCOPE)
endfunction()

macro(define_built_realm_core_target core_directory)
    set(core_library_debug ${core_directory}/src/realm/librealm-dbg.a)
    set(core_library_release ${core_directory}/src/realm/librealm.a)
    set(core_libraries ${core_library_debug} ${core_library_release})

    ExternalProject_Add_Step(realm-core ensure-libraries
        COMMAND ${CMAKE_COMMAND} -E touch_nocreate ${core_libraries}
        OUTPUT ${core_libraries}
        DEPENDEES build
        )

    add_library(realm STATIC IMPORTED)
    add_dependencies(realm realm-core)

    set_property(TARGET realm PROPERTY IMPORTED_LOCATION_DEBUG ${core_library_debug})
    set_property(TARGET realm PROPERTY IMPORTED_LOCATION_COVERAGE ${core_library_debug})
    set_property(TARGET realm PROPERTY IMPORTED_LOCATION_RELEASE ${core_library_release})
    set_property(TARGET realm PROPERTY IMPORTED_LOCATION ${core_library_release})

    set(REALM_CORE_INCLUDE_DIR ${core_directory}/src PARENT_SCOPE)
endmacro()

function(clone_and_build_realm_core branch)
    set(core_prefix_directory "${CMAKE_CURRENT_SOURCE_DIR}${CMAKE_FILES_DIRECTORY}/realm-core")
    ExternalProject_Add(realm-core
        GIT_REPOSITORY "git@github.com:realm/realm-core.git"
        GIT_TAG ${branch}
        PREFIX ${core_prefix_directory}
        BUILD_IN_SOURCE 1
        CONFIGURE_COMMAND ""
        BUILD_COMMAND ${EXPORT_MAKEFLAGS} && make -C src/realm librealm.a librealm-dbg.a
        INSTALL_COMMAND ""
        ${USES_TERMINAL_BUILD}
        )

    ExternalProject_Get_Property(realm-core SOURCE_DIR)
    define_built_realm_core_target(${SOURCE_DIR})
endfunction()

function(build_existing_realm_core core_directory)
    get_filename_component(core_directory ${core_directory} ABSOLUTE)
    ExternalProject_Add(realm-core
        URL ""
        PREFIX ${CMAKE_CURRENT_SOURCE_DIR}${CMAKE_FILES_DIRECTORY}/realm-core
        SOURCE_DIR ${core_directory}
        BUILD_IN_SOURCE 1
        BUILD_ALWAYS 1
        CONFIGURE_COMMAND ""
        BUILD_COMMAND ${EXPORT_MAKEFLAGS} && make -C src/realm librealm.a librealm-dbg.a
        INSTALL_COMMAND ""
        ${USES_TERMINAL_BUILD}
        )

    define_built_realm_core_target(${core_directory})
endfunction()
