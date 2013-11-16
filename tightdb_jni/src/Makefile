EXTRA_PRIMARY_PREFIXES = jni
jni_LIBRARIES   = libtightdb-jni.a
jni_INSTALL_DIR = $(JNI_INSTALL_DIR)

JNI_SOURCES := $(wildcard *.cpp)
libtightdb_jni_a_SOURCES = $(JNI_SOURCES)

libtightdb_jni_a_CFLAGS = $(JAVA_CFLAGS)

ifneq ($(TIGHTDB_ENABLE_MEM_USAGE),)
libtightdb_jni_a_CFLAGS  += -DTIGHTDB_ENABLE_MEM_USAGE
ifeq ($(shell pkg-config libprocps --exists 2>/dev/null && echo yes),yes)
PROCPS_CFLAGS  := $(shell pkg-config libprocps --cflags)
PROCPS_LDFLAGS := $(shell pkg-config libprocps --libs)
libtightdb_jni_a_CFLAGS  += $(PROCPS_CFLAGS)
libtightdb_jni_a_LDFLAGS += $(PROCPS_LDFLAGS)
else
libtightdb_jni_a_LDFLAGS += -lproc
endif
endif

libtightdb_jni_a_CFLAGS_OPTIM  = $(TIGHTDB_CFLAGS)
libtightdb_jni_a_CFLAGS_DEBUG  = $(TIGHTDB_CFLAGS_DBG)
libtightdb_jni_a_CFLAGS_COVER  = $(TIGHTDB_CFLAGS_DBG)
libtightdb_jni_a_LDFLAGS_OPTIM = $(TIGHTDB_LDFLAGS)
libtightdb_jni_a_LDFLAGS_DEBUG = $(TIGHTDB_LDFLAGS_DBG)
libtightdb_jni_a_LDFLAGS_COVER = $(TIGHTDB_LDFLAGS_DBG)

include ../generic.mk

# Used by ../../build.sh
get-inst-libraries:
	@echo $(filter-out libtightdb-jni-cov.%,$(TARGETS_LIB_SHARED_ALIASES))
