package io.realm.internal.objectserver.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is responsible for keeping track of system events related to the network so it can delegate them to
 * interested parties.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    private static List<ConnectionListener> listeners = new CopyOnWriteArrayList<ConnectionListener>();

    /**
     * Add a listener to be notified about any network changes.
     * This method is thread safe.
     *
     * IMPORTANT: Not removing it again will result in major leaks.
     */
    public static void addListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a network listener.
     * This method is thread safe.
     */
    public static synchronized void removeListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Try to detect if a device is online and can transmit or receive data.
     * This method is thread safe.
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }


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