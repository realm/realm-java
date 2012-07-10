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
#ifndef TIGHTDB_ERROR_HPP
#define TIGHTDB_ERROR_HPP

#include <stdexcept>

namespace tightdb {


enum error_code {
    ERROR_NONE = 0,

    /// An invalid argument was specified.
    ERROR_INVALID_ARG,

    /// A specified file system path (or the directory prefix of a
    /// specified file system path) was not found in the file system.
    ERROR_NO_SUCH_FILE,

    /// A specified file system path was found, but could not be
    /// resolved, or the file was of an unsupported type. This error
    /// type is not to be used for cases where a failure to access a
    /// path is due to lacking permissions or insufficient
    /// priviledges.
    ERROR_BAD_FILESYS_PATH,

    /// Lacking permissions or insufficient privileges.
    ERROR_PERMISSION,

    /// Insufficient memory.
    ERROR_OUT_OF_MEMORY,

    /// Insufficient resources (not including memory).
    ERROR_NO_RESOURCE,

    /// Input/output error.
    ERROR_IO,

    /// A blocking operation was interrupted for example by a handled
    /// system signal.
    ERROR_INTERRUPTED,

    /// A function was called, or a feature was requested, that was
    /// not implemented.
    ERROR_NOT_IMPLEMENTED,

    /// An error of unknown type, or one that is not covered by any of
    /// the preceeding error types.
    ERROR_OTHER
};


const char* get_message(error_code);


inline void throw_error(error_code err)
{
    // FIXME: Do we allow exceptions?
    // FIXME: Should throw TightdbException(err, get_message(err)) or maybe one type of exception per error type.
    throw std::runtime_error(get_message(err));
}


} // namespace tightdb

#endif // TIGHTDB_ERROR_HPP
