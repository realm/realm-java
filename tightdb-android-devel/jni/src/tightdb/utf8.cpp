#include <string>
#include <assert.h>
#include "utf8.hpp"

namespace tightdb {

// Return size in bytes of one utf8 character
size_t sequence_length(const char *lead)
{
    unsigned char lead2 = *lead;
    if (lead2 < 0x80)
        return 1;
    else if ((lead2 >> 5) == 0x6)
        return 2;
    else if ((lead2 >> 4) == 0xe)
        return 3;
    else if ((lead2 >> 3) == 0x1e)
        return 4;
    else
        return 0;
}


// assumes both chars have same lengths. Assumes both chars are in 0-terminated strings
size_t comparechars(const char* c1, const char* c2)
{
    size_t p = 0;
    do {
        if(c1[p] != c2[p])
            return 0;
        p++;
    } while((c1[p] & 0x80) == 0x80);

    return p;
}

// If constant == source, return 1.
// Else, if constant is a prefix of source, return 0
// Else return -1
size_t case_prefix(const char* constant_upper, const char* constant_lower, const char* source)
{
    size_t matchlen = 0;
    do {
        size_t m = comparechars(&constant_lower[matchlen], &source[matchlen]);
        if(m == 0)
            m = comparechars(&constant_upper[matchlen], &source[matchlen]);
        if(m != 0)
            matchlen += m;
        else
            return (size_t)-1;
    }
    while(constant_lower[matchlen] != 0 && source[matchlen] != 0);

    if(constant_lower[matchlen] == 0 && source[matchlen] != 0)
        return 0;
    else if (constant_lower[matchlen] == 0 && source[matchlen] == 0)
        return 1;

    return (size_t)-1;
}

// If constant == source, return true. NOTE: This function first performs a case insensitive *byte*
// compare instead of one whole UTF-8 character at a time. This is very fast, but enough to guarantee
// that the strings are identical, so we need a slower character compare later (we use case_prefix()
// for this).
bool case_cmp(const char* constant_upper, const char* constant_lower, const char *source)
{
    size_t matchlen = 0;
    do {
        if(constant_lower[matchlen] == source[matchlen] || constant_upper[matchlen] == source[matchlen])
            matchlen++;
        else
            return false;
    } while (constant_lower[matchlen] != 0 && source[matchlen] != 0);

    if(case_prefix(constant_upper, constant_lower, source) != (size_t)-1)
        return true;
    else
        return false;
}

// Test if constant is a substring of source
bool case_strstr(const char *constant_upper, const char *constant_lower, const char *source) {
    size_t source_pos = 0;
    do {
        if(case_cmp(constant_upper, constant_lower, source + source_pos))
            return true;
        source_pos++;
    } while(source[source_pos] != 0);

    return false;
}

// Converts a single utf8 character to upper or lower case. Operating system specific function.
bool utf8case_single(const char* source, char* destination, int upper)
{
#if (defined(_WIN32) || defined(__WIN32__) || defined(_WIN64))
    wchar_t tmp[2];

    int i = MultiByteToWideChar(CP_UTF8, 0, (LPCSTR)source, (int)sequence_length(source), &tmp[0], 1);
    if(i == 0)
        return false;

    tmp[1] = 0;

    if(upper)
        CharUpperW((LPWSTR)&tmp);
    else
        CharLowerW((LPWSTR)&tmp);

    i = WideCharToMultiByte(CP_UTF8, 0, (LPCWSTR)&tmp, 1, (LPSTR)destination, 6, 0, 0);
    if(i == 0)
        return false;

    return true;
#else
    memcpy(destination, source, sequence_length(source));
    (void)upper;
    return true;
#endif
}

// Converts utf8 source into upper or lower case. This function preserves the byte length of each utf8
// character in following way: If an output character differs in size, it is simply substituded by the
// original character. This may of course give wrong search results in very special cases. Todo.
bool utf8case(const char* source, char* destination, int upper)
{
    while(*source != 0) {
        if(sequence_length(source) == 0)
            return false;

        bool b = utf8case_single(source, destination, upper);
        if(!b) {
            return false;
        }

        if(sequence_length(destination) != sequence_length(source)) {
            memcpy(destination, source, sequence_length(source));
            destination += sequence_length(source);
        }
        else
            destination += sequence_length(destination);

        source += sequence_length(source);
    }

    *destination = 0;
    return true;
}

}
