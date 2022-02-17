-dontnote android.security.KeyStore
-dontwarn okio.Okio
-dontwarn okio.DeflaterSink

-dontnote com.android.org.conscrypt.SSLParametersImpl
-dontnote org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontnote sun.security.ssl.SSLContextImpl

# See https://github.com/square/okhttp/issues/3922
-dontwarn okhttp3.internal.platform.*

# Referenced from JNI
-keep class io.realm.internal.objectstore.OsJavaNetworkTransport$Response {
    int getHttpResponseCode();
    int getCustomResponseCode();
    java.lang.String[] getJNIFriendlyHeaders();
    java.lang.String getBody();
}

-keep class io.realm.internal.objectstore.OsJavaNetworkTransport$Request {
    <init>(...);
}

-keep class io.realm.internal.OsSharedRealm$SchemaChangedCallback {
    void onSchemaChanged();
}

-keep class io.realm.internal.objectstore.OsApp {
    io.realm.internal.objectstore.OsJavaNetworkTransport getNetworkTransport();
}
