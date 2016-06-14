option(SANITIZE_ADDRESS "build with ASan")
option(SANITIZE_THREAD "build with TSan")
option(SANITIZE_UNDEFINED "build with UBSan")

if(SANITIZE_ADDRESS)
    set(SANITIZER_FLAGS "${SANITIZER_FLAGS} -fsanitize=address")
endif()

if(SANITIZE_THREAD)
    set(SANITIZER_FLAGS "${SANITIZER_FLAGS} -fsanitize=thread")
endif()

if(SANITIZE_UNDEFINED)
    set(SANITIZER_FLAGS "${SANITIZER_FLAGS} -fsanitize=undefined")
endif()

if(SANITIZE_ADDRESS OR SANITIZE_THREAD OR SANITIZE_UNDEFINED)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${SANITIZER_FLAGS} -fno-omit-frame-pointer")
    set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} ${SANITIZER_FLAGS}")
endif()
