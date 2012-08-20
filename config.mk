ifneq ($(CC_AND_CXX_ARE_GCC_LIKE),)
CFLAGS_DEFAULT  += -Wextra -ansi -pedantic -Wno-long-long
CFLAGS_OPTIMIZE += -msse4.2
# FIXME: '-fno-elide-constructors' currently causes TightDB to fail
#CFLAGS_DEBUG    += -fno-elide-constructors
CFLAGS_COVERAGE += -msse4.2
CFLAGS_PTHREAD  += -pthread
endif

CFLAGS_OPTIMIZE += -DUSE_SSE
CFLAGS_DEBUG    += -DTIGHTDB_DEBUG -DMAX_LIST_SIZE=4
CFLAGS_COVERAGE += -DUSE_SSE -DTIGHTDB_DEBUG -DMAX_LIST_SIZE=4

jnidir = $(libdir)/jni
