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
#ifndef TIGHTDB_TYPE_LIST_HPP
#define TIGHTDB_TYPE_LIST_HPP

namespace tightdb {


/**
 * The 'cons' operator for building lists of types.
 *
 * \tparam H The head of the list, that is, the first type in the
 * list.
 *
 * \tparam T The tail of the list, that is, the list of types
 * following the head. It is 'void' if nothing follows the head,
 * otherwise it matches TypeCons<H2,T2>.
 *
 * Note that 'void' is interpreted as a zero-length list.
 */
template<class H, class T> struct TypeCons {
    typedef H head;
    typedef T tail;
};


/**
 * Append a type the the end of a type list. The resulting type list
 * is available as TypeAppend<List, T>::type.
 *
 * \tparam List A list of types constructed using TypeCons<>. Note
 * that 'void' is interpreted as a zero-length list.
 *
 * \tparam T The new type to be appended.
 */
template<class List, class T> struct TypeAppend {
    typedef TypeCons<typename List::head, typename TypeAppend<typename List::tail, T>::type> type;
};
template<class T> struct TypeAppend<void, T> {
    typedef TypeCons<T, void> type;
};


/**
 * Get an element from the specified list of types. The
 * result is available as TypeAt<List, i>::type.
 *
 * \tparam List A list of types constructed using TypeCons<>. Note
 * that 'void' is interpreted as a zero-length list.
 *
 * \tparam i The index of the list element to get.
 */
template<class List, int i> struct TypeAt {
    typedef typename TypeAt<typename List::tail, i-1>::type type;
};
template<class List> struct TypeAt<List, 0> { typedef typename List::head type; };


/**
 * Count the number of elements in the specified list of types. The
 * result is available as TypeCount<List>::value.
 *
 * \tparam List The list of types constructed using TypeCons<>. Note
 * that 'void' is interpreted as a zero-length list.
 */
template<class List> struct TypeCount {
    static const int value = 1 + TypeCount<typename List::tail>::value;
};
template<> struct TypeCount<void> { static const int value = 0; };


/**
 * Execute an action for each element in the specified list of types.
 *
 * \tparam List The list of types constructed using TypeCons<>. Note
 * that 'void' is interpreted as a zero-length list.
 */
template<class List, template<class T, int i> class Op, int i=0> struct ForEachType {
    static void exec()
    {
        Op<typename List::head, i>::exec();
        ForEachType<typename List::tail, Op, i+1>::exec();
    }
    template<class A> static void exec(const A& a)
    {
        Op<typename List::head, i>::exec(a);
        ForEachType<typename List::tail, Op, i+1>::exec(a);
    }
    template<class A, class B> static void exec(const A& a, const B& b)
    {
        Op<typename List::head, i>::exec(a,b);
        ForEachType<typename List::tail, Op, i+1>::exec(a,b);
    }
    template<class A, class B, class C> static void exec(const A& a, const B& b, const C& c)
    {
        Op<typename List::head, i>::exec(a,b,c);
        ForEachType<typename List::tail, Op, i+1>::exec(a,b,c);
    }
};
template<template<class T, int i> class Op, int i> struct ForEachType<void, Op, i> {
    static void exec() {}
    template<class A> static void exec(const A&) {}
    template<class A, class B> static void exec(const A&, const B&) {}
    template<class A, class B, class C> static void exec(const A&, const B&, const C&) {}
};


/**
 * Execute a predicate for each element in the specified list of
 * types, and return true if, and only if the predicate returns true
 * for at least one of those elements.
 *
 * \tparam List The list of types constructed using TypeCons<>. Note
 * that 'void' is interpreted as a zero-length list.
 */
template<class List, template<class T, int i> class Pred, int i=0> struct HasType {
    static bool exec()
    {
        return Pred<typename List::head, i>::exec() ||
            HasType<typename List::tail, Pred, i+1>::exec();
    }
    template<class A> static bool exec(const A& a)
    {
        return Pred<typename List::head, i>::exec(a) ||
            HasType<typename List::tail, Pred, i+1>::exec(a);
    }
    template<class A, class B> static bool exec(const A& a, const B& b)
    {
        return Pred<typename List::head, i>::exec(a,b) ||
            HasType<typename List::tail, Pred, i+1>::exec(a,b);
    }
    template<class A, class B, class C> static bool exec(const A& a, const B& b, const C& c)
    {
        return Pred<typename List::head, i>::exec(a,b,c) ||
            HasType<typename List::tail, Pred, i+1>::exec(a,b,c);
    }
};
template<template<class T, int i> class Pred, int i> struct HasType<void, Pred, i> {
    static bool exec() { return false; }
    template<class A> static bool exec(const A&) { return false; }
    template<class A, class B> static bool exec(const A&, const B&) { return false; }
    template<class A, class B, class C>
    static bool exec(const A&, const B&, const C&) { return false; }
};


} // namespace tightdb

#endif // TIGHTDB_TYPE_LIST_HPP
