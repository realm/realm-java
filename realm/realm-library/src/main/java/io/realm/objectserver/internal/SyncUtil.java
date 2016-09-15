package io.realm.objectserver.internal;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Helper class for Object Server classes.
 */
public class SyncUtil {

    /**
     * Fully resolve an URL so all placeholder objects are replaced with the user identity.
     */
    public static URI getFullServerUrl(URI serverUrl, String userIdentity) {
        try {
            return new URI(serverUrl.toString().replace("/~/", "/" + userIdentity + "/"));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not replace '/~/' with a valid user ID.", e);
        }
    }
}
