package io.realm.internal.network;

import java.io.IOException;
import java.util.Map;

import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.mongodb.AppException;

public class MockableNetworkTransport extends OsJavaNetworkTransport {

    private final OsJavaNetworkTransport originalNetworkTransport;
    private OsJavaNetworkTransport networkTransport;

    public MockableNetworkTransport(OsJavaNetworkTransport networkTransport) {
        this.originalNetworkTransport = networkTransport;
        this.networkTransport = networkTransport;
    }

    public void setMockNetworkTransport(OsJavaNetworkTransport networkTransport) {
        if (networkTransport == null)
            this.networkTransport = this.originalNetworkTransport;
        else
            this.networkTransport = networkTransport;
    }

    @Override
    public void sendRequestAsync(String method, String url, long timeoutMs, Map<String, String> headers, String body, long completionBlockPtr) {
        this.networkTransport.sendRequestAsync(method, url, timeoutMs, headers, body, completionBlockPtr);
    }

    @Override
    public Response executeRequest(String method, String url, long timeoutMs, Map<String, String> headers, String body) {
        return this.networkTransport.executeRequest(method, url, timeoutMs, headers, body);
    }

    @Override
    public Response sendStreamingRequest(Request request) throws IOException, AppException {
        return this.networkTransport.sendStreamingRequest(request);
    }

    @Override
    public void setAuthorizationHeaderName(String headerName) {
        this.networkTransport.setAuthorizationHeaderName(headerName);
    }

    @Override
    public void addCustomRequestHeader(String headerName, String headerValue) {
        this.networkTransport.addCustomRequestHeader(headerName, headerValue);
    }

    @Override
    public void reset() {
        this.networkTransport.reset();
    }
}
