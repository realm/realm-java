package io.realm.entities.pojo;

import io.realm.RealmList;
import io.realm.annotations.RealmClass;
import io.realm.entities.MyCustomInterface;

@RealmClass
public class RealmModelWithCustomInterface extends MyCustomInterface {
    public String name;
//    public RealmList<MyCustomInterface> list;
    public MyCustomInterface obj;
}