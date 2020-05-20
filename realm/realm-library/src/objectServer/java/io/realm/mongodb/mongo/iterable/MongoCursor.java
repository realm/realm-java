package io.realm.mongodb.mongo.iterable;

import com.google.android.gms.tasks.Task;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import io.realm.internal.common.TaskDispatcher;

/**
 * The Mongo Cursor interface.
 * An application should ensure that a cursor is closed in all circumstances, e.g. using a
 * try-with-resources statement.
 *
 * @param <ResultT> The type of documents the cursor contains
 */
public class MongoCursor<ResultT> implements Closeable {

    private final TaskDispatcher dispatcher;
    private final Iterator<ResultT> documents;

    public MongoCursor(TaskDispatcher dispatcher, Iterator<ResultT> documents) {
        this.dispatcher = dispatcher;
        this.documents = documents;
    }

    /**
     * Returns whether or not there is a next document to retrieve with {@code next()}.
     *
     * @return A {@link Task} containing whether or not there is a next document to
     * retrieve with {@code next()}.
     */
    Task<Boolean> hasNext() {
        return dispatcher.dispatchTask(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return documents.hasNext();
            }
        });
    }

    /**
     * Returns the next document.
     *
     * @return A {@link Task} containing the next document if available or a failed task with
     * a {@link NoSuchElementException } exception.
     */
    Task<ResultT> next() {
        return dispatcher.dispatchTask(new Callable<ResultT>() {
            @Override
            public ResultT call() {
                return documents.next();
            }
        });
    }

    /**
     * A special {@code next()} case that returns the next document if available or null.
     *
     * @return A {@link Task} containing the next document if available or null.
     */
    Task<ResultT> tryNext() {
        return dispatcher.dispatchTask(new Callable<ResultT>() {
            @Override
            public ResultT call() {
                if (!documents.hasNext()) {
                    return null;
                }
                return documents.next();
            }
        });
    }

    @Override
    public void close() throws IOException {

    }
}
