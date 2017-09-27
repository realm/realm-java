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

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class IOSAllTypes extends RealmObject {

    public final static String CLASS_NAME = "IOSAllTypes";

    @PrimaryKey
    private long id;

    private boolean boolCol;
    private short shortCol;
    private int intCol;
    private int longCol;
    private long longLongCol;
    private float floatCol;
    private double doubleCol;
    private byte[] byteCol = new byte[0];
    private String stringCol = "";
    private Date dateCol = new Date(0);
    private IOSChild child;
    private RealmList<IOSChild> children;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isBoolCol() {
        return boolCol;
    }

    public void setBoolCol(boolean boolCol) {
        this.boolCol = boolCol;
    }

    public short getShortCol() {
        return shortCol;
    }

    public void setShortCol(short shortCol) {
        this.shortCol = shortCol;
    }

    public int getIntCol() {
        return intCol;
    }

    public void setIntCol(int intCol) {
        this.intCol = intCol;
    }

    public int getLongCol() {
        return longCol;
    }

    public void setLongCol(int longCol) {
        this.longCol = longCol;
    }

    public long getLongLongCol() {
        return longLongCol;
    }

    public void setLongLongCol(long longLongCol) {
        this.longLongCol = longCol;
    }

    public float getFloatCol() {
        return floatCol;
    }

    public void setFloatCol(float floatCol) {
        this.floatCol = floatCol;
    }

    public double getDoubleCol() {
        return doubleCol;
    }

    public void setDoubleCol(double doubleCol) {
        this.doubleCol = doubleCol;
    }

    public byte[] getByteCol() {
        return byteCol;
    }

    public void setByteCol(byte[] byteCol) {
        this.byteCol = byteCol;
    }

    public String getStringCol() {
        return stringCol;
    }

    public void setStringCol(String stringCol) {
        this.stringCol = stringCol;
    }

    public Date getDateCol() {
        return dateCol;
    }

    public void setDateCol(Date dateCol) {
        this.dateCol = dateCol;
    }

    public IOSChild getChild() {
        return child;
    }

    public void setChild(IOSChild child) {
        this.child = child;
    }

    public RealmList<IOSChild> getChildren() {
        return children;
    }

    public void setChildren(RealmList<IOSChild> children) {
        this.children = children;
    }
}
