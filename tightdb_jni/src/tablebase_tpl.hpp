#ifndef TIGHTDB_JNI_TABLEBASE_TPL_HPP
#define TIGHTDB_JNI_TABLEBASE_TPL_HPP


template <class T> jbyteArray tbl_GetByteArray(
	JNIEnv* env, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	BinaryData data = reinterpret_cast<T*>(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
	if (data.len <= MAX_JSIZE) {
        jbyteArray jresult = env->NewByteArray(static_cast<jsize>(data.len));
        if (jresult)
            env->SetByteArrayRegion(jresult, 0, static_cast<jsize>(data.len), (const jbyte*)(data.pointer));
	    return jresult;
    } else {
        //???TODO: Better exception
        ThrowException(env, IndexOutOfBounds, "Length of ByteArray is larger than int.");
        return NULL;
    }
}

#endif // TIGHTDB_JNI_TABLEBASE_TPL_HPP