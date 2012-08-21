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
#ifndef TIGHTDB_TABLE_MACROS_HPP
#define TIGHTDB_TABLE_MACROS_HPP

#include <tightdb/table_basic.hpp>


#define TIGHTDB_TABLE_1(Table, name1, type1) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        ColNames(Init i): name1(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1)); \
        } \
        void insert(std::size_t _i, type1 name1) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1)); \
        } \
        void set(std::size_t _i, type1 name1) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_2(Table, name1, type1, name2, type2) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        ColNames(Init i): name1(i), name2(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_3(Table, name1, type1, name2, type2, name3, type3) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        ColNames(Init i): name1(i), name2(i), name3(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_4(Table, name1, type1, name2, type2, name3, type3, name4, type4) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_5(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_6(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_7(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6, name7, type7) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns6; \
    typedef ::tightdb::TypeAppend< Columns6, type7 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        typename Col<6>::type name7; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i), name7(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6, #name7 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_8(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6, name7, type7, name8, type8) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns6; \
    typedef ::tightdb::TypeAppend< Columns6, type7 >::type Columns7; \
    typedef ::tightdb::TypeAppend< Columns7, type8 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        typename Col<6>::type name7; \
        typename Col<7>::type name8; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i), name7(i), name8(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6, #name7, #name8 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_9(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6, name7, type7, name8, type8, name9, type9) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns6; \
    typedef ::tightdb::TypeAppend< Columns6, type7 >::type Columns7; \
    typedef ::tightdb::TypeAppend< Columns7, type8 >::type Columns8; \
    typedef ::tightdb::TypeAppend< Columns8, type9 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        typename Col<6>::type name7; \
        typename Col<7>::type name8; \
        typename Col<8>::type name9; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i), name7(i), name8(i), name9(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6, #name7, #name8, #name9 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_10(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6, name7, type7, name8, type8, name9, type9, name10, type10) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns6; \
    typedef ::tightdb::TypeAppend< Columns6, type7 >::type Columns7; \
    typedef ::tightdb::TypeAppend< Columns7, type8 >::type Columns8; \
    typedef ::tightdb::TypeAppend< Columns8, type9 >::type Columns9; \
    typedef ::tightdb::TypeAppend< Columns9, type10 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        typename Col<6>::type name7; \
        typename Col<7>::type name8; \
        typename Col<8>::type name9; \
        typename Col<9>::type name10; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i), name7(i), name8(i), name9(i), name10(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6, #name7, #name8, #name9, #name10 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_11(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6, name7, type7, name8, type8, name9, type9, name10, type10, name11, type11) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns6; \
    typedef ::tightdb::TypeAppend< Columns6, type7 >::type Columns7; \
    typedef ::tightdb::TypeAppend< Columns7, type8 >::type Columns8; \
    typedef ::tightdb::TypeAppend< Columns8, type9 >::type Columns9; \
    typedef ::tightdb::TypeAppend< Columns9, type10 >::type Columns10; \
    typedef ::tightdb::TypeAppend< Columns10, type11 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        typename Col<6>::type name7; \
        typename Col<7>::type name8; \
        typename Col<8>::type name9; \
        typename Col<9>::type name10; \
        typename Col<10>::type name11; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i), name7(i), name8(i), name9(i), name10(i), name11(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6, #name7, #name8, #name9, #name10, #name11 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_12(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6, name7, type7, name8, type8, name9, type9, name10, type10, name11, type11, name12, type12) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns6; \
    typedef ::tightdb::TypeAppend< Columns6, type7 >::type Columns7; \
    typedef ::tightdb::TypeAppend< Columns7, type8 >::type Columns8; \
    typedef ::tightdb::TypeAppend< Columns8, type9 >::type Columns9; \
    typedef ::tightdb::TypeAppend< Columns9, type10 >::type Columns10; \
    typedef ::tightdb::TypeAppend< Columns10, type11 >::type Columns11; \
    typedef ::tightdb::TypeAppend< Columns11, type12 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        typename Col<6>::type name7; \
        typename Col<7>::type name8; \
        typename Col<8>::type name9; \
        typename Col<9>::type name10; \
        typename Col<10>::type name11; \
        typename Col<11>::type name12; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i), name7(i), name8(i), name9(i), name10(i), name11(i), name12(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6, #name7, #name8, #name9, #name10, #name11, #name12 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_13(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6, name7, type7, name8, type8, name9, type9, name10, type10, name11, type11, name12, type12, name13, type13) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns6; \
    typedef ::tightdb::TypeAppend< Columns6, type7 >::type Columns7; \
    typedef ::tightdb::TypeAppend< Columns7, type8 >::type Columns8; \
    typedef ::tightdb::TypeAppend< Columns8, type9 >::type Columns9; \
    typedef ::tightdb::TypeAppend< Columns9, type10 >::type Columns10; \
    typedef ::tightdb::TypeAppend< Columns10, type11 >::type Columns11; \
    typedef ::tightdb::TypeAppend< Columns11, type12 >::type Columns12; \
    typedef ::tightdb::TypeAppend< Columns12, type13 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        typename Col<6>::type name7; \
        typename Col<7>::type name8; \
        typename Col<8>::type name9; \
        typename Col<9>::type name10; \
        typename Col<10>::type name11; \
        typename Col<11>::type name12; \
        typename Col<12>::type name13; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i), name7(i), name8(i), name9(i), name10(i), name11(i), name12(i), name13(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6, #name7, #name8, #name9, #name10, #name11, #name12, #name13 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12, type13 name13) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12, name13)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12, type13 name13) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12, name13)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12, type13 name13) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12, name13)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_14(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6, name7, type7, name8, type8, name9, type9, name10, type10, name11, type11, name12, type12, name13, type13, name14, type14) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns6; \
    typedef ::tightdb::TypeAppend< Columns6, type7 >::type Columns7; \
    typedef ::tightdb::TypeAppend< Columns7, type8 >::type Columns8; \
    typedef ::tightdb::TypeAppend< Columns8, type9 >::type Columns9; \
    typedef ::tightdb::TypeAppend< Columns9, type10 >::type Columns10; \
    typedef ::tightdb::TypeAppend< Columns10, type11 >::type Columns11; \
    typedef ::tightdb::TypeAppend< Columns11, type12 >::type Columns12; \
    typedef ::tightdb::TypeAppend< Columns12, type13 >::type Columns13; \
    typedef ::tightdb::TypeAppend< Columns13, type14 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        typename Col<6>::type name7; \
        typename Col<7>::type name8; \
        typename Col<8>::type name9; \
        typename Col<9>::type name10; \
        typename Col<10>::type name11; \
        typename Col<11>::type name12; \
        typename Col<12>::type name13; \
        typename Col<13>::type name14; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i), name7(i), name8(i), name9(i), name10(i), name11(i), name12(i), name13(i), name14(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6, #name7, #name8, #name9, #name10, #name11, #name12, #name13, #name14 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12, type13 name13, type14 name14) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12, name13, name14)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12, type13 name13, type14 name14) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12, name13, name14)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12, type13 name13, type14 name14) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12, name13, name14)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#define TIGHTDB_TABLE_15(Table, name1, type1, name2, type2, name3, type3, name4, type4, name5, type5, name6, type6, name7, type7, name8, type8, name9, type9, name10, type10, name11, type11, name12, type12, name13, type13, name14, type14, name15, type15) \
struct Table##Spec: ::tightdb::SpecBase { \
    typedef ::tightdb::TypeAppend< void,     type1 >::type Columns1; \
    typedef ::tightdb::TypeAppend< Columns1, type2 >::type Columns2; \
    typedef ::tightdb::TypeAppend< Columns2, type3 >::type Columns3; \
    typedef ::tightdb::TypeAppend< Columns3, type4 >::type Columns4; \
    typedef ::tightdb::TypeAppend< Columns4, type5 >::type Columns5; \
    typedef ::tightdb::TypeAppend< Columns5, type6 >::type Columns6; \
    typedef ::tightdb::TypeAppend< Columns6, type7 >::type Columns7; \
    typedef ::tightdb::TypeAppend< Columns7, type8 >::type Columns8; \
    typedef ::tightdb::TypeAppend< Columns8, type9 >::type Columns9; \
    typedef ::tightdb::TypeAppend< Columns9, type10 >::type Columns10; \
    typedef ::tightdb::TypeAppend< Columns10, type11 >::type Columns11; \
    typedef ::tightdb::TypeAppend< Columns11, type12 >::type Columns12; \
    typedef ::tightdb::TypeAppend< Columns12, type13 >::type Columns13; \
    typedef ::tightdb::TypeAppend< Columns13, type14 >::type Columns14; \
    typedef ::tightdb::TypeAppend< Columns14, type15 >::type Columns; \
 \
    template<template<int> class Col, class Init> struct ColNames { \
        typename Col<0>::type name1; \
        typename Col<1>::type name2; \
        typename Col<2>::type name3; \
        typename Col<3>::type name4; \
        typename Col<4>::type name5; \
        typename Col<5>::type name6; \
        typename Col<6>::type name7; \
        typename Col<7>::type name8; \
        typename Col<8>::type name9; \
        typename Col<9>::type name10; \
        typename Col<10>::type name11; \
        typename Col<11>::type name12; \
        typename Col<12>::type name13; \
        typename Col<13>::type name14; \
        typename Col<14>::type name15; \
        ColNames(Init i): name1(i), name2(i), name3(i), name4(i), name5(i), name6(i), name7(i), name8(i), name9(i), name10(i), name11(i), name12(i), name13(i), name14(i), name15(i) {} \
    }; \
 \
    static const char* const* dyn_col_names() \
    { \
        static const char* names[] = { #name1, #name2, #name3, #name4, #name5, #name6, #name7, #name8, #name9, #name10, #name11, #name12, #name13, #name14, #name15 }; \
        return names; \
    } \
 \
    struct ConvenienceMethods { \
        void add(type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12, type13 name13, type14 name14, type15 name15) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->add((::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12, name13, name14, name15)); \
        } \
        void insert(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12, type13 name13, type14 name14, type15 name15) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->insert(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12, name13, name14, name15)); \
        } \
        void set(std::size_t _i, type1 name1, type2 name2, type3 name3, type4 name4, type5 name5, type6 name6, type7 name7, type8 name8, type9 name9, type10 name10, type11 name11, type12 name12, type13 name13, type14 name14, type15 name15) \
        { \
            ::tightdb::BasicTable<Table##Spec>* const t = \
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \
            t->set(_i, (::tightdb::tuple(), name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12, name13, name14, name15)); \
        } \
    }; \
}; \
typedef ::tightdb::BasicTable<Table##Spec> Table;


#endif // TIGHTDB_TABLE_MACROS_HPP
