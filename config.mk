# Construct fat binaries on Darwin when using Clang
ifneq ($(TIGHTDB_ENABLE_FAT_BINARIES),)
ifneq ($(call CC_CXX_AND_LD_ARE,clang),)
ifeq ($(shell uname),Darwin)
CFLAGS_DEFAULT   += -arch i386 -arch x86_64
LDFLAGS_DEFAULT  += -arch i386 -arch x86_64
endif
endif
endif

ifneq ($(CC_CXX_AND_LD_ARE_GCC_LIKE),)
CFLAGS_DEFAULT   += -Wextra -ansi -pedantic -Wno-long-long
# FIXME: '-fno-elide-constructors' currently causes TightDB to fail
#CFLAGS_DEBUG     += -fno-elide-constructors
CFLAGS_PTHREAD   += -pthread
ifeq ($(TIGHTDB_DISABLE_SSE),)
CFLAGS_DEFAULT   += -msse4.2
endif
endif


ifeq ($(TIGHTDB_DISABLE_SSE),)
CFLAGS_DEFAULT   += -DUSE_SSE42
endif

CFLAGS_DEBUG     += -DTIGHTDB_DEBUG -DMAX_LIST_SIZE=4
CFLAGS_COVERAGE  += -DTIGHTDB_DEBUG -DMAX_LIST_SIZE=4
