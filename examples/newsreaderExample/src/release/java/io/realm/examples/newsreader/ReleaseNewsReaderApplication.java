package io.realm.examples.newsreader;

/**
 * Release specific application setup.
 */
public class ReleaseNewsReaderApplication extends NewsReaderApplication {
    @Override
    protected void initializeTimber() {
        // No logging in Release mode.
    }
}
