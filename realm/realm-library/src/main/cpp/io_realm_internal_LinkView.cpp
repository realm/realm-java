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
#include "util.hpp"

using namespace realm;

JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeClose
  (JNIEnv*, jclass, jlong nativeLinkViewPtr)
{
    LangBindHelper::unbind_linklist_ptr(*LV(nativeLinkViewPtr));
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeGetRow
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    LinkViewRef *lv = LV(nativeLinkViewPtr);
    if (!ROW_INDEX_VALID(env, *lv, pos)) {
        return -1;
    }
    try {
        LinkViewRef lvr = *lv;
        Row* row = new Row( (*lvr)[ S(pos) ] );
        return reinterpret_cast<jlong>(row);
    } CATCH_STD()
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeGetTargetRowIndex
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong linkViewIndex)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    LinkViewRef *lv = LV(nativeLinkViewPtr);
    if (!ROW_INDEX_VALID(env, *lv, linkViewIndex)) {
        return -1;
    }
    try {
        LinkViewRef lvr = *lv;
        return lvr->get(S(linkViewIndex)).get_index();
    } CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeAdd
  (JNIEnv* env, jclass, jlong nativeLinkViewPtr, jlong rowIndex)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    LinkViewRef *lv = LV(nativeLinkViewPtr);
    try {
        LinkViewRef lvr = *lv;
        lvr->add( S(rowIndex) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeInsert
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos, jlong rowIndex)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    LinkViewRef *lv = LV(nativeLinkViewPtr);
    try {
        LinkViewRef lvr = *lv;
        lvr->insert( S(pos), S(rowIndex) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeSet
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos, jlong rowIndex)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    LinkViewRef *lv = LV(nativeLinkViewPtr);
    if (!ROW_INDEX_VALID(env, *lv, pos)) {
        return;
    }
    try {
        LinkViewRef lvr = *lv;
        lvr->set( S(pos), S(rowIndex) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeMove
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong old_pos, jlong new_pos)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    try {
        LinkViewRef *lv = LV(nativeLinkViewPtr);
        LinkViewRef lvr = *lv;
        size_t size = lvr->size();
        if (old_pos < 0 || new_pos < 0 || size_t(old_pos) >= size || size_t(new_pos) >= size) {
            ThrowException(env, IndexOutOfBounds,
                "Indices must be within range [0, " + num_to_string(size) + "[. " +
                "Yours were (" + num_to_string(old_pos) + "," + num_to_string(new_pos) + ")");
            return;
        }
        lvr->move( S(old_pos), S(new_pos) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeRemove
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    LinkViewRef *lv = LV(nativeLinkViewPtr);
    if (!ROW_INDEX_VALID(env, *lv, pos)) {
        return;
    }
    try {
        LinkViewRef lvr = *lv;
        return lvr->remove( S(pos) );
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeClear
  (JNIEnv* env, jclass, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    try {
        LinkViewRef *lv = LV(nativeLinkViewPtr);
        LinkViewRef lvr = *lv;
        return lvr->clear();
    } CATCH_STD()
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeSize
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr)
{
    
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    try {
        LinkViewRef *lv = LV(nativeLinkViewPtr);
        LinkViewRef lvr = *lv;
        return lvr->size();
    } CATCH_STD()
    return 0;
}


JNIEXPORT jboolean JNICALL Java_io_realm_internal_LinkView_nativeIsEmpty
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    try {
        LinkViewRef *lv = LV(nativeLinkViewPtr);
        LinkViewRef lvr = *lv;
        return lvr->is_empty();
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeWhere
  (JNIEnv *env, jobject, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    try {
        LinkViewRef *lv = LV(nativeLinkViewPtr);
        LinkViewRef lvr = *lv;
        Query *queryPtr = new Query(lvr->get_target_table().where(LinkViewRef(lvr)));
        return reinterpret_cast<jlong>(queryPtr);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_LinkView_nativeIsAttached
  (JNIEnv *env, jobject, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    try {
        LinkViewRef *lv = LV(nativeLinkViewPtr);
        LinkViewRef lvr = *lv;
        return lvr->is_attached();
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeFind
  (JNIEnv *env, jobject, jlong nativeLinkViewPtr, jlong targetRowIndex)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    try {
        LinkViewRef *lv = LV(nativeLinkViewPtr);
        LinkViewRef lvr = *lv;
        if (!ROW_INDEX_VALID(env, &lvr->get_target_table(), targetRowIndex)) {
            return -1;
        }
        size_t ndx = lvr->find(targetRowIndex);
        return to_jlong_or_not_found(ndx);
    } CATCH_STD()
    return -1;
}

JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeRemoveAllTargetRows
  (JNIEnv *env, jobject, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    try {
        LinkViewRef* lv = LV(nativeLinkViewPtr);
        LinkViewRef lvr = *lv;
        lvr->remove_all_target_rows();
    } CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_LinkView_nativeGetTargetTable
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)

    LinkViewRef* lv = LV(nativeLinkViewPtr);
    LinkViewRef lvr = *lv;
    Table* pTable = &(lvr->get_target_table());
    LangBindHelper::bind_table_ptr(pTable);

    return reinterpret_cast<jlong>(pTable);
}

JNIEXPORT void JNICALL Java_io_realm_internal_LinkView_nativeRemoveTargetRow
  (JNIEnv* env, jobject, jlong nativeLinkViewPtr, jlong pos)
{
    TR_ENTER_PTR(env, nativeLinkViewPtr)
    LinkViewRef* lv = LV(nativeLinkViewPtr);
    if (!ROW_INDEX_VALID(env, *lv, pos)) {
        return;
    }
    try {
        LinkViewRef lvr = *lv;
        return lvr->remove_target_row( S(pos) );
    } CATCH_STD()
}
