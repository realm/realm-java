#include "io_realm_LinkView.h"
#include "util.hpp"

using namespace tightdb;

JNIEXPORT void JNICALL Java_io_realm_LinkView_nativeClose
  (JNIEnv*, jclass, jlong nativeLinkViewPtr)
{
    LangBindHelper::unbind_linklist_ptr( LV( nativeLinkViewPtr ) );
}


JNIEXPORT jlong JNICALL Java_io_realm_LinkView_nativeGetRow
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos)
{
    try {
        Row* row = new Row( (*LV(nativeLinkViewPtr))[ S(pos) ] );
        return reinterpret_cast<jlong>(row);
    } CATCH_STD()
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_LinkView_nativeGetTargetRowIndex
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos)
{
    try {
        return LV(nativeLinkViewPtr)->get( S(pos) ).get_index();
    } CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_LinkView_nativeAdd
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong rowIndex)
{
    try {
        return LV(nativeLinkViewPtr)->add( S(rowIndex) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_LinkView_nativeInsert
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos, jlong rowIndex)
{
    try {
        return LV(nativeLinkViewPtr)->insert( S(pos), S(rowIndex) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_LinkView_nativeSet
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos, jlong rowIndex)
{
    try {
        return LV(nativeLinkViewPtr)->set( S(pos), S(rowIndex) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_LinkView_nativeMove
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong old_pos, jlong new_pos)
{
    try {
        return LV(nativeLinkViewPtr)->move( S(old_pos), S(new_pos) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_LinkView_nativeRemove
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos)
{
    try {
        return LV(nativeLinkViewPtr)->remove( S(pos) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_LinkView_nativeClear
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr)
{
    try {
        return LV(nativeLinkViewPtr)->clear();
    } CATCH_STD()
}


JNIEXPORT jlong JNICALL Java_io_realm_LinkView_nativeSize
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr)
{
    try {
        return LV(nativeLinkViewPtr)->size();
    } CATCH_STD()
    return 0;
}


JNIEXPORT jboolean JNICALL Java_io_realm_LinkView_nativeIsEmpty
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr)
{
    try {
        return LV(nativeLinkViewPtr)->is_empty();
    } CATCH_STD()
    return 0;
}
