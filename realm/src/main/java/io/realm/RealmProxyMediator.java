package io.realm;

import java.util.List;

import io.realm.internal.ImplicitTransaction;
import io.realm.internal.Table;

/**
 * Interface for the ProxyMediator class. Should contain all static methods introduced by the
 * RealmProxy annotation processor.
 */
interface RealmProxyMediator {

    public static final String APT_NOT_EXECUTED_MESSAGE = "Annotation processor may not have been executed.";

    /**
     * Creates the backing table in Realm for the given model class.
     *
     * @param clazz         RealmObject model class to create backing table for.
     * @param transaction   Read transaction for the Realm to create table in.
     */
    public Table createTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction);

    /**
     * Validate the backing table in Realm for the given model class.
     *
     * @param clazz         RealmObject model class to validate.
     * @param transaction   Read transaction for the Realm to validate against.
     */
    public void validateTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction);

    /**
     * Returns a non-obfuscated list of fields in the model class that should be known by Realm.
     *
     * @param clazz  RealmObject model class reference.
     * @return The simple name of an model class (before it has been obfuscated)
     */
    public List<String> getFieldNames(Class<? extends RealmObject> clazz);

    /**
     * Returns the non-obfuscated simple name of an model class. This is used by Realm to name the
     * internal tables.
     *
     * @param clazz  RealmObject model class reference.
     * @return The simple name of an model class (before it has been obfuscated)
     */
    public String getClassModelName(Class<? extends RealmObject> clazz);

    /**
     * Creates a new instance of an RealmProxy for the given model class.
     *
     * @param clazz RealmObject to create RealmProxy for.
     * @return Created RealmProxy object.
     */
    public <E extends RealmObject> E newInstance(Class<E> clazz);

    /**
     * Returns the list of model classes that Realm supports in this application.
     *
     * @return List of class references to model classes. Empty list if no models are supported.
     */
    public List<Class<? extends RealmObject>> getModelClasses();
}
