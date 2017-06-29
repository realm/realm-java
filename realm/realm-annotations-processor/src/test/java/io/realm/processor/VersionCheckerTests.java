
package io.realm.processor;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class VersionCheckerTests {

    @Test
    public void ignoreNamedReleases() {
        String[] newReleases = {
                "1.0.0-BETA",
                "1.0.0-beta",
                "foo-BETA",
                "STABLE-1.0.0",
                "1.alpha.0"
        };

        for (String version : newReleases) {
            assertFalse(version + " failed", RealmVersionChecker.reportNewVersion(version, "0.9.0"));
        }
    }

    @Test
    public void ignoreInvalidVersions() {
        String[] invalidVersions = {
                null,
                ""
        };

        for (String version : invalidVersions) {
            assertFalse(version + " failed", RealmVersionChecker.reportNewVersion(version, "1.0.0"));
        }
    }

    @Test
    public void ignoreSameVersion() {
        assertFalse(RealmVersionChecker.reportNewVersion("1.0.0", "1.0.0"));
    }

    @Test
    public void notifyForDifferentVersion() {
        String[] validVersions = {
                "0.9.0",
                "1.1.0",
                "999.999.999"
        };

        for (String version : validVersions) {
            assertTrue(version + " failed", RealmVersionChecker.reportNewVersion(version, "1.0.0"));
        }
    }

}
