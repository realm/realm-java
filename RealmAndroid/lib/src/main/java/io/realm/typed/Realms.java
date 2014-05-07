package io.realm.typed;

import android.content.Context;

public class Realms {


    public static <T> Realm<T> newList(Context context, Class<T> type) {
        return new Realm<T>(type, context);
    }

}
