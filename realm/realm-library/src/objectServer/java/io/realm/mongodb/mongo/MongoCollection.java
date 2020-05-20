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

package io.realm.mongodb.mongo;

import com.google.android.gms.tasks.Task;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;

import io.realm.internal.common.TaskDispatcher;
import io.realm.internal.objectstore.OsMongoCollection;
import io.realm.mongodb.mongo.iterable.AggregateIterable;
import io.realm.mongodb.mongo.iterable.FindIterable;
import io.realm.mongodb.mongo.options.CountOptions;
import io.realm.mongodb.mongo.options.FindOneAndModifyOptions;
import io.realm.mongodb.mongo.options.FindOptions;
import io.realm.mongodb.mongo.options.InsertManyResult;
import io.realm.mongodb.mongo.options.UpdateOptions;
import io.realm.mongodb.mongo.result.DeleteResult;
import io.realm.mongodb.mongo.result.InsertOneResult;
import io.realm.mongodb.mongo.result.UpdateResult;

/**
 * The RemoteMongoCollection interface provides read and write access to documents.
 * <p>
 * Use {@link MongoDatabase#getCollection} to get a collection instance.
 * </p><p>
 * Before any access is possible, there must be an active, logged-in user.
 *
 * @param <DocumentT> The type that this collection will encode documents from and decode documents
 *                    to.
 * @see MongoDatabase
 */
public class MongoCollection<DocumentT> {

    private OsMongoCollection<DocumentT> osMongoCollection;

    private TaskDispatcher dispatcher;

    MongoCollection(OsMongoCollection<DocumentT> osMongoCollection) {
        this.dispatcher = new TaskDispatcher();
        this.osMongoCollection = osMongoCollection;
    }

