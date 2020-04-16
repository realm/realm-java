package io.realm.mongodb;

import com.google.android.gms.tasks.Task;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;

import io.realm.mongodb.remote.RemoteCountOptions;
import io.realm.mongodb.remote.RemoteUpdateOptions;
import io.realm.mongodb.remote.internal.RemoteDeleteResult;
import io.realm.mongodb.remote.internal.RemoteInsertManyResult;
import io.realm.mongodb.remote.internal.RemoteInsertOneResult;
import io.realm.mongodb.remote.internal.RemoteUpdateResult;
import io.realm.mongodb.remote.internal.aggregate.RemoteAggregateIterable;
import io.realm.mongodb.remote.internal.find.RemoteFindIterable;

/**
 * The RemoteMongoCollection interface.
 *
 * @param <DocumentT> The type that this collection will encode documents from and decode documents
 *                    to.
 */
public final class RemoteMongoCollection<DocumentT> {

    /**
     * Gets the namespace of this collection.
     *
     * @return the namespace
     */
    MongoNamespace getNamespace() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Get the class of documents stored in this collection.
     *
     * @return the class
     */
    Class<DocumentT> getDocumentClass() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Get the codec registry for the RemoteMongoCollection.
     *
     * @return the {@link CodecRegistry}
     */
    CodecRegistry getCodecRegistry() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Create a new RemoteMongoCollection instance with a different default class to cast any
     * documents returned from the database into.
     *
     * @param clazz          the default class to cast any documents returned from the database into.
     * @param <NewDocumentT> The type that the new collection will encode documents from and decode
     *                       documents to.
     * @return a new RemoteMongoCollection instance with the different default class
     */
    <NewDocumentT> RemoteMongoCollection<NewDocumentT> withDocumentClass(final Class<NewDocumentT> clazz) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Create a new RemoteMongoCollection instance with a different codec registry.
     *
     * @param codecRegistry the new {@link CodecRegistry} for the
     *                      collection.
     * @return a new RemoteMongoCollection instance with the different codec registry
     */
    RemoteMongoCollection<DocumentT> withCodecRegistry(final CodecRegistry codecRegistry) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Counts the number of documents in the collection.
     *
     * @return a task containing the number of documents in the collection
     */
    Task<Long> count() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     * @param filter the query filter
     * @return a task containing the number of documents in the collection
     */
    Task<Long> count(final Bson filter) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     * @param filter  the query filter
     * @param options the options describing the count
     * @return a task containing the number of documents in the collection
     */
    Task<Long> count(final Bson filter, final RemoteCountOptions options) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds all documents in the collection.
     *
     * @return the find iterable interface
     */
    RemoteFindIterable<DocumentT> find() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds all documents in the collection.
     *
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return the find iterable interface
     */
    <ResultT> RemoteFindIterable<ResultT> find(final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds all documents in the collection.
     *
     * @param filter the query filter
     * @return the find iterable interface
     */
    RemoteFindIterable<DocumentT> find(final Bson filter) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds all documents in the collection.
     *
     * @param filter      the query filter
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return the find iterable interface
     */
    <ResultT> RemoteFindIterable<ResultT> find(final Bson filter, final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }


    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param pipeline the aggregation pipeline
     * @return an iterable containing the result of the aggregation operation
     */
    RemoteAggregateIterable<DocumentT> aggregate(final List<? extends Bson> pipeline) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param pipeline    the aggregation pipeline
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return an iterable containing the result of the aggregation operation
     */
    <ResultT> RemoteAggregateIterable<ResultT> aggregate(
            final List<? extends Bson> pipeline,
            final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Inserts the provided document. If the document is missing an identifier, the client should
     * generate one.
     *
     * @param document the document to insert
     * @return a task containing the result of the insert one operation
     */
    Task<RemoteInsertOneResult> insertOne(final DocumentT document) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Inserts one or more documents.
     *
     * @param documents the documents to insert
     * @return a task containing the result of the insert many operation
     */
    Task<RemoteInsertManyResult> insertMany(final List<? extends DocumentT> documents) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Removes at most one document from the collection that matches the given filter.  If no
     * documents match, the collection is not
     * modified.
     *
     * @param filter the query filter to apply the the delete operation
     * @return a task containing the result of the remove one operation
     */
    Task<RemoteDeleteResult> deleteOne(final Bson filter) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Removes all documents from the collection that match the given query filter.  If no documents
     * match, the collection is not modified.
     *
     * @param filter the query filter to apply the the delete operation
     * @return a task containing the result of the remove many operation
     */
    Task<RemoteDeleteResult> deleteMany(final Bson filter) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter a document describing the query filter, which may not be null.
     * @param update a document describing the update, which may not be null. The update to
     *               apply must include only update operators.
     * @return a task containing the result of the update one operation
     */
    Task<RemoteUpdateResult> updateOne(final Bson filter, final Bson update) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter        a document describing the query filter, which may not be null.
     * @param update        a document describing the update, which may not be null. The update to
     *                      apply must include only update operators.
     * @param updateOptions the options to apply to the update operation
     * @return a task containing the result of the update one operation
     */
    Task<RemoteUpdateResult> updateOne(
            final Bson filter,
            final Bson update,
            final RemoteUpdateOptions updateOptions) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter a document describing the query filter, which may not be null.
     * @param update a document describing the update, which may not be null. The update to
     *               apply must include only update operators.
     * @return a task containing the result of the update many operation
     */
    Task<RemoteUpdateResult> updateMany(final Bson filter, final Bson update) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter        a document describing the query filter, which may not be null.
     * @param update        a document describing the update, which may not be null. The update to
     *                      apply must include only update operators.
     * @param updateOptions the options to apply to the update operation
     * @return a task containing the result of the update many operation
     */
    Task<RemoteUpdateResult> updateMany(
            final Bson filter,
            final Bson update,
            final RemoteUpdateOptions updateOptions) {
        throw new RuntimeException("Not Implemented");
    }

    // TODO: what about these two?
//    /**
//     * Watches specified IDs in a collection.  This convenience overload supports the use case
//     * of non-{@link BsonValue} instances of {@link ObjectId}.
//     * @param ids unique object identifiers of the IDs to watch.
//     * @return the stream of change events.
//     */
//    Task<ChangeStream<Task<ChangeEvent<DocumentT>>, DocumentT>> watch(final ObjectId... ids) {
//        throw new RuntimeException("Not Implemented");
//    }
//
//    /**
//     * Watches specified IDs in a collection.
//     * @param ids the ids to watch.
//     * @return the stream of change events.
//     */
//    Task<ChangeStream<Task<ChangeEvent<DocumentT>>, DocumentT>> watch(final BsonValue... ids) {
//        throw new RuntimeException("Not Implemented");
//    }

    // TODO: what about this one?
//    /**
//     * A set of synchronization related operations on this collection.
//     *
//     * <p>
//     * WARNING: This is a BETA feature and the API and on-device storage format
//     * are subject to change.
//     * </p>
//     * @return set of sync operations for this collection
//     */
//    Sync<DocumentT> sync() {
//        throw new RuntimeException("Not Implemented");
//    }
}
