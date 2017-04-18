package io.realm.objectserver.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.realm.SyncManager;

public class TestHelper {
    private final static Method SYNC_MANAGER_RESET_METHOD;
    static {
        try {
            SYNC_MANAGER_RESET_METHOD = SyncManager.class.getDeclaredMethod("reset");
            SYNC_MANAGER_RESET_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public static void resetSyncMetadata() {
        try {
            SYNC_MANAGER_RESET_METHOD.invoke(null);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}
