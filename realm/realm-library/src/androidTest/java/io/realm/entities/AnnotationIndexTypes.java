/*
 * Copyright 2015 Realm Inc.
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

package io.realm.entities;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;

// Class for testing annotation index only
public class AnnotationIndexTypes extends RealmObject {

    public static final String CLASS_NAME = "AnnotationIndexTypes";

    public static final String FIELD_INDEX_STRING = "indexString";
    public static final String FIELD_INDEX_INT = "indexInt";
    public static final String FIELD_INDEX_BYTE = "indexByte";
    public static final String FIELD_INDEX_SHORT = "indexShort";
    public static final String FIELD_INDEX_LONG = "indexLong";
    public static final String FIELD_INDEX_BOOL = "indexBoolean";
    public static final String FIELD_INDEX_DATE = "indexDate";

    public static final String FIELD_NOT_INDEX_STRING = "notIndexString";
    public static final String FIELD_NOT_INDEX_INT = "notIndexInt";
    public static final String FIELD_NOT_INDEX_BYTE = "notIndexByte";
    public static final String FIELD_NOT_INDEX_SHORT = "notIndexShort";
    public static final String FIELD_NOT_INDEX_LONG = "notIndexLong";
    public static final String FIELD_NOT_INDEX_BOOL = "notIndexBoolean";
    public static final String FIELD_NOT_INDEX_DATE = "notIndexDate";

    public static final String FIELD_OBJECT = "fieldObject";

    public static final String[] INDEX_FIELDS = new String[]{FIELD_INDEX_STRING, FIELD_INDEX_INT, FIELD_INDEX_BYTE, FIELD_INDEX_SHORT, FIELD_INDEX_LONG, FIELD_INDEX_BOOL, FIELD_INDEX_DATE};
    public static final String[] NOT_INDEX_FIELDS = new String[]{FIELD_NOT_INDEX_STRING, FIELD_NOT_INDEX_INT, FIELD_NOT_INDEX_BYTE, FIELD_NOT_INDEX_SHORT, FIELD_NOT_INDEX_LONG, FIELD_NOT_INDEX_BOOL, FIELD_NOT_INDEX_DATE};
    public static final String   INDEX_LINKED_FIELD_STRING = AnnotationIndexTypes.FIELD_OBJECT + "." + AnnotationIndexTypes.FIELD_INDEX_STRING;
    public static final String[] INDEX_LINKED_FIELDS = new String[]{FIELD_OBJECT + "." + FIELD_INDEX_STRING, FIELD_OBJECT + "." + FIELD_INDEX_INT};
    public static final String   NOT_INDEX_LINKED_FILED_STRING = FIELD_OBJECT + "." + FIELD_NOT_INDEX_STRING;
    public static final String[] NOT_INDEX_LINKED_FIELDS = new String[]{FIELD_OBJECT + "." + FIELD_NOT_INDEX_STRING, FIELD_OBJECT + "." + FIELD_NOT_INDEX_INT};
    public static final String[] NONEXISTANT_MIX_FIELDS = new String[]{FIELD_INDEX_INT, "doesNotExists", FIELD_INDEX_STRING};

    @Index
    private String indexString;
    private String notIndexString;

    @Index
    private int indexInt;
    private int notIndexInt;

    @Index
    private byte indexByte;
    private byte notIndexByte;

    @Index
    private short indexShort;
    private short notIndexShort;

    @Index
    private long indexLong;
    private long notIndexLong;

    @Index
    private boolean indexBoolean;
    private boolean notIndexBoolean;

    @Index
    private Date indexDate;
    private Date notIndexDate;

    private AnnotationIndexTypes fieldObject;

    public String getIndexString() {
        return indexString;
    }

    public void setIndexString(String indexString) {
        this.indexString = indexString;
    }

    public String getNotIndexString() {
        return notIndexString;
    }

    public void setNotIndexString(String notIndexString) {
        this.notIndexString = notIndexString;
    }

    public int getIndexInt() {
        return indexInt;
    }

    public void setIndexInt(int indexInt) {
        this.indexInt = indexInt;
    }

    public int getNotIndexInt() {
        return notIndexInt;
    }

    public void setNotIndexInt(int notIndexInt) {
        this.notIndexInt = notIndexInt;
    }

    public short getIndexShort() {
        return indexShort;
    }

    public void setIndexShort(short indexShort) {
        this.indexShort = indexShort;
    }

    public byte getIndexByte() {
        return indexByte;
    }

    public void setIndexByte(byte indexByte) {
        this.indexByte = indexByte;
    }

    public byte getNotIndexByte() {
        return notIndexByte;
    }

    public void setNotIndexByte(byte notIndexByte) {
        this.notIndexByte = notIndexByte;
    }

    public short getNotIndexShort() {
        return notIndexShort;
    }

    public void setNotIndexShort(short notIndexShort) {
        this.notIndexShort = notIndexShort;
    }

    public long getIndexLong() {
        return indexLong;
    }

    public void setIndexLong(long indexLong) {
        this.indexLong = indexLong;
    }

    public long getNotIndexLong() {
        return notIndexLong;
    }

    public void setNotIndexLong(long notIndexLong) {
        this.notIndexLong = notIndexLong;
    }

    public boolean isIndexBoolean() {
        return indexBoolean;
    }

    public void setIndexBoolean(boolean indexBoolean) {
        this.indexBoolean = indexBoolean;
    }

    public boolean isNotIndexBoolean() {
        return notIndexBoolean;
    }

    public void setNotIndexBoolean(boolean notIndexBoolean) {
        this.notIndexBoolean = notIndexBoolean;
    }

    public Date getIndexDate() {
        return indexDate;
    }

    public void setIndexDate(Date indexDate) {
        this.indexDate = indexDate;
    }

    public Date getNotIndexDate() {
        return notIndexDate;
    }

    public void setNotIndexDate(Date notIndexDate) {
        this.notIndexDate = notIndexDate;
    }

    public void setFieldObject(AnnotationIndexTypes object) {
        this.fieldObject = object;
    }

    public AnnotationIndexTypes getFieldObject() { return this.fieldObject; }
}
