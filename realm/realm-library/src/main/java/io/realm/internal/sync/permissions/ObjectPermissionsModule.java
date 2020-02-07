package io.realm.internal.sync.permissions;

import io.realm.annotations.RealmModule;
import io.realm.sync.Subscription;

/**
 * Realm model classes that are always part of Query-based Realms
 */
@RealmModule(library = true, classes = {
        Subscription.class
})
public class ObjectPermissionsModule {
}
