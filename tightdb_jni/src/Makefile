lib_LIBRARIES = libtightdb-jni.a

libtightdb_jni_a_SOURCES       = $(wildcard *.cpp)
libtightdb_jni_a_CFLAGS_OPTIM  = $(shell tightdb-config     --cflags)
libtightdb_jni_a_CFLAGS_DEBUG  = $(shell tightdb-config-dbg --cflags)
libtightdb_jni_a_CFLAGS_COVER  = $(shell tightdb-config-dbg --cflags)
libtightdb_jni_a_LDFLAGS_OPTIM = $(shell tightdb-config     --libs)
libtightdb_jni_a_LDFLAGS_DEBUG = $(shell tightdb-config-dbg --libs)
libtightdb_jni_a_LDFLAGS_COVER = $(shell tightdb-config-dbg --libs)

ifneq ($(TIGHTDB_ENABLE_MEM_USAGE),)
libtightdb_jni_a_CFLAGS_OPTIM  += -DTIGHTDB_ENABLE_MEM_USAGE
libtightdb_jni_a_CFLAGS_DEBUG  += -DTIGHTDB_ENABLE_MEM_USAGE
libtightdb_jni_a_CFLAGS_COVER  += -DTIGHTDB_ENABLE_MEM_USAGE
libtightdb_jni_a_LDFLAGS_OPTIM += -lproc
libtightdb_jni_a_LDFLAGS_DEBUG += -lproc
libtightdb_jni_a_LDFLAGS_COVER += -lproc
endif

include ../../generic.mk
