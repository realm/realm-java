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
    public static final String FIELD_OBJECT = "fieldObject";

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
        return realmGetter$indexString();
    }

    public void setIndexString(String indexString) {
        realmSetter$indexString(indexString);
    }

    public String realmGetter$indexString() {
        return indexString;
    }

    public void realmSetter$indexString(String indexString) {
        this.indexString = indexString;
    }

    public String getNotIndexString() {
        return realmGetter$notIndexString();
    }

    public void setNotIndexString(String notIndexString) {
        realmSetter$notIndexString(notIndexString);
    }

    public String realmGetter$notIndexString() {
        return notIndexString;
    }

    public void realmSetter$notIndexString(String notIndexString) {
        this.notIndexString = notIndexString;
    }

    public int getIndexInt() {
        return realmGetter$indexInt();
    }

    public void setIndexInt(int indexInt) {
        realmSetter$indexInt(indexInt);
    }

    public int realmGetter$indexInt() {
        return indexInt;
    }

    public void realmSetter$indexInt(int indexInt) {
        this.indexInt = indexInt;
    }

    public int getNotIndexInt() {
        return realmGetter$notIndexInt();
    }

    public void setNotIndexInt(int notIndexInt) {
        realmSetter$notIndexInt(notIndexInt);
    }

    public int realmGetter$notIndexInt() {
        return notIndexInt;
    }

    public void realmSetter$notIndexInt(int notIndexInt) {
        this.notIndexInt = notIndexInt;
    }

    public byte getIndexByte() {
        return realmGetter$indexByte();
    }

    public void setIndexByte(byte indexByte) {
        realmSetter$indexByte(indexByte);
    }

    public byte realmGetter$indexByte() {
        return indexByte;
    }

    public void realmSetter$indexByte(byte indexByte) {
        this.indexByte = indexByte;
    }

    public byte getNotIndexByte() {
        return realmGetter$notIndexByte();
    }

    public void setNotIndexByte(byte notIndexByte) {
        realmSetter$notIndexByte(notIndexByte);
    }

    public byte realmGetter$notIndexByte() {
        return notIndexByte;
    }

    public void realmSetter$notIndexByte(byte notIndexByte) {
        this.notIndexByte = notIndexByte;
    }

    public short getIndexShort() {
        return realmGetter$indexShort();
    }

    public void setIndexShort(short indexShort) {
        realmSetter$indexShort(indexShort);
    }

    public short realmGetter$indexShort() {
        return indexShort;
    }

    public void realmSetter$indexShort(short indexShort) {
        this.indexShort = indexShort;
    }

    public short getNotIndexShort() {
        return realmGetter$notIndexShort();
    }

    public void setNotIndexShort(short notIndexShort) {
        realmSetter$notIndexShort(notIndexShort);
    }

    public short realmGetter$notIndexShort() {
        return notIndexShort;
    }

    public void realmSetter$notIndexShort(short notIndexShort) {
        this.notIndexShort = notIndexShort;
    }

    public long getIndexLong() {
        return realmGetter$indexLong();
    }

    public void setIndexLong(long indexLong) {
        realmSetter$indexLong(indexLong);
    }

    public long realmGetter$indexLong() {
        return indexLong;
    }

    public void realmSetter$indexLong(long indexLong) {
        this.indexLong = indexLong;
    }

    public long getNotIndexLong() {
        return realmGetter$notIndexLong();
    }

    public void setNotIndexLong(long notIndexLong) {
        realmSetter$notIndexLong(notIndexLong);
    }

    public long realmGetter$notIndexLong() {
        return notIndexLong;
    }

    public void realmSetter$notIndexLong(long notIndexLong) {
        this.notIndexLong = notIndexLong;
    }

    public boolean getIndexBoolean() {
        return realmGetter$indexBoolean();
    }

    public void setIndexBoolean(boolean indexBoolean) {
        realmSetter$indexBoolean(indexBoolean);
    }

    public boolean realmGetter$indexBoolean() {
        return indexBoolean;
    }

    public void realmSetter$indexBoolean(boolean indexBoolean) {
        this.indexBoolean = indexBoolean;
    }

    public boolean getNotIndexBoolean() {
        return realmGetter$notIndexBoolean();
    }

    public void setNotIndexBoolean(boolean notIndexBoolean) {
        realmSetter$notIndexBoolean(notIndexBoolean);
    }

    public boolean realmGetter$notIndexBoolean() {
        return notIndexBoolean;
    }

    public void realmSetter$notIndexBoolean(boolean notIndexBoolean) {
        this.notIndexBoolean = notIndexBoolean;
    }

    public Date getIndexDate() {
        return realmGetter$indexDate();
    }

    public void setIndexDate(Date indexDate) {
        realmSetter$indexDate(indexDate);
    }

    public Date realmGetter$indexDate() {
        return indexDate;
    }

    public void realmSetter$indexDate(Date indexDate) {
        this.indexDate = indexDate;
    }

    public Date getNotIndexDate() {
        return realmGetter$notIndexDate();
    }

    public void setNotIndexDate(Date notIndexDate) {
        realmSetter$notIndexDate(notIndexDate);
    }

    public Date realmGetter$notIndexDate() {
        return notIndexDate;
    }

    public void realmSetter$notIndexDate(Date notIndexDate) {
        this.notIndexDate = notIndexDate;
    }

    public void setFieldObject(AnnotationIndexTypes fieldObject) {
        realmSetter$fieldObject(fieldObject);
    }

    public AnnotationIndexTypes getFieldObject() {
        return realmGetter$fieldObject();
    }

    public void realmSetter$fieldObject(AnnotationIndexTypes fieldObject) {
        this.fieldObject = fieldObject;
    }

    public AnnotationIndexTypes realmGetter$fieldObject() { return this.fieldObject; }

}
