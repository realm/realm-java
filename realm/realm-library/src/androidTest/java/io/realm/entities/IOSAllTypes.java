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
import io.realm.annotations.Required;

public class IOSAllTypes extends RealmObject {

    @PrimaryKey
    private long id;

    private boolean boolCol;
    private short shortCol;
    private int intCol;
    private long longCol;
    private float floatCol;
    private double doubleCol;
    @Required
    private byte[] byteCol = new byte[0];
    @Required
    private String stringCol = "";
    @Required
    private Date dateCol = new Date(0);
    private IOSChild child;
    private RealmList<IOSChild> children;

    public long getId() {
        return realmGetter$id();
    }

    public void setId(long id) {
        realmSetter$id(id);
    }

    public long realmGetter$id() {
        return id;
    }

    public void realmSetter$id(long id) {
        this.id = id;
    }

    public boolean isBoolCol() {
        return realmGetter$boolCol();
    }

    public void setBoolCol(boolean boolCol) {
        realmSetter$boolCol(boolCol);
    }

    public boolean realmGetter$boolCol() {
        return boolCol;
    }

    public void realmSetter$boolCol(boolean boolCol) {
        this.boolCol = boolCol;
    }

    public short getShortCol() {
        return realmGetter$shortCol();
    }

    public void setShortCol(short shortCol) {
        realmSetter$shortCol(shortCol);
    }

    public short realmGetter$shortCol() {
        return shortCol;
    }

    public void realmSetter$shortCol(short shortCol) {
        this.shortCol = shortCol;
    }

    public int getIntCol() {
        return realmGetter$intCol();
    }

    public void setIntCol(int intCol) {
        realmSetter$intCol(intCol);
    }

    public int realmGetter$intCol() {
        return intCol;
    }

    public void realmSetter$intCol(int intCol) {
        this.intCol = intCol;
    }

    public long getLongCol() {
        return realmGetter$longCol();
    }

    public void setLongCol(long longCol) {
        realmSetter$longCol(longCol);
    }

    public long realmGetter$longCol() {
        return longCol;
    }

    public void realmSetter$longCol(long longCol) {
        this.longCol = longCol;
    }

    public float getFloatCol() {
        return realmGetter$floatCol();
    }

    public void setFloatCol(float floatCol) {
        realmSetter$floatCol(floatCol);
    }

    public float realmGetter$floatCol() {
        return floatCol;
    }

    public void realmSetter$floatCol(float floatCol) {
        this.floatCol = floatCol;
    }

    public double getDoubleCol() {
        return realmGetter$doubleCol();
    }

    public void setDoubleCol(double doubleCol) {
        realmSetter$doubleCol(doubleCol);
    }

    public double realmGetter$doubleCol() {
        return doubleCol;
    }

    public void realmSetter$doubleCol(double doubleCol) {
        this.doubleCol = doubleCol;
    }

    public byte[] getByteCol() {
        return realmGetter$byteCol();
    }

    public void setByteCol(byte[] byteCol) {
        realmSetter$byteCol(byteCol);
    }

    public byte[] realmGetter$byteCol() {
        return byteCol;
    }

    public void realmSetter$byteCol(byte[] byteCol) {
        this.byteCol = byteCol;
    }

    public String getStringCol() {
        return realmGetter$stringCol();
    }

    public void setStringCol(String stringCol) {
        realmSetter$stringCol(stringCol);
    }

    public String realmGetter$stringCol() {
        return stringCol;
    }

    public void realmSetter$stringCol(String stringCol) {
        this.stringCol = stringCol;
    }

    public Date getDateCol() {
        return realmGetter$dateCol();
    }

    public void setDateCol(Date dateCol) {
        realmSetter$dateCol(dateCol);
    }

    public Date realmGetter$dateCol() {
        return dateCol;
    }

    public void realmSetter$dateCol(Date dateCol) {
        this.dateCol = dateCol;
    }

    public IOSChild getChild() {
        return realmGetter$child();
    }

    public void setChild(IOSChild child) {
        realmSetter$child(child);
    }

    public IOSChild realmGetter$child() {
        return child;
    }

    public void realmSetter$child(IOSChild child) {
        this.child = child;
    }

    public RealmList<IOSChild> getChildren() {
        return realmGetter$children();
    }

    public void setChildren(RealmList<IOSChild> children) {
        realmSetter$children(children);
    }

    public RealmList<IOSChild> realmGetter$children() {
        return children;
    }

    public void realmSetter$children(RealmList<IOSChild> children) {
        this.children = children;
    }
}
