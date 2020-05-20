package io.realm.mongodb.mongo.iterable;

import com.google.android.gms.tasks.Task;

import java.util.Collection;
import java.util.Iterator;

import io.realm.internal.common.TaskDispatcher;

/**
 * The MongoIterable is the results from an operation, such as a query.
 *
 * @param <ResultT> The type that this iterable will decode documents to.
 */
abstract class MongoIterable<ResultT> {

    private final TaskDispatcher dispatcher;
    private final Iterator<ResultT> documents;

    MongoIterable(TaskDispatcher dispatcher, Iterator<ResultT> documents) {
        this.dispatcher = dispatcher;
        this.documents = documents;
    }

    /**
     * Returns a cursor of the operation represented by this iterable.
     *
     * @return a cursor of the operation represented by this iterable.
     */
    Task<MongoCursor<ResultT>> iterator() {
        return dispatcher.dispatchTask(() ->
                new MongoCursor<>(dispatcher, documents)
        );
    }

    /**
     * Helper to return the first item in the iterator or null.
     *
     * @return a task containing the first item or null.
     */
    Task<ResultT> first() {
        return dispatcher.dispatchTask(() -> {
            if (documents == null) {
                throw new NullPointerException("Documents cannot be null.");
            }
            return documents.hasNext() ? documents.next() : null;
        });
    }

    // FIXME: these two are a framework of its own - do we have to add them?
//  /**
//   * Maps this iterable from the source document type to the target document type.
//   *
//   * @param mapper a function that maps from the source to the target document type
//   * @param <U> the target document type
//   * @return an iterable which maps T to U
//   */
//  <U> MongoIterable<U> map(Function<ResultT, U> mapper);

//  /**
//   * Iterates over all documents in the view, applying the given block to each.
//   *
//   * <p>Similar to {@code map} but the function is fully encapsulated with no returned result.</p>
//   *
//   * @param block the block to apply to each document of type T.
//   * @return a task that completes when the iteration over all documents has completed.
//   */
//  Task<Void> forEach(Block<? super ResultT> block);

    /**
     * Iterates over all the documents, adding each to the given target.
     *
     * @param target the collection to insert into
     * @param <A>    the collection type
     * @return a task containing the target.
     */
//    <A extends Collection<? super ResultT>> Task<A> into(A target);
}
