package io.realm.transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;



public class UrlEncodedAnalytics {

    private String prefix;
    private String suffix;

    public UrlEncodedAnalytics(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public void execute(RealmAnalytics analytics) {
        try {
            URL url = getUrl(analytics);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            connection.getResponseCode();
        } catch (Exception ignored) {
        }
    }

    private URL getUrl(RealmAnalytics analytics) throws
            MalformedURLException,
            SocketException,
            NoSuchAlgorithmException,
            UnsupportedEncodingException {
        return new URL(prefix + Utils.base64Encode(analytics.generateJson()) + suffix);
    }

    public static class MixPanel extends UrlEncodedAnalytics {
        private static final String ADDRESS_PREFIX = "https://api.mixpanel.com/track/?data=";
        private static final String ADDRESS_SUFFIX = "&ip=1";

        public MixPanel() {
            super(ADDRESS_PREFIX, ADDRESS_SUFFIX);
        }
    }

    public static class Segment extends UrlEncodedAnalytics {
        private static final String ADDRESS_PREFIX =
                "https://webhooks.mongodb-realm.com/api/client/v2.0/app/realmsdkmetrics-zmhtm/service/metric_webhook/incoming_webhook/metric?data=";
        private static final String ADDRESS_SUFFIX = "";

        public Segment() {
            super(ADDRESS_PREFIX, ADDRESS_SUFFIX);
        }
    }

}
