package io.realm.typed;

import android.content.Context;

public class Realms {

    private Realms() {}

    /**
     * Returns a RealmList which is backed by the default realm, it might already contain data.
     *
     * @param context       The Context from which the filesystem path is derived
     * @param type          The definition of the object which is to be accesed
     * @param <E>           The type which should be accessible from this list
     * @return              A list backed by Realm
     */
    public static <E> RealmList<E> list(Context context, Class<E> type) {
        return new RealmList<E>(type, context);
    }

}
