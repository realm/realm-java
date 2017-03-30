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
void* __builtin_memmove(void *dest, const void *src, size_t n);
}

typedef void* (*MemMoveFunc)(void *dest, const void *src, size_t n);
static MemMoveFunc s_wrap_memmove_ptr = &__real_memmove;

static void* hacked_memmove(void *dest, const void *src, size_t count)
{
    uint8_t* tmp;
    const uint8_t* s;

    if (dest <= src) {
		tmp = (uint8_t*)dest;
		s = (uint8_t*)src;
		while (count--)
			*tmp++ = *s++;
	} else {
		tmp = (uint8_t*)dest;
		tmp += count;
		s = (uint8_t*)src;
		s += count;
		while (count--)
			*--tmp = *--s;
	}
	return dest;
}

void* __wrap_memmove(void *dest, const void *src, size_t n)
{
    return (*s_wrap_memmove_ptr)(dest, src, n);
}

static void check_memmove()
{
    char *array = strdup("Foobar");
    void *ptr = __real_memmove(array + 1, array, sizeof("Foobar") - 2);
    if (ptr != array + 1) {
        Log::e("memmove is broken on this device. switch to the builtin function.");
        s_wrap_memmove_ptr = &hacked_memmove;
    }
    else {
        Log::e("memmove is not broken on this device - lucky you.");
    }
}

void hack_init()
{
    check_memmove();
}
