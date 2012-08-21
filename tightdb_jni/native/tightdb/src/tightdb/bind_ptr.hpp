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
#ifndef TIGHTDB_BIND_PTR_HPP
#define TIGHTDB_BIND_PTR_HPP

#include <algorithm>
#include <ostream>

#include <tightdb/config.h>

#ifdef TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE
#include <utility>
#endif

#ifdef TIGHTDB_HAVE_CXX11_ATOMIC
#include <atomic>
#endif


namespace tightdb {

    /**
     * A generic intrusive smart pointer that binds itself explicitely
     * to the target object.
     *
     * This class is agnostic towards what 'binding' means for the
     * target object, but a common use is 'reference counting'. See
     * RefCountBase for an example of that.
     *
     * \note This class provides a lesser form of move semantics when
     * the compiler does not support C++11 rvalue references. See
     * MoveSemantics for details about how this works. In any case you
     * may use an unqualifed move() to move a value of this class.
     */
    template<class T> class bind_ptr {
    public:
#ifdef TIGHTDB_HAVE_CXX11_CONSTEXPR
        constexpr bind_ptr(): m_ptr(0) {}
#else
        bind_ptr(): m_ptr(0) {}
#endif
        explicit bind_ptr(T* p) { bind(p); }
        template<class U> explicit bind_ptr(U* p) { bind(p); }
        ~bind_ptr() { unbind(); }

#ifdef TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE

        // Copy construct
        bind_ptr(const bind_ptr& p) { bind(p.m_ptr); }
        template<class U> bind_ptr(const bind_ptr<U>& p) { bind(p.m_ptr); }

        // Copy assign
        bind_ptr& operator=(const bind_ptr& p) { bind_ptr(p).swap(*this); return *this; }
        template<class U> bind_ptr& operator=(const bind_ptr<U>& p) { bind_ptr(p).swap(*this); return *this; }

        // Move construct
        bind_ptr(bind_ptr&& p): m_ptr(p.release()) {}
        template<class U> bind_ptr(bind_ptr<U>&& p): m_ptr(p.release()) {}

        // Move assign
        bind_ptr& operator=(bind_ptr&& p) { bind_ptr(std::move(p)).swap(*this); return *this; }
        template<class U> bind_ptr& operator=(bind_ptr<U>&& p) { bind_ptr(std::move(p)).swap(*this); return *this; }

#else // !TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE

        // Copy construct
        bind_ptr(const bind_ptr& p) { bind(p.m_ptr); }
        template<class U> bind_ptr(bind_ptr<U> p): m_ptr(p.release()) {}

        // Copy assign
        bind_ptr& operator=(bind_ptr p) { p.swap(*this); return *this; }
        template<class U> bind_ptr& operator=(bind_ptr<U> p) { bind_ptr(move(p)).swap(*this); return *this; }

#endif // !TIGHTDB_HAVE_CXX11_RVALUE_REFERENCE

        // Replacement for std::move() in C++03
        friend bind_ptr move(bind_ptr& p) { return bind_ptr(&p, move_tag()); }

        // Comparison
        template<class U> bool operator==(const bind_ptr<U>& p) const { return m_ptr == p.m_ptr; }
        template<class U> bool operator!=(const bind_ptr<U>& p) const { return m_ptr != p.m_ptr; }
        template<class U> bool operator<(const bind_ptr<U>& p) const { return m_ptr < p.m_ptr; }

        // Dereference
        T& operator*() const { return *m_ptr; }
        T* operator->() const { return m_ptr; }

#ifdef TIGHTDB_HAVE_CXX11_EXPLICIT_CONV_OPERATORS
        explicit operator bool() const { return m_ptr; }
#else
        typedef T* bind_ptr::*unspecified_bool_type;
        operator unspecified_bool_type() const { return m_ptr ? &bind_ptr::m_ptr : 0; }
#endif

        T* get() const { return m_ptr; }
        void reset() { bind_ptr().swap(*this); }
        void reset(T* p) { bind_ptr(p).swap(*this); }
        template<class U> void reset(U* p) { bind_ptr(p).swap(*this); }

        void swap(bind_ptr& p) { std::swap(m_ptr, p.m_ptr); }
        friend void swap(bind_ptr& a, bind_ptr& b) { a.swap(b); }

    protected:
        struct move_tag {};
        bind_ptr(bind_ptr* p, move_tag): m_ptr(p->release()) {}

        struct casting_move_tag {};
        template<class U> bind_ptr(bind_ptr<U>* p, casting_move_tag):
            m_ptr(static_cast<T*>(p->release())) {}

    private:
        T* m_ptr;

        void bind(T* p) { if (p) p->bind_ref(); m_ptr = p; }
        void unbind() { if (m_ptr) m_ptr->unbind_ref(); }

        T* release() { T* const p = m_ptr; m_ptr = 0; return p; }

        template<class> friend class bind_ptr;
    };


    template<class C, class T, class U>
    inline std::basic_ostream<C,T>& operator<<(std::basic_ostream<C,T>& out, const bind_ptr<U>& p)
    {
        out << static_cast<const void*>(p.get());
        return out;
    }




#ifdef TIGHTDB_HAVE_CXX11_ATOMIC
    /**
     * Polymorphic convenience base class for reference counting
     * objects.
     *
     * Together with bind_ptr, this class delivers simple instrusive
     * thread-safe reference counting.
     *
     * \sa bind_ptr
     */
    class RefCountBase {
    public:
        RefCountBase(): m_ref_count(0) {}
        virtual ~RefCountBase() {}

    private:
        mutable std::atomic<unsigned long> m_ref_count;

        // FIXME: Operators ++ and -- as used below use
        // std::memory_order_seq_cst. I'm not sure whether it is the
        // most effecient choice, that also guarantees safety.
        void bind_ref() const { ++m_ref_count; }
        void unbind_ref() const { if (--m_ref_count == 0) delete this; }

        template<class> friend class bind_ptr;
    };
#endif // TIGHTDB_HAVE_CXX11_ATOMIC


} // namespace tightdb

#endif // TIGHTDB_BIND_PTR_HPP
