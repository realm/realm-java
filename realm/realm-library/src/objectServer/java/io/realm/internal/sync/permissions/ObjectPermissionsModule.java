package io.realm.internal.sync.permissions;

import io.realm.annotations.RealmModule;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.RealmPermission;
import io.realm.sync.permissions.RealmPermissions;
import io.realm.sync.permissions.Role;
import io.realm.sync.permissions.User;

@RealmModule(library = true, classes = {
        ClassPermissions.class,
        RealmPermission.class,
        RealmPermissions.class,
        Role.class,
        User.class
})
public class ObjectPermissionsModule {
}
