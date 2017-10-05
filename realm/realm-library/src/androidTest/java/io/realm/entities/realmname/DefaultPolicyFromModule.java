package io.realm.entities.realmname;

import io.realm.RealmObject;

public class DefaultPolicyFromModule extends RealmObject {
    public String camelCase; // case formatter should be inherited from CustomRealmNamesModule
}
