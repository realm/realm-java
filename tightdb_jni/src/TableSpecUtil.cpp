#include "util.hpp"
#include "TableSpecUtil.hpp"
#include "columntypeutil.hpp"

using namespace std;
using namespace tightdb;

jclass GetClassTableSpec(JNIEnv* env)
{
    static jclass myClass = GetClass(env, "com/tightdb/TableSpec");
    return myClass;
}

jmethodID GetTableSpecMethodID(JNIEnv* env, const char* methodStr, const char* typeStr)
{
    jclass myClass = GetClassTableSpec(env);
    if (myClass == NULL) {
        return NULL;
    }
    jmethodID myMethod = env->GetMethodID(myClass, methodStr, typeStr);
    if (myMethod == NULL) {
        ThrowException(env, NoSuchMethod, "TableSpec", methodStr);
        return NULL;
    }
    return myMethod;
}

jlong Java_com_tightdb_TableSpec_getColumnCount(JNIEnv* env, jobject jTableSpec)
{
    static jmethodID jGetColumnCountMethodId = GetTableSpecMethodID(env, "getColumnCount", "()J");
    if (jGetColumnCountMethodId)
        return env->CallLongMethod(jTableSpec, jGetColumnCountMethodId);
    return 0;
}

jobject Java_com_tightdb_TableSpec_getColumnType(JNIEnv* env, jobject jTableSpec, jlong columnIndex)
{
    static jmethodID jGetColumnTypeMethodId = GetTableSpecMethodID(env, "getColumnType", "(J)Lcom/tightdb/ColumnType;");
    if (jGetColumnTypeMethodId)
        return env->CallObjectMethod(jTableSpec, jGetColumnTypeMethodId, columnIndex);
    return NULL;
}

jstring Java_com_tightdb_TableSpec_getColumnName(JNIEnv* env, jobject jTableSpec, jlong columnIndex)
{
    static jmethodID jGetColumnNameMethodId = GetTableSpecMethodID(env, "getColumnName", "(J)Ljava/lang/String;");
    if (jGetColumnNameMethodId)
        return (jstring)env->CallObjectMethod(jTableSpec, jGetColumnNameMethodId, columnIndex);
    return NULL;
}

jobject Java_com_tightdb_TableSpec_getTableSpec(JNIEnv* env, jobject jTableSpec, jlong columnIndex)
{
    static jmethodID jGetTableSpecMethodId = GetTableSpecMethodID(env, "getSubtableSpec", "(J)Lcom/tightdb/TableSpec;");
    if (jGetTableSpecMethodId)
        return env->CallObjectMethod(jTableSpec, jGetTableSpecMethodId, columnIndex);
    return NULL;
}

jlong Java_com_tightdb_TableSpec_getColumnIndex(JNIEnv* env, jobject jTableSpec, jstring columnName)
{
    static jmethodID jGetColumnIndexMethodId = GetTableSpecMethodID(env, "getColumnIndex", "(Ljava/lang/String;)J");
    if (jGetColumnIndexMethodId)
        return env->CallLongMethod(jTableSpec, jGetColumnIndexMethodId, columnName);
    return 0;
}

void updateSpecFromJSpec(JNIEnv* env, Table* table, const vector<size_t>& path, jobject jTableSpec)
{
    jlong n = Java_com_tightdb_TableSpec_getColumnCount(env, jTableSpec);
    for (jlong i = 0; i != n; ++i) {
        jstring jColumnName = Java_com_tightdb_TableSpec_getColumnName(env, jTableSpec, i);
        JStringAccessor name(env, jColumnName);
        if (!name)
            return;

        jobject jColumnType = Java_com_tightdb_TableSpec_getColumnType(env, jTableSpec, i);
        DataType type = GetColumnTypeFromJColumnType(env, jColumnType);
        table->add_subcolumn(path, type, name);
        if (type == type_Table) {
            vector<size_t> subpath = path;
            subpath.push_back(i);
            jobject jNextColumnTableSpec = Java_com_tightdb_TableSpec_getTableSpec(env, jTableSpec, i);
            updateSpecFromJSpec(env, table, subpath, jNextColumnTableSpec);
        }
    }
}

void UpdateJTableSpecFromSpec(JNIEnv* env, const Spec& spec, jobject jTableSpec)
{
    static jmethodID jAddColumnMethodId = GetTableSpecMethodID(env, "addColumn", "(ILjava/lang/String;)V");
    static jmethodID jAddSubtableColumnMethodId = GetTableSpecMethodID(env, "addSubtableColumn", 
                                                                            "(Ljava/lang/String;)Lcom/tightdb/TableSpec;");

    if (jAddColumnMethodId == NULL || jAddSubtableColumnMethodId == NULL) {
        return;
    }

    size_t n = spec.get_column_count(); // noexcept
    for (size_t i = 0; i != n; ++i) {
        DataType type   = spec.get_column_type(i); // noexcept
        StringData name = spec.get_column_name(i); // noexcept
        if (type == type_Table) {
            jobject jSubTableSpec = env->CallObjectMethod(jTableSpec, jAddSubtableColumnMethodId, 
                                                          to_jstring(env, name));
            Spec subspec = SubspecRef(SubspecRef::const_cast_tag(), spec.get_subtable_spec(i)); // Throws
            UpdateJTableSpecFromSpec(env, subspec, jSubTableSpec);
        }
        else {
            env->CallVoidMethod(jTableSpec, jAddColumnMethodId, jint(type), to_jstring(env, name));
        }
    }
}
