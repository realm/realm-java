/*
 * Copyright 2016 Realm Inc.
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

package io.realm.internal.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.SyncManager;
import io.realm.internal.Util;

/**
 * This class is responsible for keeping track of system events related to the network so it can delegate them to
 * interested parties.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    private static List<ConnectionListener> listeners = new CopyOnWriteArrayList<ConnectionListener>();

    /**
     * Add a listener to be notified about any network changes.
     * This method is thread safe.
     * <p>
     * IMPORTANT: Not removing it again will result in major leaks.
     *
     * @param listener the listener.
     */
    public static void addListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a network listener.
     * This method is thread safe.
     *
     * @param listener the listener.
     */
    public static synchronized void removeListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Attempt to detect if a device is online and can transmit or receive data.
     * This method is thread safe.
     * <p>
     * An emulator is always considered online, as `getActiveNetworkInfo()` does not report the correct value.
     *
     * @param context an Android context.
     * @return {@code true} if device is online, otherwise {@code false}.
     */
    public static boolean isOnline(Context context) {
        if (SyncManager.Debug.skipOnlineChecking) {
            return true;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return ((networkInfo != null && networkInfo.isConnectedOrConnecting()) || Util.isEmulator());
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = isOnline(context);
        for (ConnectionListener listener : listeners) {
            listener.onChange(connected);
        }
    }

    public interface ConnectionListener {
        void onChange(boolean connectionAvailable);
    }
}
