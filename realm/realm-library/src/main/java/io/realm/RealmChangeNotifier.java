package io.realm;

import java.io.Closeable;

import io.realm.internal.SharedGroup;

class RealmChangeNotifier implements Closeable {

    public interface Callback {
        void onChanged(RealmConfiguration configuration);
    }

    private final RealmConfiguration configuration;
    private final SharedGroup sharedGroup;
    private final Callback callback;
    private final Thread notifierThread;

    public RealmChangeNotifier(final RealmConfiguration configuration, Callback onChangedCallback) {
        this.configuration = configuration;
        this.sharedGroup = new SharedGroup(
                configuration.getPath(),
                SharedGroup.IMPLICIT_TRANSACTION,
                configuration.getDurability(),
                configuration.getEncryptionKey());
        this.callback = onChangedCallback;

        notifierThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (sharedGroup.waitForChange()) {
                    callback.onChanged(configuration);
                    sharedGroup.beginRead();
                    sharedGroup.endRead();
                }
                sharedGroup.close();
            }
        });
        notifierThread.start();
    }

    @Override
    public void close() {
        sharedGroup.setWaitForChangeEnabled(false);
        try {
            notifierThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
