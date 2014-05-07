jni_LIBRARIES = libtightdb-jni.a

JNI_SOURCES := $(wildcard *.cpp)
libtightdb_jni_a_SOURCES = $(JNI_SOURCES)

# Used by ../../build.sh
get-inst-libraries:
	@echo $(filter-out libtightdb-jni-cov.%,$(TARGETS_LIB_SHARED_ALIASES))

include ../generic.mk
