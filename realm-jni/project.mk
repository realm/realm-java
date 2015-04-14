ENABLE_INSTALL_DEBUG_LIBS = 1

# Construct fat binaries on Darwin when using Clang
ifneq ($(REALM_ENABLE_FAT_BINARIES),)
  ifeq ($(OS),Darwin)
    ifeq ($(COMPILER_IS),clang)
      CFLAGS_ARCH += -arch i386 -arch x86_64
    endif
  endif
endif

ifeq ($(OS),Darwin)
  CFLAGS_ARCH += -mmacosx-version-min=10.8 -stdlib=libc++
endif

# FIXME: '-fno-elide-constructors' currently causes Realm to fail
#CFLAGS_DEBUG += -fno-elide-constructors
CFLAGS_PTHREADS += -pthread
CFLAGS_GENERAL += -Wextra -ansi -pedantic -Wno-long-long

# Avoid a warning from Clang when linking on OS X. By default,
# `LDFLAGS_PTHREADS` inherits its value from `CFLAGS_PTHREADS`, so we
# have to override that with an empty value.
ifeq ($(OS),Darwin)
  ifeq ($(LD_IS),clang)
    LDFLAGS_PTHREADS = $(EMPTY)
  endif
endif

# Load dynamic configuration
#ifeq ($(NO_CONFIG_MK),)
  #CONFIG_MK = $(GENERIC_MK_DIR)/config.mk
  #DEP_MAKEFILES += $(CONFIG_MK)
  #include $(CONFIG_MK)
  LIB_SUFFIX_SHARED = $(JNI_SUFFIX)
  #EXTRA_PRIMARY_PREFIXES = jni
  #jnidir = $(JNI_INSTALL_DIR)
#endif

ifeq ($(REALM_ANDROID),)
  REALM_LDFLAGS += -llog
  CFLAGS_INCLUDE += $(JAVA_CFLAGS)
  ifneq ($(REALM_ENABLE_MEM_USAGE),)
    PROJECT_CFLAGS += -DREALM_ENABLE_MEM_USAGE
    ifeq ($(shell pkg-config libprocps --exists 2>/dev/null && echo yes),yes)
      PROCPS_CFLAGS  := $(shell pkg-config libprocps --cflags)
      PROCPS_LDFLAGS := $(shell pkg-config libprocps --libs)
      PROJECT_CFLAGS  += $(PROCPS_CFLAGS)
      PROJECT_LDFLAGS += $(PROCPS_LDFLAGS)
    else
      PROJECT_LDFLAGS += -lproc
    endif
  endif
else
  PROJECT_CFLAGS += -fvisibility=hidden -DANDROID
  CFLAGS_OPTIM = -Os -DNDEBUG
endif

PROJECT_CFLAGS_OPTIM  += $(REALM_CFLAGS)
PROJECT_CFLAGS_DEBUG  += $(REALM_CFLAGS_DBG)
PROJECT_CFLAGS_COVER  += $(REALM_CFLAGS_DBG)
PROJECT_LDFLAGS_OPTIM += $(REALM_LDFLAGS)
PROJECT_LDFLAGS_DEBUG += $(REALM_LDFLAGS_DBG)
PROJECT_LDFLAGS_COVER += $(REALM_LDFLAGS_DBG)
