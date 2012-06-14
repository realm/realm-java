LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#STLPORT_BASE        := /Users/bc/android-ndk-r8/sources/cxx-stl/stlport
STLPORT_BASE        := $(STLPORT_BASE)
LOCAL_CFLAGS        += -I$(STLPORT_BASE)/stlport -Ijni/jni/mem_usage -Ijni/src -D__NEW__ -D__SGI_STL_INTERNAL_PAIR_H -DANDROID -DOS_ANDROID 
LOCAL_LDLIBS        += -L$(STLPORT_BASE)/libs/armeabi -lstlport_static

LOCAL_MODULE    := tightdb-jni
LOCAL_C_INCLUDES := $(STLPORT_BASE)/stlport
LOCAL_SRC_FILES := jni/columntypeutil.cpp jni/com_tightdb_Group.cpp jni/com_tightdb_TableBase.cpp jni/com_tightdb_TableQuery.cpp jni/com_tightdb_TableViewBase.cpp jni/com_tightdb_util.cpp jni/java_lang_List_Util.cpp jni/mixedutil.cpp jni/TableSpecUtil.cpp jni/util.cpp src/tightdb/alloc_slab.cpp src/tightdb/array.cpp src/tightdb/array_binary.cpp src/tightdb/array_blob.cpp src/tightdb/array_string.cpp src/tightdb/array_string_long.cpp src/tightdb/c-table.cpp src/tightdb/column.cpp src/tightdb/column_binary.cpp src/tightdb/column_mixed.cpp src/tightdb/column_string.cpp src/tightdb/column_string_enum.cpp src/tightdb/column_table.cpp src/tightdb/group.cpp src/tightdb/group_writer.cpp src/tightdb/index.cpp src/tightdb/query.cpp src/tightdb/spec.cpp src/tightdb/table.cpp src/tightdb/table_view.cpp src/tightdb/utf8.cpp src/tightdb/utilities.cpp

include $(BUILD_SHARED_LIBRARY)
