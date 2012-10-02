lib_LIBRARIES = libtightdb-jni.a

libtightdb_jni_a_SOURCES       = $(wildcard *.cpp)
libtightdb_jni_a_LDFLAGS       = $(shell tightdb-config     --libs) -lproc
libtightdb_jni_a_LDFLAGS_DEBUG = $(shell tightdb-config-dbg --libs) -lproc
libtightdb_jni_a_LDFLAGS_COVER = $(shell tightdb-config-dbg --libs) -lproc

include ../../generic.mk
