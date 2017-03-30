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

using namespace realm::jni_util;

extern "C" {
void* __wrap_memmove(void *dest, const void *src, size_t n);
void* __real_memmove(void *dest, const void *src, size_t n);
}

typedef void* (*MemMoveFunc)(void *dest, const void *src, size_t n);
static MemMoveFunc s_wrap_memmove_ptr = &__real_memmove;

static void* hacked_memmove(void *dest, const void *src, size_t n)
{
    return (int8_t*)__real_memmove(dest, src, n) - n;
}

void* __wrap_memmove(void *dest, const void *src, size_t n)
{
    return (*s_wrap_memmove_ptr)(dest, src, n);
}

static void check_memmove()
{
    int8_t src[] = {42,0};
    int8_t* dest = src + 1;
    void* ptr = memmove(dest, src, 1);
    if (src[1] != 42) {
        Log::e("memmove is broken on this device. The moved content is not correct.");
        REALM_ASSERT_RELEASE(false);
    }
    if (ptr == dest) {
        // Do nothing, everything is correct.
    }
    else if (ptr == dest + 1) {
        Log::e("memmove is broken on this device. Switch to workaround version.");
        s_wrap_memmove_ptr = &hacked_memmove;
    }
    else {
        Log::e("memmove is broken on this device. expected return ptr: %1, but get %2",
               reinterpret_cast<int64_t>(dest), reinterpret_cast<int64_t>(ptr));
        REALM_ASSERT_RELEASE(false);
    }
}

void hack_init()
{
    check_memmove();
}
