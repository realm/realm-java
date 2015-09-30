lib_LIBRARIES = librealm-jni.a

JNI_SOURCES := $(wildcard *.cpp)
librealm_jni_a_SOURCES = $(JNI_SOURCES)

# Used by ../../build.sh
get-inst-libraries:
	@echo $(filter-out librealm-jni-cov.%,$(TARGETS_LIB_SHARED_ALIASES))

include ../generic.mk
