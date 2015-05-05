package io.realm.internal;

import android.util.JsonReader;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Interface for classes capable of adding JSON data to both mananged and non-managed RealmObjects.
 */
public interface RealmJson {
    public <E extends RealmObject> E createOrUpdateUsingJsonObject(Class<E> clazz, Realm realm, JSONObject json, boolean update) throws JSONException;
    public <E extends RealmObject> E createUsingJsonStream(Class<E> clazz, Realm realm, JsonReader reader) throws IOException;
}