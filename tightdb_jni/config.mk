ENABLE_INSTALL_DEBUG_LIBS = 1

# Construct fat binaries on Darwin when using Clang
ifneq ($(TIGHTDB_ENABLE_FAT_BINARIES),)
ifneq ($(call CC_CXX_AND_LD_ARE,clang),)
ifeq ($(OS),Darwin)
CFLAGS_ARCH  += -arch i386 -arch x86_64
endif
endif
endif

# FIXME: '-fno-elide-constructors' currently causes TightDB to fail
#CFLAGS_DEBUG   += -fno-elide-constructors
CFLAGS_PTHREAD += -pthread
CFLAGS_GENERAL += -Wextra -ansi -pedantic -Wno-long-long

# Load dynamic configuration
ifeq ($(NO_CONFIG_DYN_MK),)
CONFIG_DYN_MK = $(GENERIC_MK_DIR)/config-dyn.mk
DEP_MAKEFILES += $(CONFIG_DYN_MK)
include $(CONFIG_DYN_MK)
TIGHTDB_INCLUDEDIR    := $(shell $(TIGHTDB_CONFIG)     --includedir)
TIGHTDB_CFLAGS        := $(shell $(TIGHTDB_CONFIG)     --cflags)
TIGHTDB_CFLAGS_DEBUG  := $(shell $(TIGHTDB_CONFIG_DBG) --cflags)
TIGHTDB_LIBDIR        := $(shell $(TIGHTDB_CONFIG)     --libdir)
TIGHTDB_LDFLAGS       := $(shell $(TIGHTDB_CONFIG)     --libs)
TIGHTDB_LDFLAGS_DEBUG := $(shell $(TIGHTDB_CONFIG_DBG) --libs)
LIB_SUFFIX_SHARED = $(JNI_SUFFIX)
endif
