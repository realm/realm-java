lib_LIBRARIES = librealm-jni.a

OBJECT_STORE_SRC_DIR := $(OBJECT_STORE_DIR)/src
OBJECT_STORE_SORUCES := $(wildcard $(OBJECT_STORE_SRC_DIR)/*.cpp $(OBJECT_STORE_SRC_DIR)/impl/*.cpp)
OBJECT_STORE_CFLAGS := -I$(OBJECT_STORE_SRC_DIR) -I$(OBJECT_STORE_SRC_DIR)/impl/android -I$(OBJECT_STORE_SRC_DIR)/impl

JNI_SOURCES := $(wildcard *.cpp)

librealm_jni_a_SOURCES = $(JNI_SOURCES) $(OBJECT_STORE_SORUCES)
librealm_jni_a_CFLAGS = $(OBJECT_STORE_CFLAGS)

include ../generic.mk
