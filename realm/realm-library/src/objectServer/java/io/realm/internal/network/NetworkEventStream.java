package io.realm.internal.network;

import org.bson.codecs.configuration.CodecRegistry;

import java.io.IOException;

import io.realm.internal.objectserver.EventStream;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsWatchStream;

public class NetworkEventStream<T> implements EventStream<T> {
    private final OsJavaNetworkTransport.Response response;
    private final OsWatchStream<T> watchStream;

    public NetworkEventStream(OsJavaNetworkTransport.Response response, CodecRegistry codecRegistry, Class<T> documentClass) {
        this.response = response;
        this.watchStream = new OsWatchStream<>(codecRegistry, documentClass);
    }

    /**
     * Fetch the next event from a given stream
     *
     * @return the next event
     * @throws IOException any io exception that could occur
     */
    @Override
    public T getNextEvent() throws IOException {
        String line;

        while ((line = response.readBodyLine()) != null) {
            watchStream.feedLine(line);

            if (watchStream.getState().equals(OsWatchStream.HAVE_EVENT))
                return watchStream.getNextEvent();
            if (watchStream.getState().equals(OsWatchStream.HAVE_ERROR)) {
                response.close();
                throw new IllegalStateException("Watch stream has error");
            }
        }

        return null;
    }

    /**
     * Closes the current stream.
     *
     * @throws IOException can throw exception if internal buffer not closed properly
     */
    @Override
    public void close() throws IOException {
        response.close();
    }

    /**
     * Indicates whether or not the change stream is currently open.
     *
     * @return True if the underlying change stream is open.
     */
    @Override
    public boolean isOpen() {
        return response.isOpen();
    }
}
