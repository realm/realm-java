/*
 * Copyright 2017 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "hack.hpp"
#include "log.hpp"

#include <string.h>

#include <realm/util/assert.hpp>

#ifndef REALM_WRAP_MEMMOVE
#error "REALM_WRAP_MEMMOVE is not defined!"
#endif

#if REALM_WRAP_MEMMOVE
extern "C" {
void* __wrap_memmove(void *dest, const void *src, size_t n);
void* __real_memmove(void *dest, const void *src, size_t n);

void* __wrap_memcpy(void *dest, const void *src, size_t n);
void* __real_memcpy(void *dest, const void *src, size_t n);
}

using namespace realm::jni_util;

typedef void* (*MemMoveFunc)(void *dest, const void *src, size_t n);
static MemMoveFunc s_wrap_memmove_ptr = &__real_memmove;
static MemMoveFunc s_wrap_memcpy_ptr = &__real_memcpy;

static void* hacked_memmove(void* s1, const void* s2, size_t n)
{
    // adapted from https://github.com/dryc/libc11/blob/master/src/string/memmove.c
    char* dest = (char*)s1;
    const char* src = (const char*)s2;
    if (dest <= src) {
        while (n--) {
            *dest++ = *src++;
        }
    }
    else {
        src += n;
        dest += n;
        while (n--) {
            *--dest = *--src;
        }
    }
    return static_cast<void*>(s1);
}

static void* hacked_memcpy(void* s1, const void* s2, size_t n)
{
    // adapted from https://github.com/dryc/libc11/blob/master/src/string/memcpy.c
    char* dest = (char*)s1;
    const char* src = (const char*)s2;
    while (n--) {
        *dest++ = *src++;
    }
    return static_cast<void*>(s1);
}

void* __wrap_memmove(void *dest, const void *src, size_t n)
{
    return (*s_wrap_memmove_ptr)(dest, src, n);
}

void* __wrap_memcpy(void *dest, const void *src, size_t n)
{
    return (*s_wrap_memcpy_ptr)(dest, src, n);
}


// See https://github.com/realm/realm-java/issues/3651#issuecomment-290290228
// There is a bug in memmove for some Samsung devices which will return "dest-n" instead of dest.
// The bug was originally found by QT, see https://bugreports.qt.io/browse/QTBUG-34984 .
// To work around it, we use linker's wrap feature to use a pure C implementation of memmove if the device has the
// problem.
static void check_memmove()
{
    char* array = strdup("Foobar");
    size_t len = strlen(array);
    void* ptr = __real_memmove(array + 1, array, len - 1);
    if (ptr != array + 1 || strncmp(array, "FFooba", len) != 0) {
        Log::e("memmove is broken on this device. Switching to the builtin implementation.");
        s_wrap_memmove_ptr = &hacked_memmove;
        s_wrap_memcpy_ptr  = &hacked_memcpy;
    }
    free(array);
}
#endif

namespace realm {
namespace jni_util {

void hack_init()
{
#if REALM_WRAP_MEMMOVE
    check_memmove();
#endif
}

}
}

