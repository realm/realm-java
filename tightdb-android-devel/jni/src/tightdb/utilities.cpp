
#include "utilities.hpp"
#include <string>
#include <assert.h>
#include <cstdlib> // size_t
#ifndef _MSC_VER
#include <stdint.h>
#else
#include <win32/stdint.h>
#endif

namespace tightdb {

size_t TO_REF(int64_t v)
{
#if !defined(NDEBUG) && defined(_DEBUG)
    uint64_t m = (size_t)(-1);
    assert((uint64_t)v <= m);
#endif
    return (size_t)v;
}

void* round_up(void* p, size_t align)
{
    size_t r = ((size_t)p % align == 0 ? 0 : align - (size_t)p % align);
    return (char *)p + r;
}

void* round_down(void* p, size_t align)
{
    size_t r = (size_t)p;
    return (void *)(r & (~(align - 1)));
}

size_t round_up(size_t p, size_t align)
{
    size_t r = ((size_t)p % align == 0 ? 0 : align - (size_t)p % align);
    return p + r;
}

size_t round_down(size_t p, size_t align)
{
    size_t r = (size_t)p;
    return r & (~(align - 1));
}


void checksum_init(checksum_t* t)
{
    t->remainder = 0;
    t->remainder_len = 0;
    t->b_val = 0x794e80091e8f2bc7ULL;
    t->a_val = 0xc20f9a8b761b7e4cULL;
    t->result = 0;
}

unsigned long long checksum(unsigned char* data, size_t len)
{
    checksum_t t;
    checksum_init(&t);
    checksum_rolling(data, len, &t);
    return t.result;
}

void checksum_rolling(unsigned char* data, size_t len, checksum_t* t)
{
    while(t->remainder_len < 8 && len > 0)
    {
        t->remainder = t->remainder >> 8;
        t->remainder = t->remainder | (unsigned long long)*data << (7*8);
        t->remainder_len++;
        data++;
        len--;
    }

    if(t->remainder_len < 8)
    {
        t->result = t->a_val + t->b_val;
        return;
    }

    t->a_val += t->remainder * t->b_val;
    t->b_val++;
    t->remainder_len = 0;
    t->remainder = 0;

    while(len >= 8)
    {
#ifdef X86X64
        t->a_val += (*(unsigned long long *)data) * t->b_val;
#else
        unsigned long long l = 0;
        for(unsigned int i = 0; i < 8; i++)
        {
            l = l >> 8;
            l = l | (unsigned long long)*(data + i) << (7*8);
        }
        t->a_val += l * t->b_val;
#endif
        t->b_val++;
        len -= 8;
        data += 8;
    }

    while(len > 0)
    {
        t->remainder = t->remainder >> 8;
        t->remainder = t->remainder | (unsigned long long)*data << (7*8);
        t->remainder_len++;
        data++;
        len--;
    }

    t->result = t->a_val + t->b_val;
    return;
}

}
