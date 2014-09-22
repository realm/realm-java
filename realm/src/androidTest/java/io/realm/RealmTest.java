package io.realm;

import android.test.AndroidTestCase;

public class RealmTest extends AndroidTestCase {

    public void testRealmThreadCachingSpeed() {
        long tic1 = System.currentTimeMillis();
        Realm realm1 = Realm.create(this.getContext());
        long toc1 = System.currentTimeMillis();
        long t1 = toc1 - tic1;

        long tic2 = System.currentTimeMillis();
        Realm realm2 = Realm.create(this.getContext());
        long toc2 = System.currentTimeMillis();
        long t2 = toc2 - tic2;

        // At least 5 times faster?
        assertTrue(t2 < (t1 / 5));
    }
}
