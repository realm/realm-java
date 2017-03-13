/*
 * Copyright 2011 Realm Inc.
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

#ifndef REALM_UTIL_UTF8_HPP
#define REALM_UTIL_UTF8_HPP

#include <stdint.h>
#include <string>

#include <realm/util/safe_int_ops.hpp>
#include <realm/string_data.hpp>
#include <realm/util/features.h>
#include <realm/utilities.hpp>

namespace realm {
namespace util {


/// Transcode between UTF-8 and UTF-16.
///
/// \tparam Char16 Must be an integral type with at least 16 bits.
///
/// \tparam Traits16 Must define to_int_type() and to_char_type() for
/// \a Char16.
template <class Char16, class Traits16 = std::char_traits<Char16>>
struct Utf8x16 {
    /// Transcode as much as possible of the specified UTF-8 input, to
    /// UTF-16. Returns true if all input characters were transcoded, or
    /// transcoding stopped because the next character did not fit into the
    /// output buffer. Returns false if transcoding stopped due to invalid
    /// input. It is not specified whether this function returns true or false
    /// if invalid input occurs at the same time as the output buffer runs
    /// full. In any case, upon return, \a in_begin and \a out_begin are
    /// advanced to the position where transcoding stopped.
    ///
    /// Throws only if Traits16::to_char_type() throws.
    static size_t to_utf16(const char*& in_begin, const char* in_end, Char16*& out_begin, Char16* out_end);

    /// Same as to_utf16(), but in reverse.
    ///
    /// Throws only if Traits16::to_int_type() throws.
    static size_t to_utf8(const Char16*& in_begin, const Char16* in_end, char*& out_begin, char* out_end,
                          size_t& error_code);

    /// Summarize the number of UTF-16 elements needed to hold the result of
    /// transcoding the specified UTF-8 string. Upon return, if \a in_begin !=
    /// \a in_end, then the summation stopped due to invalid UTF-8 input. The
    /// returned size then reflects the number of UTF-16 elements needed to hold
    /// the result of transcoding the part of the input that was examined. This
    /// function will only detect a few UTF-8 validity issues, and can therefore
    /// not be used for general UTF-8 validation.
    static std::size_t find_utf16_buf_size(const char*& in_begin, const char* in_end, size_t& error_code);

    /// Summarize the number of UTF-8 bytes needed to hold the result of
    /// transcoding the specified UTF-16 string. Upon return, if \a in_begin !=
    /// \a in_end, then the summation stopped due to invalid UTF-16 input, or to
    /// prevent the returned \c size_t value from overflowing. The returned size
    /// then reflects the number of UTF-8 bytes needed to hold the result of
    /// transcoding the part of the input that was examined. This function will
    /// only detect a few UTF-16 validity issues, and can therefore not be used
    /// for general UTF-16 validation.
    static std::size_t find_utf8_buf_size(const Char16*& in_begin, const Char16* in_end, size_t& error_code);
};


// Implementation:

// Adapted from reference implementation.
// http://www.unicode.org/resources/utf8.html
// http://www.bsdua.org/files/unicode.tar.gz
template <class Char16, class Traits16>
inline size_t Utf8x16<Char16, Traits16>::to_utf16(const char*& in_begin, const char* in_end, Char16*& out_begin,
                                                  Char16* out_end)
{
    using namespace std;
    typedef char_traits<char> traits8;
    size_t invalid = 0;
    const char* in = in_begin;
    Char16* out = out_begin;
    while (in != in_end) {
        if (REALM_UNLIKELY(out == out_end)) {
            break; // Need space in output buffer
        }
        uint_fast16_t v1 = uint_fast16_t(traits8::to_int_type(in[0]));
        if (REALM_LIKELY(v1 < 0x80)) { // One byte
            // UTF-8 layout: 0xxxxxxx
            *out++ = Traits16::to_char_type(v1);
            in += 1;
            continue;
        }
        if (REALM_UNLIKELY(v1 < 0xC0)) {
            invalid = true;
            break; // Invalid first byte of UTF-8 sequence
        }
        if (REALM_LIKELY(v1 < 0xE0)) { // Two bytes
            if (REALM_UNLIKELY(in_end - in < 2)) {
                invalid = 1;
                break; // Incomplete UTF-8 sequence
            }
            uint_fast16_t v2 = uint_fast16_t(traits8::to_int_type(in[1]));
            // UTF-8 layout: 110xxxxx 10xxxxxx
            if (REALM_UNLIKELY((v2 & 0xC0) != 0x80)) {
                invalid = 2;
                break; // Invalid continuation byte
            }
            uint_fast16_t v = uint_fast16_t(((v1 & 0x1F) << 6) | ((v2 & 0x3F) << 0));
            if (REALM_UNLIKELY(v < 0x80)) {
                invalid = 3;
                break; // Overlong encoding is invalid
            }
            *out++ = Traits16::to_char_type(v);
            in += 2;
            continue;
        }
        if (REALM_LIKELY(v1 < 0xF0)) { // Three bytes
            if (REALM_UNLIKELY(in_end - in < 3)) {
                invalid = 4;
                break; // Incomplete UTF-8 sequence
            }
            uint_fast16_t v2 = uint_fast16_t(traits8::to_int_type(in[1]));
            uint_fast16_t v3 = uint_fast16_t(traits8::to_int_type(in[2]));
            // UTF-8 layout: 1110xxxx 10xxxxxx 10xxxxxx
            if (REALM_UNLIKELY((v2 & 0xC0) != 0x80 || (v3 & 0xC0) != 0x80)) {
                invalid = true;
                break; // Invalid continuation byte
            }
            uint_fast16_t v = uint_fast16_t(((v1 & 0x0F) << 12) | ((v2 & 0x3F) << 6) | ((v3 & 0x3F) << 0));
            if (REALM_UNLIKELY(v < 0x800)) {
                invalid = 5;
                break; // Overlong encoding is invalid
            }
            if (REALM_UNLIKELY(0xD800 <= v && v < 0xE000)) {
                invalid = 6;
                break; // Illegal code point range (reserved for UTF-16 surrogate pairs)
            }
            *out++ = Traits16::to_char_type(v);
            in += 3;
            continue;
        }
        if (REALM_UNLIKELY(out + 1 == out_end)) {
            break; // Need space in output buffer for surrogate pair
        }
        if (REALM_LIKELY(v1 < 0xF8)) { // Four bytes
            if (REALM_UNLIKELY(in_end - in < 4)) {
                invalid = 7;
                break; // Incomplete UTF-8 sequence
            }
            uint_fast32_t w1 = uint_fast32_t(v1);                          // 16 bit -> 32 bit
            uint_fast32_t v2 = uint_fast32_t(traits8::to_int_type(in[1])); // 32 bit intended
            uint_fast16_t v3 = uint_fast16_t(traits8::to_int_type(in[2])); // 16 bit intended
            uint_fast16_t v4 = uint_fast16_t(traits8::to_int_type(in[3])); // 16 bit intended
            // UTF-8 layout: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            if (REALM_UNLIKELY((v2 & 0xC0) != 0x80 || (v3 & 0xC0) != 0x80 || (v4 & 0xC0) != 0x80)) {
                invalid = 8;
                break; // Invalid continuation byte
            }
            uint_fast32_t v = uint_fast32_t(((w1 & 0x07) << 18) | // Parenthesis is 32 bit partial result
                                            ((v2 & 0x3F) << 12) | // Parenthesis is 32 bit partial result
                                            ((v3 & 0x3F) << 6) |  // Parenthesis is 16 bit partial result
                                            ((v4 & 0x3F) << 0));  // Parenthesis is 16 bit partial result
            if (REALM_UNLIKELY(v < 0x10000)) {
                invalid = 9;
                break; // Overlong encoding is invalid
            }
            if (REALM_UNLIKELY(0x110000 <= v)) {
                invalid = 19;
                break; // Code point too big for UTF-16
            }
            v -= 0x10000l;
            *out++ = Traits16::to_char_type(0xD800 + (v / 0x400));
            *out++ = Traits16::to_char_type(0xDC00 + (v % 0x400));
            in += 4;
            continue;
        }
        // Invalid first byte of UTF-8 sequence, or code point too big for UTF-16
        invalid = 11;
        break;
    }

    in_begin = in;
    out_begin = out;
    return invalid;
}


template <class Char16, class Traits16>
inline std::size_t Utf8x16<Char16, Traits16>::find_utf16_buf_size(const char*& in_begin, const char* in_end,
                                                                  size_t& error_code)
{
    using namespace std;
    typedef char_traits<char> traits8;
    size_t num_out = 0;
    error_code = 0;
    const char* in = in_begin;
    while (in != in_end) {
        uint_fast16_t v1 = uint_fast16_t(traits8::to_int_type(in[0]));
        if (REALM_LIKELY(v1 < 0x80)) { // One byte
            num_out += 1;
            in += 1;
            continue;
        }
        if (REALM_UNLIKELY(v1 < 0xC0)) {
            error_code = 1;
            break; // Invalid first byte of UTF-8 sequence
        }
        if (REALM_LIKELY(v1 < 0xE0)) { // Two bytes
            if (REALM_UNLIKELY(in_end - in < 2)) {
                error_code = 2;
                break; // Incomplete UTF-8 sequence
            }
            num_out += 1;
            in += 2;
            continue;
        }
        if (REALM_LIKELY(v1 < 0xF0)) { // Three bytes
            if (REALM_UNLIKELY(in_end - in < 3)) {
                error_code = 3;
                break; // Incomplete UTF-8 sequence
            }
            num_out += 1;
            in += 3;
            continue;
        }
        if (REALM_LIKELY(v1 < 0xF8)) { // Four bytes
            if (REALM_UNLIKELY(in_end - in < 4)) {
                error_code = 4;
                break; // Incomplete UTF-8 sequence
            }
            num_out += 2; // Surrogate pair
            in += 4;
            continue;
        }
        // Invalid first byte of UTF-8 sequence, or code point too big for UTF-16
        error_code = 5;
        break;
    }

    in_begin = in;
    return num_out;
}


// Adapted from reference implementation.
// http://www.unicode.org/resources/utf8.html
// http://www.bsdua.org/files/unicode.tar.gz
template <class Char16, class Traits16>
inline size_t Utf8x16<Char16, Traits16>::to_utf8(const Char16*& in_begin, const Char16* in_end, char*& out_begin,
                                                 char* out_end, size_t& error_code)
{
    using namespace std;
    typedef char_traits<char> traits8;
    typedef typename traits8::int_type traits8_int_type;
    bool invalid = false;
    error_code = 0;
    const Char16* in = in_begin;
    char* out = out_begin;
    while (in != in_end) {
        uint_fast16_t v1 = uint_fast16_t(Traits16::to_int_type(in[0]));
        if (REALM_LIKELY(v1 < 0x80)) {
            if (REALM_UNLIKELY(out == out_end)) {
                error_code = 1;
                break; // Not enough output buffer space
            }
            // UTF-8 layout: 0xxxxxxx
            *out++ = traits8::to_char_type(traits8_int_type(v1));
            in += 1;
            continue;
        }
        if (REALM_LIKELY(v1 < 0x800)) {
            if (REALM_UNLIKELY(out_end - out < 2)) {
                error_code = 2;
                break; // Not enough output buffer space
            }
            // UTF-8 layout: 110xxxxx 10xxxxxx
            *out++ = traits8::to_char_type(traits8_int_type(0xC0 + v1 / 0x40));
            *out++ = traits8::to_char_type(traits8_int_type(0x80 + v1 % 0x40));
            in += 1;
            continue;
        }
        if (REALM_LIKELY(v1 < 0xD800 || 0xE000 <= v1)) {
            if (REALM_UNLIKELY(out_end - out < 3)) {
                error_code = 3;
                break; // Not enough output buffer space
            }
            // UTF-8 layout: 1110xxxx 10xxxxxx 10xxxxxx
            *out++ = traits8::to_char_type(traits8_int_type(0xE0 + v1 / 0x1000));
            *out++ = traits8::to_char_type(traits8_int_type(0x80 + v1 / 0x40 % 0x40));
            *out++ = traits8::to_char_type(traits8_int_type(0x80 + v1 % 0x40));
            in += 1;
            continue;
        }

        // Surrogate pair
        if (REALM_UNLIKELY(out_end - out < 4)) {
            error_code = 4;
            break; // Not enough output buffer space
        }
        if (REALM_UNLIKELY(0xDC00 <= v1)) {
            error_code = 5;
            invalid = true;
            break; // Invalid first half of surrogate pair
        }
        if (REALM_UNLIKELY(in + 1 == in_end)) {
            error_code = 6;
            invalid = true;
            break; // Incomplete surrogate pair
        }
        uint_fast16_t v2 = uint_fast16_t(Traits16::to_int_type(in[1]));
        if (REALM_UNLIKELY(v2 < 0xDC00 || 0xE000 <= v2)) {
            error_code = 7;
            invalid = true;
            break; // Invalid second half of surrogate pair
        }
        uint_fast32_t v = 0x10000l + (uint_fast32_t(v1 - 0xD800) * 0x400 + (v2 - 0xDC00));
        // UTF-8 layout: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        *out++ = traits8::to_char_type(traits8_int_type(0xF0 + v / 0x40000));
        *out++ = traits8::to_char_type(traits8_int_type(0x80 + v / 0x1000 % 0x40));
        *out++ = traits8::to_char_type(traits8_int_type(0x80 + v / 0x40 % 0x40));
        *out++ = traits8::to_char_type(traits8_int_type(0x80 + v % 0x40));
        in += 2;
    }

    in_begin = in;
    out_begin = out;
    return !invalid;
}


template <class Char16, class Traits16>
inline std::size_t Utf8x16<Char16, Traits16>::find_utf8_buf_size(const Char16*& in_begin, const Char16* in_end,
                                                                 size_t& error_code)
{
    using namespace std;
    size_t num_out = 0;
    error_code = 0;
    const Char16* in = in_begin;
    while (in != in_end) {
        uint_fast16_t v = uint_fast16_t(Traits16::to_int_type(in[0]));
        if (REALM_LIKELY(v < 0x80)) {
            if (REALM_UNLIKELY(int_add_with_overflow_detect(num_out, 1))) {
                error_code = 1;
                break; // Avoid overflow
            }
            in += 1;
        }
        else if (REALM_LIKELY(v < 0x800)) {
            if (REALM_UNLIKELY(int_add_with_overflow_detect(num_out, 2))) {
                error_code = 2;
                break; // Avoid overflow
            }
            in += 1;
        }
        else if (REALM_LIKELY(v < 0xD800 || 0xE000 <= v)) {
            if (REALM_UNLIKELY(int_add_with_overflow_detect(num_out, 3))) {
                error_code = 3;
                break; // Avoid overflow
            }
            in += 1;
        }
        else {
            if (REALM_UNLIKELY(in + 1 == in_end)) {
                error_code = 4;
                break; // Incomplete surrogate pair
            }
            if (REALM_UNLIKELY(int_add_with_overflow_detect(num_out, 4))) {
                error_code = 5;
                break; // Avoid overflow
            }
            in += 2;
        }
    }

    in_begin = in;
    return num_out;
}
} // namespace util
} // namespace realm

#endif // REALM_UTIL_UTF8_HPP
