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
#ifndef TIGHTDB_META_HPP
#define TIGHTDB_META_HPP

#include <climits>
#include <cwchar>
#include <limits>

#include <tightdb/config.h>

namespace tightdb {


/**
 * A ternary operator that selects the first type if the condition
 * evaluates to true, otherwise it selects the second type.
 */
template<bool cond, class A, class B> struct CondType   { typedef A type; };
template<class A, class B> struct CondType<false, A, B> { typedef B type; };

template<class A, class B> struct SameType { static bool const value = false; };
template<class A> struct SameType<A,A>     { static bool const value = true;  };

template<class T, class A, class B> struct EitherTypeIs { static const bool value = false; };
template<class T, class A> struct EitherTypeIs<T,T,A> { static const bool value = true; };
template<class T, class A> struct EitherTypeIs<T,A,T> { static const bool value = true; };
template<class T> struct EitherTypeIs<T,T,T> { static const bool value = true; };

template<class T> struct IsConst          { static const bool value = false; };
template<class T> struct IsConst<const T> { static const bool value = true;  };

template<class From, class To> struct CopyConstness                 { typedef       To type; };
template<class From, class To> struct CopyConstness<const From, To> { typedef const To type; };

template<class T> struct DerefType {};
template<class T> struct DerefType<T*> { typedef T type; };



/**
 * Determine the type resulting from integral promotion.
 *
 * \note Enum types are supported only when the compiler supports the
 * C++11 'decltype' feature.
 */
#ifdef TIGHTDB_HAVE_CXX11_DECLTYPE
template<class T> struct IntegralPromote { typedef decltype(+T()) type; };
#else // !TIGHTDB_HAVE_CXX11_DECLTYPE
template<class T> struct IntegralPromote;
template<> struct IntegralPromote<bool> { typedef int type; };
template<> struct IntegralPromote<char> {
    typedef CondType<INT_MIN <= CHAR_MIN && CHAR_MAX <= INT_MAX, int, unsigned>::type type;
};
template<> struct IntegralPromote<signed char> {
    typedef CondType<INT_MIN <= SCHAR_MIN && SCHAR_MAX <= INT_MAX, int, unsigned>::type type;
};
template<> struct IntegralPromote<unsigned char> {
    typedef CondType<UCHAR_MAX <= INT_MAX, int, unsigned>::type type;
};
template<> struct IntegralPromote<wchar_t> {
private:
    typedef CondType<LLONG_MIN <= WCHAR_MIN && WCHAR_MAX <= LLONG_MAX, long long, unsigned long long>::type type_1;
    typedef CondType<0 <= WCHAR_MIN && WCHAR_MAX <= ULONG_MAX, unsigned long, type_1>::type type_2;
    typedef CondType<LONG_MIN <= WCHAR_MIN && WCHAR_MAX <= LONG_MAX, long, type_2>::type type_3;
    typedef CondType<0 <= WCHAR_MIN && WCHAR_MAX <= UINT_MAX, unsigned, type_3>::type type_4;
public:
    typedef CondType<INT_MIN <= WCHAR_MIN && WCHAR_MAX <= INT_MAX, int, type_4>::type type;
};
template<> struct IntegralPromote<short> {
    typedef CondType<INT_MIN <= SHRT_MIN && SHRT_MAX <= INT_MAX, int, unsigned>::type type;
};
template<> struct IntegralPromote<unsigned short> {
    typedef CondType<USHRT_MAX <= INT_MAX, int, unsigned>::type type;
};
template<> struct IntegralPromote<int> { typedef int type; };
template<> struct IntegralPromote<unsigned> { typedef unsigned type; };
template<> struct IntegralPromote<long> { typedef long type; };
template<> struct IntegralPromote<unsigned long> { typedef unsigned long type; };
template<> struct IntegralPromote<long long> { typedef long long type; };
template<> struct IntegralPromote<unsigned long long> { typedef unsigned long long type; };
template<> struct IntegralPromote<float> { typedef float type; };
template<> struct IntegralPromote<double> { typedef double type; };
template<> struct IntegralPromote<long double> { typedef long double type; };
#endif // !TIGHTDB_HAVE_CXX11_DECLTYPE



/**
 * Determine type of the result of an arithmetic operation (+, -, *,
 * /, %, |, &, ^). The type of the result of a shift operation (<<,
 * >>) can instead be found as the type resulting from integral
 * promotion of the left operand.
 *
 * \note Enum types are supported only when the compiler supports the
 * C++11 'decltype' feature.
 */
#ifdef TIGHTDB_HAVE_CXX11_DECLTYPE
template<class A, class B> struct ArithBinOpType { typedef decltype(A()+B()) type; };
#else // !TIGHTDB_HAVE_CXX11_DECLTYPE
template<class A, class B> struct ArithBinOpType {
private:
    typedef typename IntegralPromote<A>::type A2;
    typedef typename IntegralPromote<B>::type B2;

    typedef typename CondType<UINT_MAX <= LONG_MAX, long, unsigned long>::type type_l_u;
    typedef typename CondType<EitherTypeIs<unsigned, A2, B2>::value, type_l_u, long>::type type_l;

    typedef typename CondType<UINT_MAX <= LLONG_MAX, long long, unsigned long long>::type type_ll_u;
    typedef typename CondType<ULONG_MAX <= LLONG_MAX, long long, unsigned long long>::type type_ll_ul;
    typedef typename CondType<EitherTypeIs<unsigned, A2, B2>::value, type_ll_u, long long>::type type_ll_1;
    typedef typename CondType<EitherTypeIs<unsigned long, A2, B2>::value, type_ll_ul, type_ll_1>::type type_ll;

    typedef typename CondType<EitherTypeIs<unsigned, A2, B2>::value, unsigned, int>::type type_1;
    typedef typename CondType<EitherTypeIs<long, A2, B2>::value, type_l, type_1>::type type_2;
    typedef typename CondType<EitherTypeIs<unsigned long, A2, B2>::value, unsigned long, type_2>::type type_3;
    typedef typename CondType<EitherTypeIs<long long, A2, B2>::value, type_ll, type_3>::type type_4;
    typedef typename CondType<EitherTypeIs<unsigned long long, A2, B2>::value, unsigned long long, type_4>::type type_5;
    typedef typename CondType<EitherTypeIs<float, A, B>::value, float, type_5>::type type_6;
    typedef typename CondType<EitherTypeIs<double, A, B>::value, double, type_6>::type type_7;

public:
    typedef typename CondType<EitherTypeIs<long double, A, B>::value, long double, type_7>::type type;
};
#endif // !TIGHTDB_HAVE_CXX11_DECLTYPE



template<class T> struct Wrap {
    Wrap(const T& v): m_value(v) {}
    operator T() const { return m_value; }
private:
    T m_value;
};



namespace _impl {
    template<class T, bool is_signed> struct IsNegative {
        static bool test(T value) { return value < 0; }
    };
    template<class T> struct IsNegative<T, false> {
        static bool test(T) { return false; }
    };
}

/// This function allows you to test for a negative value in any
/// numeric type. Normally, if the type is unsigned, such a test will
/// produce a compiler warning.
template<class T> inline bool is_negative(T value)
{
    return _impl::IsNegative<T, std::numeric_limits<T>::is_signed>::test(value);
}


} // namespace tightdb

#endif // TIGHTDB_META_HPP
