package io.realm.internal.sync.permissions;

import io.realm.annotations.RealmModule;
import io.realm.sync.Subscription;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.Permission;
import io.realm.sync.permissions.RealmPermissions;
import io.realm.sync.permissions.PermissionUser;
import io.realm.sync.permissions.Role;

/**
 * Realm model classses that are always part of Query-based Realms
 */
@RealmModule(library = true, classes = {
        ClassPermissions.class,
        Permission.class,
        RealmPermissions.class,
        Role.class,
        PermissionUser.class,
        Subscription.class
})
public class ObjectPermissionsModule {
}
