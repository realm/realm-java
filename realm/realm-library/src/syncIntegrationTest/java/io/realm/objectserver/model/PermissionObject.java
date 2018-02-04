package io.realm.objectserver.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import io.realm.sync.permissions.RealmPermission;

public class PermissionObject extends RealmObject {
    @PrimaryKey
    @Required
    private String name;
    private RealmList<RealmPermission> permissions = new RealmList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<RealmPermission> getPermissions() {
        return permissions;
    }
}
