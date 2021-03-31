/*
 * Copyright 2020 Realm Inc.
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

package io.realm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import io.realm.internal.OsObjectStore;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;
import io.realm.internal.Util;


public class CollectionUtils {
    public static final String LIST_TYPE = "list";
    public static final String DICTIONARY_TYPE = "dictionary";
    public static final String SET_TYPE = "set";

    static boolean isClassForRealmModel(Class<?> clazz) {
        return RealmModel.class.isAssignableFrom(clazz);
    }

    /**
     * Called by both list and set operators to determine whether a RealmModel is an embedded object
     *
     * @param realm  the Realm instance to check against.
     * @param object the object to check.
     * @return true if the object can be copied, false otherwise
     */
    static boolean isEmbedded(BaseRealm realm, RealmModel object) {
        if (realm instanceof Realm) {
            return realm.getSchema().getSchemaForClass(object.getClass()).isEmbedded();
        } else {
            String objectType = ((DynamicRealmObject) object).getType();
            return realm.getSchema().getSchemaForClass(objectType).isEmbedded();
        }
    }

    /**
     * Called by both list and dictionary operators to determine whether a RealmModel can be copied
     * to a Realm.
     *
     * @param realm          the Realm instance to check against.
     * @param object         the object to copy.
     * @param className      the object class.
     * @param collectionType the type of the calling collection.
     * @return true if the object can be copied, false otherwise
     */
    static boolean checkCanObjectBeCopied(BaseRealm realm, RealmModel object, String className, String collectionType) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;

            if (proxy instanceof DynamicRealmObject) {
                if (proxy.realmGet$proxyState().getRealm$realm() == realm) {
                    String objectClassName = ((DynamicRealmObject) object).getType();
                    if (className.equals(objectClassName)) {
                        // Same Realm instance and same target table
                        return false;
                    } else {
                        // Different target table
                        throw new IllegalArgumentException(String.format(Locale.US,
                                "The object has a different type from %s's." +
                                        " Type of the %s is '%s', type of object is '%s'.", collectionType, collectionType, className, objectClassName));
                    }
                } else if (realm.threadId == proxy.realmGet$proxyState().getRealm$realm().threadId) {
                    // We don't support moving DynamicRealmObjects across Realms automatically. The overhead is too big as
                    // you have to run a full schema validation for each object.
                    // And copying from another Realm instance pointed to the same Realm file is not supported as well.
                    throw new IllegalArgumentException("Cannot pass DynamicRealmObject between Realm instances.");
                } else {
                    throw new IllegalStateException("Cannot pass an object to a Realm instance created in another thread.");
                }
            } else {
                // Object is already in this realm
                if (proxy.realmGet$proxyState().getRow$realm() != null && proxy.realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    if (realm != proxy.realmGet$proxyState().getRealm$realm()) {
                        throw new IllegalArgumentException("Cannot pass an object from another Realm instance.");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Called by both list and dictionary operators to determine whether a Mixed instance contains
     * a RealmModel in it and, if so, copy it to the provided Realm or not. This method acts as a
     * pass-through in case the Mixed instance contains a Realm primitive.
     *
     * @param realm the Realm instance to check against and to which the object inside the Mixed
     *              instance will be copied if needed.
     * @param mixed the Mixed instance containing the RealmModel to copy to Realm, if needed.
     * @return the Mixed instance that may or may not be copied
     */
    @SuppressWarnings("unchecked")
    static Mixed copyToRealmIfNeeded(BaseRealm realm, Mixed mixed) {
        if (mixed.getType() == MixedType.OBJECT) {
            Class<? extends RealmModel> objectClass = (Class<? extends RealmModel>) mixed.getValueClass();
            RealmModel object = mixed.asRealmModel(objectClass);

            if (object instanceof RealmObjectProxy) {
                RealmObjectProxy proxy = (RealmObjectProxy) object;
                if (proxy instanceof DynamicRealmObject) {
                    if (proxy.realmGet$proxyState().getRealm$realm() == realm) {
                        return mixed;
                    } else if (realm.threadId == proxy.realmGet$proxyState().getRealm$realm().threadId) {
                        // We don't support moving DynamicRealmObjects across Realms automatically. The overhead is too big as
                        // you have to run a full schema validation for each object.
                        // And copying from another Realm instance pointed to the same Realm file is not supported as well.
                        throw new IllegalArgumentException("Cannot copy DynamicRealmObject between Realm instances.");
                    } else {
                        throw new IllegalStateException("Cannot copy an object to a Realm instance created in another thread.");
                    }
                } else {
                    if (realm.getSchema().getSchemaForClass(objectClass).isEmbedded()) {
                        throw new IllegalArgumentException("Embedded objects are not supported by Mixed.");
                    }

                    // Object is already in this realm
                    if ((proxy.realmGet$proxyState().getRow$realm() != null) && proxy.realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                        if (realm != proxy.realmGet$proxyState().getRealm$realm()) {
                            throw new IllegalArgumentException("Cannot copy an object from another Realm instance.");
                        }
                        return mixed;
                    }
                }
            }

            return Mixed.valueOf(copyToRealm(realm, object));
        }

        return mixed;
    }

    /**
     * Called by both list and dictionary operators to copy a RealmModel to Realm in case it has
     * been deemed necessary.
     *
     * @param baseRealm The Realm instance to copy the object to.
     * @param object    The object to copy.
     * @param <E>       The RealmModel type.
     * @return the copied object
     */
    public static <E extends RealmModel> E copyToRealm(BaseRealm baseRealm, E object) {
        // At this point the object can only be a typed object, so the backing Realm cannot be a DynamicRealm.
        Realm realm = (Realm) baseRealm;
        String simpleClassName = realm.getConfiguration().getSchemaMediator().getSimpleClassName(object.getClass());
        if (OsObjectStore.getPrimaryKeyForObject(realm.getSharedRealm(), simpleClassName) != null) {
            return realm.copyToRealmOrUpdate(object);
        } else {
            return realm.copyToRealm(object);
        }
    }

    /**
     * Called by both list and dictionary operators to copy a RealmModel to Realm in case it has
     * been deemed necessary.
     *
     * @param baseRealm The Realm instance to copy the object to.
     * @param objects   collection of object to copy.
     * @param <E>       The RealmModel type.
     * @return the copied object
     */
    public static <E extends RealmModel> Collection<E> copyToRealm(BaseRealm baseRealm, Collection<E> objects) {
        // At this point the object can only be a typed object, so the backing Realm cannot be a DynamicRealm.
        Realm realm = (Realm) baseRealm;
        Collection<E> managedObjects = new ArrayList<>();
        for (E object : objects) {
            String simpleClassName = realm.getConfiguration().getSchemaMediator().getSimpleClassName(object.getClass());
            if (OsObjectStore.getPrimaryKeyForObject(realm.getSharedRealm(), simpleClassName) != null) {
                managedObjects.add(realm.copyToRealmOrUpdate(object));
            } else {
                managedObjects.add(realm.copyToRealm(object));
            }
        }

        return managedObjects;
    }

    /**
     * Used to update an embedded object internally after its row has been created.
     *
     * @param realm      The Realm instance used to create the object.
     * @param realmModel the model that will be used to update the object.
     * @param objKey     the object key.
     */
    static void updateEmbeddedObject(Realm realm, RealmModel realmModel, long objKey) {
        RealmProxyMediator schemaMediator = realm.getConfiguration().getSchemaMediator();
        Class<? extends RealmModel> modelClass = Util.getOriginalModelClass(realmModel.getClass());
        Table table = realm.getTable(modelClass);
        RealmModel managedObject = schemaMediator.newInstance(modelClass, realm, table.getUncheckedRow(objKey), realm.getSchema().getColumnInfo(modelClass), true, Collections.EMPTY_LIST);
        schemaMediator.updateEmbeddedObject(realm, realmModel, managedObject, new HashMap<>(), Collections.EMPTY_SET);
    }
}
