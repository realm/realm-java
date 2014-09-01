#ifndef __SUPPORT_MEM__
#define __SUPPORT_MEM__

#include <cstdlib> // size_t

/// This function requires that TIGHTDB_ENABLE_MEM_USAGE is specified
/// during building. Otherwise it always returns zero.
size_t GetMemUsage();

#endif //__SUPPORT_MEM__
