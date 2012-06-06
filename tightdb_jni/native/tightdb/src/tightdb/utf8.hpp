/*************************************************************************
 *
 * TIGHTDB CONFIDENTIAL
 * __________________
 *
 *  [2011] - [2012] TightDB Inc
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of TightDB Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to TightDB Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from TightDB Incorporated.
 *
 **************************************************************************/
#ifndef TIGHTDB_UTF8_HPP
#define TIGHTDB_UTF8_HPP

#include <string>
#include <memory.h>
#if defined(_WIN32) || defined(__WIN32__) || defined(_WIN64)
#include <Windows.h>
#endif

namespace tightdb {

bool case_cmp(const char* constant_upper, const char* constant_lower, const char* source);
bool case_strstr(const char* constant_upper, const char* constant_lower, const char* source);
bool utf8case(const char* source, char* destination, int upper);
size_t case_prefix(const char* constant_upper, const char* constant_lower, const char* source);
bool utf8case_single(const char** source, char** destination, int upper);
size_t sequence_length(const char* lead);
size_t comparechars(const char* c1, const char* c2);
bool utf8case_single(const char* source, char* destination, int upper);

} // namespace tightdb

#endif // TIGHTDB_UTF8_HPP
