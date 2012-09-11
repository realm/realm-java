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
#ifndef TIGHTDB_CONFIG_H
#define TIGHTDB_CONFIG_H


/* GCC defines __GXX_RTTI when '-fno-rtti' is not specified. The same
 * is true for Clang >= v3.0. Microsoft Visual C++ defines _CPPRTTI
 * when '/GR' is specified. */
#if defined __GXX_RTTI || defined _CPPRTTI
#define TIGHTDB_HAVE_RTTI 1
#endif


/* GCC defines __EXCEPTIONS when '-fno-exceptions' is not
 * specified. The same is true for Clang >= v3.0. Microsoft Visual C++
 * defines _CPPUNWIND when '/GX' is specified. */
#if defined __EXCEPTIONS || defined _CPPUNWIND
#define TIGHTDB_HAVE_EXCEPTIONS 1
#endif


/* This one works for both GCC and Clang, and of course any compiler
 * that fully supports C++11. */
#if defined __cplusplus && __cplusplus >= 201103 || \
    defined __GXX_EXPERIMENTAL_CXX0X__ && __GXX_EXPERIMENTAL_CXX0X__ && defined __GNUC__ && (__GNUC__ > 4 || __GNUC__ == 4 && __GNUC_MINOR__ >= 3)
#define TIGHTDB_HAVE_CXX11_STATIC_ASSERT 1
#define TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE 1
#define TIGHTDB_HAVE_CXX11_DECLTYPE 1
#endif


/* This one works for both GCC and Clang, and of course any compiler
 * that fully supports C++11. */
#if defined __cplusplus && __cplusplus >= 201103 || \
    defined __GXX_EXPERIMENTAL_CXX0X__ && __GXX_EXPERIMENTAL_CXX0X__ && defined __GNUC__ && (__GNUC__ > 4 || __GNUC__ == 4 && __GNUC_MINOR__ >= 4)
#define TIGHTDB_HAVE_CXX11_INITIALIZER_LISTS 1
#define TIGHTDB_HAVE_CXX11_ATOMIC 1
#endif


/* This one works for both GCC and Clang, and of course any compiler
 * that fully supports C++11. */
#if defined __cplusplus && __cplusplus >= 201103 || \
    defined __GXX_EXPERIMENTAL_CXX0X__ && __GXX_EXPERIMENTAL_CXX0X__ && defined __GNUC__ && (__GNUC__ > 4 || __GNUC__ == 4 && __GNUC_MINOR__ >= 5)
#define TIGHTDB_HAVE_CXX11_EXPLICIT_CONV_OPERATORS 1
#endif


/* This one works for both GCC and Clang, and of course any compiler
 * that fully supports C++11. */
#if defined __cplusplus && __cplusplus >= 201103 || \
    defined __GXX_EXPERIMENTAL_CXX0X__ && __GXX_EXPERIMENTAL_CXX0X__ && defined __GNUC__ && (__GNUC__ > 4 || __GNUC__ == 4 && __GNUC_MINOR__ >= 6)
#define TIGHTDB_HAVE_CXX11_CONSTEXPR 1
#endif


#endif /* TIGHTDB_CONFIG_H */
