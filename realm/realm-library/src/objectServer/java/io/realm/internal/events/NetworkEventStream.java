package io.realm.internal.events;

import org.bson.codecs.configuration.CodecRegistry;

import java.io.IOException;

import io.realm.internal.objectserver.EventStream;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsWatchStream;
import io.realm.mongodb.AppException;
import io.realm.mongodb.mongo.events.BaseChangeEvent;

public class NetworkEventStream<T> implements EventStream<T> {
    private final OsJavaNetworkTransport.Response response;
    private final OsWatchStream watchStream;
    private final CodecRegistry codecRegistry;
    private final Class<T> documentClass;

    public NetworkEventStream(OsJavaNetworkTransport.Response response, CodecRegistry codecRegistry, Class<T> documentClass) {
        this.response = response;
        this.watchStream = new OsWatchStream(codecRegistry);
        this.codecRegistry = codecRegistry;
        this.documentClass = documentClass;
    }

    /**
     * Fetch the next event from a given stream
     *
     * @return the next event
     * @throws AppException on a stream error
     */
    @Override
    public BaseChangeEvent<T> getNextEvent() throws AppException, IOException {
        while (true) {
            watchStream.feedLine(response.readBodyLine());
            String watchStreamState = watchStream.getState();

            if (watchStreamState.equals(OsWatchStream.HAVE_EVENT)) {
                return ChangeEvent.fromBsonDocument(watchStream.getNextEvent(), documentClass, codecRegistry);
            }
            if (watchStreamState.equals(OsWatchStream.HAVE_ERROR)) {
                response.close();
                throw watchStream.getError();
            }
        }
    }

    /**
     * Closes the current stream.
     * <p>
     * Note: we use a close flag because the underlaying input stream might not be thread safe.
     *
     * @see <a href="http://google.com">https://github.com/square/okio/issues/163#issuecomment-127052956</a>
     */
    @Override
    public void close() {
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
