lib_LIBRARIES = libearthworm.a

JNI_SOURCES := $(wildcard *.cpp)
libearthworm_a_SOURCES = $(JNI_SOURCES)

# Used by ../../build.sh
get-inst-libraries:
	@echo $(filter-out libearthworm-cov.%,$(TARGETS_LIB_SHARED_ALIASES))

include ../generic.mk
