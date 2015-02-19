package io.realm.internal;

import android.util.JsonReader;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;

/**
 * Interface for classes capable of adding JSON data to both mananged and non-managed RealmObjects.
 */
public interface RealmJson {
    public <E extends RealmObject> void populateUsingJsonObject(E obj, JSONObject json) throws JSONException;
    public <E extends RealmObject> void populateUsingJsonStream(E obj, JsonReader reader) throws IOException;
}