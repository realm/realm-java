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
#ifndef TIGHTDB_TABLE_REF_HPP
#define TIGHTDB_TABLE_REF_HPP

#include <cstddef>
#include <algorithm>

#include "bind_ptr.hpp"

namespace tightdb {


template<class> class BasicTable;


/**
 * A "smart" reference to a table.
 *
 * This kind of table reference is often needed when working with
 * subtables. For example:
 *
 * \code{.cpp}
 *
 *   void func(Table& table)
 *   {
 *     Table& sub1 = *table.get_subtable(0,0); // INVALID! (sub1 becomes 'dangeling')
 *     TableRef sub2 = table.get_subtable(0,0); // Safe!
 *   }
 *
 * \endcode
 *
 * \note This class provides a lesser form of move semantics when the
 * compiler does not support C++11 rvalue references. See
 * MoveSemantics for details about how this works. In any case you may
 * use an unqualifed move() to move a value of this class.
 *
 * \note A top-level table (explicitely created or obtained from a
 * group) may not be destroyed until all "smart" table references
 * obtained from it, or from any of its subtables, are destroyed.
 */
template<class T> class BasicTableRef: private bind_ptr<T> {
public:
#ifdef TIGHTDB_HAVE_CXX11_CONSTEXPR
    constexpr BasicTableRef() {}
#else
    BasicTableRef() {}
#endif

#ifdef TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE

    // Copy construct
    BasicTableRef(const BasicTableRef& r): bind_ptr<T>(r) {}
    template<class U> BasicTableRef(const BasicTableRef<U>& r): bind_ptr<T>(r) {}

    // Copy assign
    BasicTableRef& operator=(const BasicTableRef&);
    template<class U> BasicTableRef& operator=(const BasicTableRef<U>&);

    // Move construct
    BasicTableRef(BasicTableRef&& r): bind_ptr<T>(std::move(r)) {}
    template<class U> BasicTableRef(BasicTableRef<U>&& r): bind_ptr<T>(std::move(r)) {}

    // Move assign
    BasicTableRef& operator=(BasicTableRef&&);
    template<class U> BasicTableRef& operator=(BasicTableRef<U>&&);

#else // !TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE

    // Copy construct
    BasicTableRef(const BasicTableRef& r): bind_ptr<T>(r) {}
    template<class U> BasicTableRef(BasicTableRef<U> r): bind_ptr<T>(move(r)) {}

    // Copy assign
    BasicTableRef& operator=(BasicTableRef);
    template<class U> BasicTableRef& operator=(BasicTableRef<U>);

#endif // !TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE

    // Replacement for std::move() in C++03
    friend BasicTableRef move(BasicTableRef& r) { return BasicTableRef(&r, move_tag()); }

    // Comparison
    template<class U> bool operator==(const BasicTableRef<U>&) const;
    template<class U> bool operator!=(const BasicTableRef<U>&) const;
    template<class U> bool operator<(const BasicTableRef<U>&) const;

    // Dereference
    using bind_ptr<T>::operator*;
    using bind_ptr<T>::operator->;

#ifdef TIGHTDB_HAVE_CXX11_EXPLICIT_CONV_OPERATORS
    using bind_ptr<T>::operator bool;
#else
    using bind_ptr<T>::operator typename bind_ptr<T>::unspecified_bool_type;
#endif

    void swap(BasicTableRef& r) { this->bind_ptr<T>::swap(r); }
    friend void swap(BasicTableRef& a, BasicTableRef& b) { a.swap(b); }

private:
    template<class> struct GetRowAccType { typedef void type; };
    template<class Spec> struct GetRowAccType<BasicTable<Spec> > {
        typedef typename BasicTable<Spec>::RowAccessor type;
    };
    template<class Spec> struct GetRowAccType<const BasicTable<Spec> > {
        typedef typename BasicTable<Spec>::ConstRowAccessor type;
    };
    typedef typename GetRowAccType<T>::type RowAccessor;

public:
    /**
     * Same as 'table[i]' where 'table' is the referenced table.
     */
    RowAccessor operator[](std::size_t i) const { return (*this->get())[i]; }

private:
    friend class ColumnSubtableParent;
    friend class Table;
    template<class> friend class BasicTable;
    template<class> friend class BasicTableRef;

    explicit BasicTableRef(T* t): bind_ptr<T>(t) {}

    typedef typename bind_ptr<T>::move_tag move_tag;
    BasicTableRef(BasicTableRef* r, move_tag): bind_ptr<T>(r, move_tag()) {}
};


class Table;
typedef BasicTableRef<Table> TableRef;
typedef BasicTableRef<const Table> ConstTableRef;


template<class C, class T, class U>
inline std::basic_ostream<C,T>& operator<<(std::basic_ostream<C,T>& out, const BasicTableRef<U>& p)
{
    out << static_cast<const void*>(&*p);
    return out;
}





// Implementation:

#ifdef TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE

template<class T> inline BasicTableRef<T>& BasicTableRef<T>::operator=(const BasicTableRef& r)
{
    this->bind_ptr<T>::operator=(r);
    return *this;
}

template<class T> template<class U>
inline BasicTableRef<T>& BasicTableRef<T>::operator=(const BasicTableRef<U>& r)
{
    this->bind_ptr<T>::operator=(r);
    return *this;
}

template<class T> inline BasicTableRef<T>& BasicTableRef<T>::operator=(BasicTableRef&& r)
{
    this->bind_ptr<T>::operator=(std::move(r));
    return *this;
}

template<class T> template<class U>
inline BasicTableRef<T>& BasicTableRef<T>::operator=(BasicTableRef<U>&& r)
{
    this->bind_ptr<T>::operator=(std::move(r));
    return *this;
}

#else // !TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE

template<class T> inline BasicTableRef<T>& BasicTableRef<T>::operator=(BasicTableRef r)
{
    this->bind_ptr<T>::operator=(move(static_cast<bind_ptr<T>&>(r)));
    return *this;
}

template<class T> template<class U>
inline BasicTableRef<T>& BasicTableRef<T>::operator=(BasicTableRef<U> r)
{
    this->bind_ptr<T>::operator=(move(static_cast<bind_ptr<U>&>(r)));
    return *this;
}

#endif // !TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE

template<class T> template<class U>
inline bool BasicTableRef<T>::operator==(const BasicTableRef<U>& r) const
{
    return this->bind_ptr<T>::operator==(r);
}

template<class T> template<class U>
inline bool BasicTableRef<T>::operator!=(const BasicTableRef<U>& r) const
{
    return this->bind_ptr<T>::operator!=(r);
}

template<class T> template<class U>
inline bool BasicTableRef<T>::operator<(const BasicTableRef<U>& r) const
{
    return this->bind_ptr<T>::operator<(r);
}


} // namespace tightdb

#endif // TIGHTDB_TABLE_REF_HPP
