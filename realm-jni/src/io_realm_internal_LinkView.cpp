/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "io_realm_internal_LinkView.h"
#include "tablequery.hpp"
#include "util.hpp"

using namespace realm;

JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeClose
  (JNIEnv*, jclass, jlong nativeLinkViewPtr)
{
    LangBindHelper::unbind_linklist_ptr( LV( nativeLinkViewPtr ) );
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeGetRow
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    LinkView *lv = LV(nativeLinkViewPtr);
    if (!ROW_INDEX_VALID(env, lv, pos)) {
        return -1;
    }
    try {
        Row* row = new Row( (*lv)[ S(pos) ] );
        return reinterpret_cast<jlong>(row);
    } CATCH_STD()
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeGetTargetRowIndex
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    LinkView *lv = LV(nativeLinkViewPtr);
    if (!ROW_INDEX_VALID(env, lv, pos)) {
        return -1;
    }
    try {
        return lv->get( S(pos) ).get_index();
    } CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeAdd
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong rowIndex)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    try {
        LV(nativeLinkViewPtr)->add( S(rowIndex) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeInsert
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos, jlong rowIndex)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    try {
        LV(nativeLinkViewPtr)->insert( S(pos), S(rowIndex) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeSet
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos, jlong rowIndex)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    LinkView *lv = LV(nativeLinkViewPtr);
    if (!ROW_INDEX_VALID(env, lv, pos)) {
        return;
    }
    try {
        lv->set( S(pos), S(rowIndex) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeMove
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong old_pos, jlong new_pos)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    try {
        size_t size = LV(nativeLinkViewPtr)->size();
        if (old_pos < 0 || new_pos < 0 || size_t(old_pos) >= size || size_t(new_pos) >= size) {
            ThrowException(env, IndexOutOfBounds,
                "Indices must be within range [0, " + num_to_string(size) + "[. " +
                "Yours were (" + num_to_string(old_pos) + "," + num_to_string(new_pos) + ")");
        }
        return LV(nativeLinkViewPtr)->move( S(old_pos), S(new_pos) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeRemove
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    LinkView *lv = LV(nativeLinkViewPtr);
    if (!ROW_INDEX_VALID(env, lv, pos)) {
        return;
    }
    try {
        return lv->remove( S(pos) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeClear
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    try {
        return LV(nativeLinkViewPtr)->clear();
    } CATCH_STD()
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeSize
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    try {
        return LV(nativeLinkViewPtr)->size();
    } CATCH_STD()
    return 0;
}


JNIEXPORT jboolean JNICALL Java_io_realm_internal_LinkView_nativeIsEmpty
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
    try {
        return LV(nativeLinkViewPtr)->is_empty();
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeWhere
  (JNIEnv *env, jobject, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(nativeLinkViewPtr)
//    try {
//        LinkView *lv = LV(nativeLinkViewPtr);
//        Query query = lv->get_target_table().where(lv);
//        TableQuery* queryPtr = new TableQuery(query);
//        return reinterpret_cast<jlong>(queryPtr);
//    } CATCH_STD()
    return 0;
}
