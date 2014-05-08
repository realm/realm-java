package io.realm.typed;

import android.content.Context;

public class Realms {


    public static <T> RealmList<T> list(Context context, Class<T> type) {
        return new RealmList<T>(type, context);
    }

}
