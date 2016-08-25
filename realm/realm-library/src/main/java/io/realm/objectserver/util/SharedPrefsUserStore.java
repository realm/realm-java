package io.realm.objectserver.util;

import android.content.Context;
import android.content.SharedPreferences;

import io.realm.objectserver.User;

/**
 * A User Store backed by a SharedPreferences file.
 */
public class SharedPrefsUserStore implements UserStore {

    private final SharedPreferences sp;
    private User currentUser;

    public SharedPrefsUserStore(Context context) {
        sp = context.getSharedPreferences("realm_object_server_users", Context.MODE_PRIVATE);
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public void save(String key, User user) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, user.toJson());
        editor.apply();
    }

    @Override
    public void saveAsync(String key, User user) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void saveASync(String key, User user, Callback callback) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public User load(String key) {
        String userData = sp.getString(key, "");
        if (userData.equals("")) {
            return null;
        }
        return User.fromJson(userData);
    }

    @Override
    public void loadAsync(String key, Callback callback) {
        throw new UnsupportedOperationException("TODO");
    }
}
