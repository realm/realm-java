package io.realm;


import android.util.JsonReader;
import io.realm.exceptions.RealmException;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import some.test.AllTypes;

@io.realm.annotations.internal.RealmModule
class DefaultRealmModuleMediator
        implements RealmProxyMediator {

    private static final List<Class<? extends RealmObject>> MODEL_CLASSES;
    static {
        List<Class<? extends RealmObject>> modelClasses = new ArrayList<Class<? extends RealmObject>>();
        modelClasses.add(AllTypes.class);
        MODEL_CLASSES = Collections.unmodifiableList(modelClasses);
    }

    @Override
    public Table createTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction) {
        if (clazz == null) {
            throw new NullPointerException("A class extending RealmObject must be provided");
        }

        if (clazz.equals(AllTypes.class)) {
            return AllTypesRealmProxy.initTable(transaction);
        } else {
            throw new RealmException("Could not find the generated proxy class for " + clazz + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
        }
    }

    @Override
    public void validateTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction) {
        if (clazz == null) {
            throw new NullPointerException("A class extending RealmObject must be provided");
        }

        if (clazz.equals(AllTypes.class)) {
            AllTypesRealmProxy.validateTable(transaction);
        } else {
            throw new RealmException("Could not find the generated proxy class for " + clazz + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
        }
    }

    @Override
    public List<String> getFieldNames(Class<? extends RealmObject> clazz) {
        if (clazz == null) {
            throw new NullPointerException("A class extending RealmObject must be provided");
        }

        if (clazz.equals(AllTypes.class)) {
            return AllTypesRealmProxy.getFieldNames();
        } else {
            throw new RealmException("Could not find the generated proxy class for " + clazz + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
        }
    }

    @Override
    public String getTableName(Class<? extends RealmObject> clazz) {
        if (clazz == null) {
            throw new NullPointerException("A class extending RealmObject must be provided");
        }

        if (clazz.equals(AllTypes.class)) {
            return AllTypesRealmProxy.getTableName();
        } else {
            throw new RealmException("Could not find the generated proxy class for " + clazz + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
        }
    }

    @Override
    public <E extends RealmObject> E newInstance(Class<E> clazz) {
        if (clazz == null) {
            throw new NullPointerException("A class extending RealmObject must be provided");
        }

        if (clazz.equals(AllTypes.class)) {
            return (E) new AllTypesRealmProxy();
        } else {
            throw new RealmException("Could not find the generated proxy class for " + clazz + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
        }
    }

    @Override
    public List<Class<? extends RealmObject>> getModelClasses() {
        return MODEL_CLASSES;
    }

    @Override
    public Map<String, Long> getColumnIndices(Class<? extends RealmObject> clazz) {
        if (clazz.equals(AllTypes.class)) {
            return AllTypesRealmProxy.getColumnIndices();
        } else {
            throw new RealmException("Could not find the generated proxy class for " + clazz + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
        }
    }

    @Override
    public <E extends RealmObject> E copyOrUpdate(Realm realm, E obj, boolean update, Map<RealmObject, RealmObjectProxy> cache) {
        Class<E> clazz = (Class<E>) ((obj instanceof RealmObjectProxy) ? obj.getClass().getSuperclass() : obj.getClass());

        if (clazz.equals(AllTypes.class)) {
            return (E) AllTypesRealmProxy.copyOrUpdate(realm, (AllTypes) obj, update, cache);
        } else {
            throw new RealmException("Could not find the generated proxy class for " + clazz + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
        }
    }

    @Override
    public <E extends RealmObject> E createOrUpdateUsingJsonObject(Class<E> clazz, Realm realm, JSONObject json, boolean update)
            throws JSONException {
        if (clazz == null) {
            throw new NullPointerException("A class extending RealmObject must be provided");
        }

        if (clazz.equals(AllTypes.class)) {
            return (E) AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json, update);
        } else {
            throw new RealmException("Could not find the generated proxy class for " + clazz + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
        }
    }

    @Override
    public <E extends RealmObject> E createUsingJsonStream(Class<E> clazz, Realm realm, JsonReader reader)
            throws IOException {
        if (clazz == null) {
            throw new NullPointerException("A class extending RealmObject must be provided");
        }

        if (clazz.equals(AllTypes.class)) {
            return (E) AllTypesRealmProxy.createUsingJsonStream(realm, reader);
        } else {
            throw new RealmException("Could not find the generated proxy class for " + clazz + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
        }
    }

}