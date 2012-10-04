lib_LIBRARIES = libtightdb-jni.a

libtightdb_jni_a_SOURCES       = $(wildcard *.cpp)
libtightdb_jni_a_LDFLAGS       = $(shell tightdb-config     --libs)
libtightdb_jni_a_LDFLAGS_DEBUG = $(shell tightdb-config-dbg --libs)
libtightdb_jni_a_LDFLAGS_COVER = $(shell tightdb-config-dbg --libs)

ifneq ($(TIGHTDB_ENABLE_MEM_USAGE),)
ADD_CFLAGS_INCLUDE += -DTIGHTDB_ENABLE_MEM_USAGE
libtightdb_jni_a_LDFLAGS       += -lproc
libtightdb_jni_a_LDFLAGS_DEBUG += -lproc
libtightdb_jni_a_LDFLAGS_COVER += -lproc
endif

include ../../generic.mk
