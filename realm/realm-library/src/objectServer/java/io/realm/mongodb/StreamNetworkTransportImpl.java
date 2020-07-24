package io.realm.mongodb;

import org.bson.BsonArray;

import java.io.IOException;

import io.realm.internal.network.StreamNetworkTransport;
import io.realm.internal.objectstore.OsJavaNetworkTransport;

public class StreamNetworkTransportImpl extends StreamNetworkTransport {
    private final App app;
    private final User user;

    public StreamNetworkTransportImpl(App app, User user) {
        this.app = app;
        this.user = user;
    }

    @Override
    public OsJavaNetworkTransport.Request makeStreamingRequest(String functionName, BsonArray bsonArgs, String serviceName) {
        return app.makeStreamingRequest(user, functionName, bsonArgs, serviceName);
    }

    @Override
    public OsJavaNetworkTransport.Response sendRequest(OsJavaNetworkTransport.Request request) throws IOException {
        return app.networkTransport.sendStreamingRequest(request);
    }
}