    /**
     * Gets the namespace of this collection, i.e. the database and collection names together.
     *
     * @return the namespace
     */
    public MongoNamespace getNamespace() {
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
    public <NewDocumentT> MongoCollection<NewDocumentT> withDocumentClass(
            final Class<NewDocumentT> clazz) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Create a new RemoteMongoCollection instance with a different codec registry.
     *
     * @param codecRegistry the new {@link CodecRegistry} for the
     *                      collection.
     * @return a new RemoteMongoCollection instance with the different codec registry
     */
    public MongoCollection<DocumentT> withCodecRegistry(final CodecRegistry codecRegistry) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Counts the number of documents in the collection.
     *
     * @return a task containing the number of documents in the collection
     */
    public Task<Long> count() {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.count()
        );
    }

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     * @param filter the query filter
     * @return a task containing the number of documents in the collection
     */
    public Task<Long> count(final Bson filter) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.count(filter)
        );
    }

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     * @param filter  the query filter
     * @param options the options describing the count
     * @return a task containing the number of documents in the collection
     */
    public Task<Long> count(final Bson filter, final CountOptions options) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.count(filter, options)
        );
    }

    /**
     * Finds a document in the collection.
     *
     * @return a task containing the result of the find one operation
     */
    public Task<DocumentT> findOne() {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOne()
        );
    }

    /**
     * Finds a document in the collection.
     *
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type
     * @return a task containing the result of the find one operation
     */
    public <ResultT> Task<ResultT> findOne(final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOne(resultClass)
        );
    }

    /**
     * Finds a document in the collection.
     *
     * @param filter the query filter
     * @return a task containing the result of the find one operation
     */
    public Task<DocumentT> findOne(final Bson filter) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOne(filter)
        );
    }

    /**
     * Finds a document in the collection.
     *
     * @param filter      the query filter
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the result of the find one operation
     */
    public <ResultT> Task<ResultT> findOne(final Bson filter, final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOne(filter, resultClass)
        );
    }

    /**
     * Finds a document in the collection.
     *
     * @param filter  the query filter
     * @param options A RemoteFindOptions struct
     * @return a task containing the result of the find one operation
     */
    public Task<DocumentT> findOne(final Bson filter, final FindOptions options) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOne(filter, options)
        );
    }

    /**
     * Finds a document in the collection.
     *
     * @param filter      the query filter
     * @param options     A RemoteFindOptions struct
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the result of the find one operation
     */
    public <ResultT> Task<ResultT> findOne(
            final Bson filter,
            final FindOptions options,
            final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOne(filter, options, resultClass)
        );
    }

    /**
     * Finds all documents in the collection.
     *
     * @return the find iterable interface
     */
    public FindIterable<DocumentT> find() {
        return osMongoCollection.find();
    }

    /**
     * Finds all documents in the collection.
     *
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return the find iterable interface
     */
    public <ResultT> FindIterable<ResultT> find(final Class<ResultT> resultClass) {
        return osMongoCollection.find(resultClass);
    }

    /**
     * Finds all documents in the collection that match the given filter.
     *
     * @param filter the query filter
     * @return the find iterable interface
     */
    public FindIterable<DocumentT> find(final Bson filter) {
        return osMongoCollection.find(filter);
    }

    /**
     * Finds all documents in the collection that match the given filter.
     *
     * @param filter      the query filter
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return the find iterable interface
     */
    public <ResultT> FindIterable<ResultT> find(final Bson filter, final Class<ResultT> resultClass) {
        return osMongoCollection.find(filter, resultClass);
    }

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param pipeline the aggregation pipeline
     * @return an iterable containing the result of the aggregation operation
     */
    public AggregateIterable<DocumentT> aggregate(final List<? extends Bson> pipeline) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param pipeline    the aggregation pipeline
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return an iterable containing the result of the aggregation operation
     */
    public <ResultT> AggregateIterable<ResultT> aggregate(
            final List<? extends Bson> pipeline,
            final Class<ResultT> resultClass) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Inserts the provided document. If the document is missing an identifier, the client should
     * generate one.
     *
     * @param document the document to insert
     * @return a task containing the result of the insert one operation
     */
    public Task<InsertOneResult> insertOne(final DocumentT document) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.insertOne(document)
        );
    }

    /**
     * Inserts one or more documents.
     *
     * @param documents the documents to insert
     * @return a task containing the result of the insert many operation
     */
    public Task<InsertManyResult> insertMany(final List<? extends DocumentT> documents) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.insertMany(documents)
        );
    }

    /**
     * Removes at most one document from the collection that matches the given filter.  If no
     * documents match, the collection is not
     * modified.
     *
     * @param filter the query filter to apply the the delete operation
     * @return a task containing the result of the remove one operation
     */
    public Task<DeleteResult> deleteOne(final Bson filter) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.deleteOne(filter)
        );
    }

    /**
     * Removes all documents from the collection that match the given query filter.  If no documents
     * match, the collection is not modified.
     *
     * @param filter the query filter to apply the the delete operation
     * @return a task containing the result of the remove many operation
     */
    public Task<DeleteResult> deleteMany(final Bson filter) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.deleteMany(filter)
        );
    }

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter a document describing the query filter, which may not be null.
     * @param update a document describing the update, which may not be null. The update to
     *               apply must include only update operators.
     * @return a task containing the result of the update one operation
     */
    public Task<UpdateResult> updateOne(final Bson filter, final Bson update) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.updateOne(filter, update)
        );
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
    public Task<UpdateResult> updateOne(
            final Bson filter,
            final Bson update,
            final UpdateOptions updateOptions) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.updateOne(filter, update, updateOptions)
        );
    }

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter a document describing the query filter, which may not be null.
     * @param update a document describing the update, which may not be null. The update to
     *               apply must include only update operators.
     * @return a task containing the result of the update many operation
     */
    public Task<UpdateResult> updateMany(final Bson filter, final Bson update) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.updateMany(filter, update)
        );
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
    public Task<UpdateResult> updateMany(
            final Bson filter,
            final Bson update,
            final UpdateOptions updateOptions) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.updateMany(filter, update, updateOptions)
        );
    }

    /**
     * Finds a document in the collection and performs the given update.
     *
     * @param filter the query filter
     * @param update the update document
     * @return a task containing the resulting document
     */
    public Task<DocumentT> findOneAndUpdate(final Bson filter, final Bson update) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndUpdate(filter, update)
        );
    }

    /**
     * Finds a document in the collection and performs the given update.
     *
     * @param filter      the query filter
     * @param update      the update document
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the resulting document
     */
    public <ResultT> Task<ResultT> findOneAndUpdate(final Bson filter,
                                                    final Bson update,
                                                    final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndUpdate(filter, update, resultClass)
        );
    }

    /**
     * Finds a document in the collection and performs the given update.
     *
     * @param filter  the query filter
     * @param update  the update document
     * @param options A RemoteFindOneAndModifyOptions struct
     * @return a task containing the resulting document
     */
    public Task<DocumentT> findOneAndUpdate(final Bson filter,
                                            final Bson update,
                                            final FindOneAndModifyOptions options) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndUpdate(filter, update, options)
        );
    }

    /**
     * Finds a document in the collection and performs the given update.
     *
     * @param filter      the query filter
     * @param update      the update document
     * @param options     A RemoteFindOneAndModifyOptions struct
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the resulting document
     */
    public <ResultT> Task<ResultT> findOneAndUpdate(
            final Bson filter,
            final Bson update,
            final FindOneAndModifyOptions options,
            final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndUpdate(filter, update, options, resultClass)
        );
    }

    /**
     * Finds a document in the collection and replaces it with the given document.
     *
     * @param filter      the query filter
     * @param replacement the document to replace the matched document with
     * @return a task containing the resulting document
     */
    public Task<DocumentT> findOneAndReplace(final Bson filter, final Bson replacement) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndReplace(filter, replacement)
        );
    }

    /**
     * Finds a document in the collection and replaces it with the given document.
     *
     * @param filter      the query filter
     * @param replacement the document to replace the matched document with
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the resulting document
     */
    public <ResultT> Task<ResultT> findOneAndReplace(final Bson filter,
                                                     final Bson replacement,
                                                     final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndReplace(filter, replacement, resultClass)
        );
    }

    /**
     * Finds a document in the collection and replaces it with the given document.
     *
     * @param filter      the query filter
     * @param replacement the document to replace the matched document with
     * @param options     A RemoteFindOneAndModifyOptions struct
     * @return a task containing the resulting document
     */
    public Task<DocumentT> findOneAndReplace(final Bson filter,
                                             final Bson replacement,
                                             final FindOneAndModifyOptions options) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndReplace(filter, replacement, options)
        );
    }

    /**
     * Finds a document in the collection and replaces it with the given document.
     *
     * @param filter      the query filter
     * @param replacement the document to replace the matched document with
     * @param options     A RemoteFindOneAndModifyOptions struct
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the resulting document
     */
    public <ResultT> Task<ResultT> findOneAndReplace(
            final Bson filter,
            final Bson replacement,
            final FindOneAndModifyOptions options,
            final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndReplace(filter, replacement, options, resultClass)
        );
    }

    /**
     * Finds a document in the collection and delete it.
     *
     * @param filter the query filter
     * @return a task containing the resulting document
     */
    public Task<DocumentT> findOneAndDelete(final Bson filter) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndDelete(filter)
        );
    }

    /**
     * Finds a document in the collection and delete it.
     *
     * @param filter      the query filter
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the resulting document
     */
    public <ResultT> Task<ResultT> findOneAndDelete(final Bson filter,
                                                    final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndDelete(filter, resultClass)
        );
    }

    /**
     * Finds a document in the collection and delete it.
     *
     * @param filter  the query filter
     * @param options A RemoteFindOneAndModifyOptions struct
     * @return a task containing the resulting document
     */
    public Task<DocumentT> findOneAndDelete(final Bson filter,
                                            final FindOneAndModifyOptions options) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndDelete(filter, options)
        );
    }

    /**
     * Finds a document in the collection and delete it.
     *
     * @param filter      the query filter
     * @param options     A RemoteFindOneAndModifyOptions struct
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the resulting document
     */
    public <ResultT> Task<ResultT> findOneAndDelete(final Bson filter,
                                                    final FindOneAndModifyOptions options,
                                                    final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOneAndDelete(filter, options, resultClass)
        );
    }

    // FIXME: what about these?
