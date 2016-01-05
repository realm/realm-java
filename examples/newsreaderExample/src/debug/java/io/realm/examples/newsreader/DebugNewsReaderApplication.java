package io.realm.examples.newsreader;

import timber.log.Timber;

/**
 * Debug specific application setup.
 */
public class DebugNewsReaderApplication extends NewsReaderApplication {
    @Override
    protected void initializeTimber() {
        Timber.plant(new Timber.DebugTree());
    }
}
