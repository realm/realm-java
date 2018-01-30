package io.realm.internal.sync.permissions;

import io.realm.annotations.RealmModule;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.Permission;
import io.realm.sync.permissions.RealmPermissions;
import io.realm.sync.permissions.Role;
import io.realm.sync.permissions.User;

@RealmModule(classes = {
        ClassPermissions.class,
        Permission.class,
        RealmPermissions.class,
        Role.class,
        User.class
})
public class ObjectPermissionsModule {
}
