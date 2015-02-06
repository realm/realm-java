package io.realm.entities;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import some.test.AllTypes;

public class NoAccessors extends RealmObject {
    private String columnString;
    private long columnLong;
    private float columnFloat;
    private double columnDouble;
    private boolean columnBoolean;
    private Date columnDate;
    private byte[] columnBinary;
    private AllTypes columnRealmObject;
    private RealmList<AllTypes> columnRealmList;
}
