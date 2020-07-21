package io.realm.mongodb.mongo;

import org.bson.BsonArray;

import java.io.IOException;

import io.realm.internal.objectstore.OsJavaNetworkTransport;

public abstract class StreamNetworkTransport {
    protected abstract OsJavaNetworkTransport.Request makeStreamingRequest(String functionName, BsonArray bsonArgs, String serviceName);

    public abstract OsJavaNetworkTransport.Response sendRequest(OsJavaNetworkTransport.Request request) throws IOException;
}
