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
#ifndef TIGHTDB_COLUMN_TYPE_HPP
#define TIGHTDB_COLUMN_TYPE_HPP

#ifdef __cplusplus
#define TIGHTDB_QUAL_CC(name) name
#define TIGHTDB_QUAL_UC(name) name
#else
#define TIGHTDB_QUAL_CC(name) Tightdb##name
#define TIGHTDB_QUAL_UC(name) TIGHTDB_##name
#endif

#ifdef __cplusplus
namespace tightdb {
#endif

// Note: tightdb_objc/Deliv/ColumnType.h must be kept in sync with his file.
// Note: tightdb_java2/src/main/java/ColumnType.java must be kept in sync with his file.

enum TIGHTDB_QUAL_CC(ColumnType) {
    // Column types
    TIGHTDB_QUAL_UC(COLUMN_TYPE_INT)         =  0,
    TIGHTDB_QUAL_UC(COLUMN_TYPE_BOOL)        =  1,
    TIGHTDB_QUAL_UC(COLUMN_TYPE_STRING)      =  2,
    TIGHTDB_QUAL_UC(COLUMN_TYPE_STRING_ENUM) =  3, // double refs
    TIGHTDB_QUAL_UC(COLUMN_TYPE_BINARY)      =  4,
    TIGHTDB_QUAL_UC(COLUMN_TYPE_TABLE)       =  5,
    TIGHTDB_QUAL_UC(COLUMN_TYPE_MIXED)       =  6,
    TIGHTDB_QUAL_UC(COLUMN_TYPE_DATE)        =  7,
    TIGHTDB_QUAL_UC(COLUMN_TYPE_RESERVED1)   =  8, // DateTime
    TIGHTDB_QUAL_UC(COLUMN_TYPE_RESERVED2)   =  9, // Float
    TIGHTDB_QUAL_UC(COLUMN_TYPE_RESERVED3)   = 10, // Double
    TIGHTDB_QUAL_UC(COLUMN_TYPE_RESERVED4)   = 11, // Decimal

    // Attributes
    TIGHTDB_QUAL_UC(COLUMN_ATTR_INDEXED)     = 100,
    TIGHTDB_QUAL_UC(COLUMN_ATTR_UNIQUE)      = 101,
    TIGHTDB_QUAL_UC(COLUMN_ATTR_SORTED)      = 102,
    TIGHTDB_QUAL_UC(COLUMN_ATTR_NONE)        = 103
};


#ifdef __cplusplus
} // namespace tightdb
#endif

#endif // TIGHTDB_COLUMN_TYPE_HPP
