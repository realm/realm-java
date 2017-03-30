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

#include <string.h>

#include <realm/util/assert.hpp>

extern "C" {
void* __wrap_memmove(void *dest, const void *src, size_t n);
void* __real_memmove(void *dest, const void *src, size_t n);
}

typedef void* (*MemMoveFunc)(void *dest, const void *src, size_t n);
static MemMoveFunc s_wrap_memmove_ptr = &__real_memmove;

static inline void* hacked_memmove(void *dest, const void *src, size_t n)
{
    return (int8_t*)__real_memmove(dest, src, n) - n;
}

void* __wrap_memmove(void *dest, const void *src, size_t n)
{
    return (*s_wrap_memmove_ptr)(dest, src, n);
}

static void check_memmove()
{
    int8_t array[] = {42,0};
    void* ptr = memmove(array + 1, array, 1);
    if (array[1] != 42) {
        REALM_ASSERT_RELEASE(false);
    }
    if (ptr == array) {
        // Do nothing, everything is correct.
    }
    else if (ptr == array + 1) {
        s_wrap_memmove_ptr = &hacked_memmove;
    } else {
        REALM_ASSERT_RELEASE(false);
    }
}

void hack_init()
{
    check_memmove();
}
