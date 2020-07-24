/*
 * Copyright 2020 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal.async;

import org.bson.codecs.configuration.CodecRegistry;

import java.io.IOException;

import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsWatchStream;

public class EventStream<T> {
    private final OsJavaNetworkTransport.Response response;
    private final OsWatchStream<T> watchStream;

    public EventStream(OsJavaNetworkTransport.Response response, CodecRegistry codecRegistry, Class<T> documentClass) {
        this.response = response;
        this.watchStream = new OsWatchStream<>(codecRegistry, documentClass);
    }
    /**
     * Fetch the next event from a given stream
     * @return the next event
     * @throws IOException any io exception that could occur
     */
    public T getNextEvent() throws IOException {
        String line;

        while((line = response.readBodyLine()) != null) {
            watchStream.feedLine(line);

            if(watchStream.getState().equals(OsWatchStream.HAVE_EVENT))
                return watchStream.getNextEvent();
            if(watchStream.getState().equals(OsWatchStream.HAVE_ERROR)){
                throw new IllegalStateException("Watch stream has error");
            }
        }

        return null;
    }

    /**
     * Closes the current stream.
     * @throws IOException can throw exception if internal buffer not closed properly
     */
    public void close() throws IOException {
        response.close();
    }
}
