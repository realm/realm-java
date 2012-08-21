EXTRA_INSTALL_PREFIXES = jni

jni_LIBRARIES = libtightdb-jni.a
jni_DIR       = $(jnidir)

libtightdb_jni_a_SOURCES       = $(wildcard *.cpp)
libtightdb_jni_a_LDFLAGS       = -ltightdb -lproc
libtightdb_jni_a_LDFLAGS_DEBUG = -ltightdb_d -lproc

include ../../generic.mk
