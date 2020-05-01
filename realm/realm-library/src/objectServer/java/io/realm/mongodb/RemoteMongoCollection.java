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

package io.realm.mongodb;

import com.google.android.gms.tasks.Task;

import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;

import io.realm.internal.objectstore.OsRemoteMongoCollection;
import io.realm.mongodb.remote.RemoteCountOptions;
import io.realm.mongodb.remote.RemoteFindOneAndModifyOptions;
import io.realm.mongodb.remote.RemoteUpdateOptions;
import io.realm.mongodb.remote.RemoteDeleteResult;
import io.realm.mongodb.remote.RemoteFindOptions;
import io.realm.mongodb.remote.RemoteInsertManyResult;
import io.realm.mongodb.remote.RemoteInsertOneResult;
import io.realm.mongodb.remote.RemoteUpdateResult;
import io.realm.mongodb.remote.aggregate.RemoteAggregateIterable;
import io.realm.mongodb.remote.find.RemoteFindIterable;

/**
 * The RemoteMongoCollection interface provides read and write access to documents.
 * <p>
 * Use {@link RemoteMongoDatabase#getCollection} to get a collection instance.
 * </p><p>
 * Before any access is possible, there must be an active, logged-in user.
 * </p><p>
 * Create, read, update and delete (CRUD) functionality is available depending
 * on the privileges of the active logged-in user. You can set up
 * <a href="https://docs.mongodb.com/stitch/mongodb/define-roles-and-permissions/" target=".">Roles</a>
 * in the Stitch console. Stitch checks any given request against the Roles for the
 * active user and determines whether the request is permitted for each requested
 * document.
 * </p>
 *
 * @param <DocumentT> The type that this collection will encode documents from and decode documents
 *                    to.
 * @see RemoteMongoDatabase
 * @see <a href="https://docs.mongodb.com/stitch/mongodb/" target=".">
 * MongoDB Atlas Overview with Stitch</a>
 */
public class RemoteMongoCollection<DocumentT> {

    private OsRemoteMongoCollection osRemoteMongoCollection;

    public RemoteMongoCollection(OsRemoteMongoCollection osRemoteMongoCollection) {
        this.osRemoteMongoCollection = osRemoteMongoCollection;
    }

    /**
     * Gets the namespace of this collection, i.e. the database and collection names together.
     *
     * @return the namespace
     */
    MongoNamespace getNamespace() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Get the class of documents stored in this collection.
     * <p>
     * If you used the simple {@link RemoteMongoDatabase#getCollection(String)} to get
     * this collection,
     * this is {@link org.bson.Document}.
     * </p>
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
    <NewDocumentT> RemoteMongoCollection<NewDocumentT> withDocumentClass(
            final Class<NewDocumentT> clazz) {
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
        BsonDocument bsonDocument = filter.toBsonDocument(null, null);
        osRemoteMongoCollection.count(bsonDocument.toJson());
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
     * Finds a document in the collection.
     *
     * @return a task containing the result of the find one operation
     */
    Task<DocumentT> findOne() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection.
     *
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type
     * @return a task containing the result of the find one operation
     */
    <ResultT> Task<ResultT> findOne(final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection.
     *
     * @param filter the query filter
     * @return a task containing the result of the find one operation
     */
    Task<DocumentT> findOne(final Bson filter) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection.
     *
     * @param filter      the query filter
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the result of the find one operation
     */
    <ResultT> Task<ResultT> findOne(final Bson filter, final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection.
     *
     * @param filter  the query filter
     * @param options A RemoteFindOptions struct
     * @return a task containing the result of the find one operation
     */
    Task<DocumentT> findOne(final Bson filter, final RemoteFindOptions options) {
        throw new RuntimeException("Not Implemented");
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
    <ResultT> Task<ResultT> findOne(
            final Bson filter,
            final RemoteFindOptions options,
            final Class<ResultT> resultClass) {
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
     * Finds all documents in the collection that match the given filter.
     *
     * @param filter the query filter
     * @return the find iterable interface
     */
    RemoteFindIterable<DocumentT> find(final Bson filter) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds all documents in the collection that match the given filter.
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

    /**
     * Finds a document in the collection and performs the given update.
     *
     * @param filter the query filter
     * @param update the update document
     * @return a task containing the resulting document
     */
    Task<DocumentT> findOneAndUpdate(final Bson filter, final Bson update) {
        throw new RuntimeException("Not Implemented");
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
    <ResultT> Task<ResultT> findOneAndUpdate(final Bson filter,
                                             final Bson update,
                                             final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection and performs the given update.
     *
     * @param filter  the query filter
     * @param update  the update document
     * @param options A RemoteFindOneAndModifyOptions struct
     * @return a task containing the resulting document
     */
    Task<DocumentT> findOneAndUpdate(final Bson filter,
                                     final Bson update,
                                     final RemoteFindOneAndModifyOptions options) {
        throw new RuntimeException("Not Implemented");
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
    <ResultT> Task<ResultT> findOneAndUpdate(
            final Bson filter,
            final Bson update,
            final RemoteFindOneAndModifyOptions options,
            final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection and replaces it with the given document.
     *
     * @param filter      the query filter
     * @param replacement the document to replace the matched document with
     * @return a task containing the resulting document
     */
    Task<DocumentT> findOneAndReplace(final Bson filter, final Bson replacement) {
        throw new RuntimeException("Not Implemented");
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
    <ResultT> Task<ResultT> findOneAndReplace(final Bson filter,
                                              final Bson replacement,
                                              final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection and replaces it with the given document.
     *
     * @param filter      the query filter
     * @param replacement the document to replace the matched document with
     * @param options     A RemoteFindOneAndModifyOptions struct
     * @return a task containing the resulting document
     */
    Task<DocumentT> findOneAndReplace(final Bson filter,
                                      final Bson replacement,
                                      final RemoteFindOneAndModifyOptions options) {
        throw new RuntimeException("Not Implemented");
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
    <ResultT> Task<ResultT> findOneAndReplace(
            final Bson filter,
            final Bson replacement,
            final RemoteFindOneAndModifyOptions options,
            final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection and delete it.
     *
     * @param filter the query filter
     * @return a task containing the resulting document
     */
    Task<DocumentT> findOneAndDelete(final Bson filter) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection and delete it.
     *
     * @param filter      the query filter
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the resulting document
     */
    <ResultT> Task<ResultT> findOneAndDelete(final Bson filter,
                                             final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Finds a document in the collection and delete it.
     *
     * @param filter  the query filter
     * @param options A RemoteFindOneAndModifyOptions struct
     * @return a task containing the resulting document
     */
    Task<DocumentT> findOneAndDelete(final Bson filter,
                                     final RemoteFindOneAndModifyOptions options) {
        throw new RuntimeException("Not Implemented");
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
    <ResultT> Task<ResultT> findOneAndDelete(
            final Bson filter,
            final RemoteFindOneAndModifyOptions options,
            final Class<ResultT> resultClass) {
        throw new RuntimeException("Not Implemented");
    }

    // TODO: what about these?
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
//    Sync<DocumentT> sync();
}
