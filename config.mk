ifneq ($(CC_AND_CXX_ARE_GCC_LIKE),)
CFLAGS_DEFAULT   += -Wextra -ansi -pedantic -Wno-long-long -msse4.2
# FIXME: '-fno-elide-constructors' currently causes TightDB to fail
#CFLAGS_DEBUG     += -fno-elide-constructors
CFLAGS_PTHREAD   += -pthread
endif

CFLAGS_DEFAULT   += -DUSE_SSE42
CFLAGS_DEBUG     += -DTIGHTDB_DEBUG -DMAX_LIST_SIZE=4
CFLAGS_COVERAGE  += -DTIGHTDB_DEBUG -DMAX_LIST_SIZE=4

jnidir = $(libdir)/jni
