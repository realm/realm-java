#include <assert.h>
#include <tightdb.hpp>

#include "util.h"
#include "com_tightdb_util.h"

using namespace tightdb;

void ThrowException(JNIEnv* env, ExceptionKind exception, std::string classStr, std::string itemStr) 
{
    std::string message;
    jclass jExceptionClass = NULL;

    TR("\njni: ThrowingException %d, %s, %s.\n", exception, classStr.c_str(), itemStr.c_str());

    switch (exception) {
        case ClassNotFound:
            jExceptionClass = env->FindClass("java/lang/ClassNotFoundException");
            message = "Class '" + classStr + "' could not be located.";
            break;

        case NoSuchField:
            jExceptionClass = env->FindClass("java/lang/NoSuchFieldException");
            message = "Field '" + itemStr + "' could not be located in class com.tightdb." + classStr;
            break;

        case NoSuchMethod:
            jExceptionClass = env->FindClass("java/lang/NoSuchMethodException");
            message = "Method '" + itemStr + "' could not be located in class com.tightdb." + classStr;
            break;

        case IllegalArgument:
            jExceptionClass = env->FindClass("java/lang/IllegalArgumentException");
            message = "Illegal Argument: " + classStr;
            break;
        
        case IOFailed:
            jExceptionClass = env->FindClass("java/lang/IOException");
            message = "Failed to open " + classStr;
            break;

        case IndexOutOfBounds:
            jExceptionClass = env->FindClass("java/lang/ArrayIndexOutOfBoundsException");
            message = classStr;
            break;
        
        default:
            assert(0);
            return;
    }
    if (jExceptionClass != NULL)
        env->ThrowNew(jExceptionClass, message.c_str());
    else {
        TR("\nERROR: Couldn't throw exception.\n");
    }

    env->DeleteLocalRef(jExceptionClass);
}

jclass GetClass(JNIEnv* env, char *classStr) 
{
    jclass localRefClass = env->FindClass(classStr);	
    if (localRefClass == NULL) {
		ThrowException(env, ClassNotFound, classStr);
		return NULL;
	}

    jclass myClass = reinterpret_cast<jclass>( env->NewGlobalRef(localRefClass) );
    env->DeleteLocalRef(localRefClass);
    return myClass;
}

void jprint(JNIEnv *env, char *txt)
{
#if 1
    fprintf(stderr, " -- JNI: %s", txt);  fflush(stderr);
#else
    static jclass myClass = GetClass(env, "com/tightdb/util");
    static jmethodID myMethod = env->GetStaticMethodID(myClass, "javaPrint", "(Ljava/lang/String;)V");
    if (myMethod)
        env->CallStaticVoidMethod(myClass, myMethod, env->NewStringUTF(txt));
#endif
}

void jprintf(JNIEnv *env, const char *format, ...) {
    va_list argptr;
    char buf[200];
    va_start(argptr, format);
    //vfprintf(stderr, format, argptr);
    vsnprintf_s(buf, 200, format, argptr);
    jprint(env, buf);
    va_end(argptr);  
    fflush(stdout);
}

bool IndexValid(JNIEnv* env, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex) 
{
    tightdb::Table *tbl = reinterpret_cast<tightdb::Table*>(nativeTablePtr);
    bool colErr = (S(columnIndex) >= tbl->get_column_count()) || (columnIndex < 0);
    bool rowErr = (S(rowIndex) >= tbl->size()) || (rowIndex < 0);

    if (!colErr && !rowErr)
        return true;

    if (colErr)
        ThrowException(env, IndexOutOfBounds, "columnIndex > available columns.");
    if (rowErr)
        ThrowException(env, IndexOutOfBounds, "rowIndex > available rows.");
    return false;
}

bool IndexAndTypeValid(JNIEnv* env, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, 
    int expectColType) 
{
    if (!IndexValid(env, nativeTablePtr, columnIndex, rowIndex))
        return false;
    Table *tbl = TBL(nativeTablePtr);
    int colType = tbl->get_column_type(columnIndex);
    if (colType == COLUMN_TYPE_MIXED)
        colType = tbl->get_mixed_type(columnIndex, rowIndex);
    
    if (colType != expectColType) {
        TR("Expected columnType %d, but got %d (real %d)", expectColType,
            tbl->get_column_type(columnIndex), tbl->GetRealColumnType(columnIndex));
	    ThrowException(env, IllegalArgument, "column type != ColumnTypeTable.");
        return false;
    }
    return true;
}