//    /**
//     * Watches a collection. The resulting stream will be notified of all events on this collection
//     * that the active user is authorized to see based on the configured MongoDB rules.
//     *
//     * @return the stream of change events.
//     */
//    Task<AsyncChangeStream<DocumentT, ChangeEvent<DocumentT>>> watch();
//
//    /**
//     * Watches specified IDs in a collection.  This convenience overload supports the use case
//     * of non-{@link BsonValue} instances of {@link ObjectId}.
//     *
//     * @param ids unique object identifiers of the IDs to watch.
//     * @return the stream of change events.
//     */
//    Task<AsyncChangeStream<DocumentT, ChangeEvent<DocumentT>>> watch(final ObjectId... ids);
//
//    /**
//     * Watches specified IDs in a collection.
//     *
//     * @param ids the ids to watch.
//     * @return the stream of change events.
//     */
//    Task<AsyncChangeStream<DocumentT, ChangeEvent<DocumentT>>> watch(final BsonValue... ids);
//
//    /**
//     * Watches a collection. The provided BSON document will be used as a match expression filter on
//     * the change events coming from the stream.
//     * See https://docs.mongodb.com/manual/reference/operator/aggregation/match/ for documentation
//     * around how to define a match filter. Defining the match expression to filter ChangeEvents is
//     * similar to defining the match expression for triggers:
//     * https://docs.mongodb.com/stitch/triggers/database-triggers/
//     *
//     * @param matchFilter the $match filter to apply to incoming change events
//     * @return the stream of change events.
//     */
//    Task<AsyncChangeStream<DocumentT, ChangeEvent<DocumentT>>> watchWithFilter(
//            final BsonDocument matchFilter);
//
//    /**
//     * Watches a collection. The provided BSON document will be used as a match expression filter on
//     * the change events coming from the stream.
//     * See https://docs.mongodb.com/manual/reference/operator/aggregation/match/ for documentation
//     * around how to define a match filter. Defining the match expression to filter ChangeEvents is
//     * similar to defining the match expression for triggers:
//     * https://docs.mongodb.com/stitch/triggers/database-triggers/
//     *
//     * @param matchFilter the $match filter to apply to incoming change events
//     * @return the stream of change events.
//     */
//    Task<AsyncChangeStream<DocumentT, ChangeEvent<DocumentT>>> watchWithFilter(
//            final Document matchFilter);
//
//    /**
//     * Watches specified IDs in a collection.  This convenience overload supports the use case
//     * of non-{@link BsonValue} instances of {@link ObjectId}. This convenience overload supports the
//     * use case of non-{@link BsonValue} instances of {@link ObjectId}. Requests a stream where the
//     * full document of update events, and several other unnecessary fields are omitted from the
//     * change event objects returned by the server. This can save on network usage when watching
//     * large documents.
//     *
//     * @param ids unique object identifiers of the IDs to watch.
//     * @return the stream of change events.
//     */
//    Task<AsyncChangeStream<DocumentT, CompactChangeEvent<DocumentT>>> watchCompact(
//            final ObjectId... ids);
//
//    /**
//     * Watches specified IDs in a collection. This convenience overload supports the use case of
//     * non-{@link BsonValue} instances of {@link ObjectId}. Requests a stream where the full document
//     * of update events, and several other unnecessary fields are omitted from the change event
//     * objects returned by the server. This can save on network usage when watching large documents.
//     *
//     * @param ids the ids to watch.
//     * @return the stream of change events.
//     */
//    Task<AsyncChangeStream<DocumentT, CompactChangeEvent<DocumentT>>> watchCompact(
//            final BsonValue... ids);

    // FIXME: what about this one?
//    /**
//     * A set of synchronization related operations on this collection.
//     *
//     * <p>
//     * WARNING: This is a BETA feature and the API and on-device storage format
//     * are subject to change.
//     * </p>
//     * @return set of sync operations for this collection
//     */
//    Sync<DocumentT> sync();
}
