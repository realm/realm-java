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
import io.realm.mongodb.mongo.iterable.AggregateIterable;
import io.realm.mongodb.mongo.iterable.FindIterable;
import io.realm.internal.objectstore.OsMongoCollection;
import io.realm.mongodb.mongo.options.CountOptions;
import io.realm.mongodb.mongo.options.FindOneAndModifyOptions;
import io.realm.mongodb.mongo.options.FindOptions;
import io.realm.mongodb.mongo.options.InsertManyResult;
import io.realm.mongodb.mongo.options.UpdateOptions;
import io.realm.mongodb.mongo.result.DeleteResult;
import io.realm.mongodb.mongo.result.InsertOneResult;
import io.realm.mongodb.mongo.result.UpdateResult;

/**
 * The MongoCollection interface provides read and write access to documents.
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

    private final MongoNamespace nameSpace;
    private final OsMongoCollection<DocumentT> osMongoCollection;
    private final TaskDispatcher dispatcher;

    MongoCollection(final MongoNamespace nameSpace,
                    final OsMongoCollection<DocumentT> osMongoCollection,
                    final TaskDispatcher dispatcher) {
        this.nameSpace = nameSpace;
        this.osMongoCollection = osMongoCollection;
        this.dispatcher = dispatcher;
    }

    /**
     * Gets the namespace of this collection, i.e. the database and collection names together.
     *
     * @return the namespace
     */
    public MongoNamespace getNamespace() {
        return nameSpace;
    }

    /**
     * Gets the class of documents stored in this collection.
     * <p>
     * If you used the simple {@link MongoDatabase#getCollection(String)} to get this collection,
     * this is {@link org.bson.Document}.
     * </p>
     *
     * @return the class of documents in this collection
     */
    public Class<DocumentT> getDocumentClass() {
        return osMongoCollection.getDocumentClass();
    }

    /**
     * Gets the codec registry for the MongoCollection.
     *
     * @return the {@link CodecRegistry} for this collection
     */
    public CodecRegistry getCodecRegistry() {
        return osMongoCollection.getCodecRegistry();
    }

    /**
     * Creates a new MongoCollection instance with a different default class to cast any
     * documents returned from the database into.
     *
     * @param clazz          the default class to which any documents returned from the database
     *                       will be cast.
     * @param <NewDocumentT> The type that the new collection will encode documents from and decode
     *                       documents to.
     * @return a new MongoCollection instance with the different default class
     */
    public <NewDocumentT> MongoCollection<NewDocumentT> withDocumentClass(
            final Class<NewDocumentT> clazz) {
        return new MongoCollection<>(nameSpace, osMongoCollection.withDocumentClass(clazz), dispatcher);
    }

    /**
     * Creates a new MongoCollection instance with a different codec registry.
     *
     * @param codecRegistry the new {@link CodecRegistry} for the
     *                      collection.
     * @return a new MongoCollection instance with the different codec registry
     */
    public MongoCollection<DocumentT> withCodecRegistry(final CodecRegistry codecRegistry) {
        return new MongoCollection<>(nameSpace, osMongoCollection.withCodecRegistry(codecRegistry), dispatcher);
    }

    /**
     * Counts the number of documents in the collection.
     *
     * @return a task containing the number of documents in the collection
     */
    public Task<Long> count() {
        return dispatcher.dispatchTask(osMongoCollection::count);
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
        return dispatcher.dispatchTask(osMongoCollection::findOne);
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
     * @param options a {@link FindOptions} struct
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
     * @param options     a {@link FindOptions} struct
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the result of the find one operation
     */
    public <ResultT> Task<ResultT> findOne(final Bson filter,
                                           final FindOptions options,
                                           final Class<ResultT> resultClass) {
        return dispatcher.dispatchTask(() ->
                osMongoCollection.findOne(filter, options, resultClass)
        );
    }

    /**
     * Finds all documents in the collection.
     * <p>
     * All documents will be delivered in the form of a {@link FindIterable} from which individual
     * elements can be extracted.
     *
     * @return an iterable containing the result of the find operation
     */
    public FindIterable<DocumentT> find() {
        return osMongoCollection.find();
    }

    /**
     * Finds all documents in the collection using {@link FindOptions} to build the query.
     * <p>
     * All documents will be delivered in the form of a {@link FindIterable} from which individual
     * elements can be extracted.
     *
     * @param options a {@link FindOptions} struct for building the query
     * @return an iterable containing the result of the find operation
     */
    public FindIterable<DocumentT> find(final FindOptions options) {
        return osMongoCollection.find(options);
    }

    /**
     * Finds all documents in the collection specifying an output class.
     * <p>
     * All documents will be delivered in the form of a {@link FindIterable} from which individual
     * elements can be extracted.
     *
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return an iterable containing the result of the find operation
     */
    public <ResultT> FindIterable<ResultT> find(final Class<ResultT> resultClass) {
        return osMongoCollection.find(resultClass);
    }

    /**
     * Finds all documents in the collection specifying an output class and also using
     * {@link FindOptions} to build the query.
     * <p>
     * All documents will be delivered in the form of a {@link FindIterable} from which individual
     * elements can be extracted.
     *
     * @param resultClass the class to decode each document into
     * @param options a {@link FindOptions} struct for building the query
     * @param <ResultT>   the target document type of the iterable.
     * @return an iterable containing the result of the find operation
     */
    public <ResultT> FindIterable<ResultT> find(final Class<ResultT> resultClass,
                                                final FindOptions options) {
        return osMongoCollection.find(resultClass, options);
    }

    /**
     * Finds all documents in the collection that match the given filter.
     * <p>
     * All documents will be delivered in the form of a {@link FindIterable} from which individual
     * elements can be extracted.
     *
     * @param filter the query filter
     * @return an iterable containing the result of the find operation
     */
    public FindIterable<DocumentT> find(final Bson filter) {
        return osMongoCollection.find(filter);
    }

    /**
     * Finds all documents in the collection that match the given filter using {@link FindOptions}
     * to build the query.
     * <p>
     * All documents will be delivered in the form of a {@link FindIterable} from which individual
     * elements can be extracted.
     *
     * @param filter the query filter
     * @param options a {@link FindOptions} struct
     * @return an iterable containing the result of the find operation
     */
    public FindIterable<DocumentT> find(final Bson filter, final FindOptions options) {
        return osMongoCollection.find(filter, options);
    }

    /**
     * Finds all documents in the collection that match the given filter specifying an output class.
     * <p>
     * All documents will be delivered in the form of a {@link FindIterable} from which individual
     * elements can be extracted.
     *
     * @param filter      the query filter
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return an iterable containing the result of the find operation
     */
    public <ResultT> FindIterable<ResultT> find(final Bson filter,
                                                final Class<ResultT> resultClass) {
        return osMongoCollection.find(filter, resultClass);
    }

    /**
     * Finds all documents in the collection that match the given filter specifying an output class
     * and also using {@link FindOptions} to build the query.
     * <p>
     * All documents will be delivered in the form of a {@link FindIterable} from which individual
     * elements can be extracted.
     *
     * @param filter      the query filter
     * @param resultClass the class to decode each document into
     * @param options     a {@link FindOptions} struct
     * @param <ResultT>   the target document type of the iterable.
     * @return an iterable containing the result of the find operation
     */
    public <ResultT> FindIterable<ResultT> find(final Bson filter,
                                                final Class<ResultT> resultClass,
                                                final FindOptions options) {
        return osMongoCollection.find(filter, resultClass, options);
    }

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     * <p>
     * All documents will be delivered in the form of an {@link AggregateIterable} from which
     * individual elements can be extracted.
     *
     * @param pipeline the aggregation pipeline
     * @return an {@link AggregateIterable} from which the results can be extracted
     */
    public AggregateIterable<DocumentT> aggregate(final List<? extends Bson> pipeline) {
        return osMongoCollection.aggregate(pipeline);
    }

    /**
     * Aggregates documents according to the specified aggregation pipeline specifying an output
     * class.
     * <p>
     * All documents will be delivered in the form of an {@link AggregateIterable} from which
     * individual elements can be extracted.
     *
     * @param pipeline    the aggregation pipeline
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return an {@link AggregateIterable} from which the results can be extracted
     */
    public <ResultT> AggregateIterable<ResultT> aggregate(final List<? extends Bson> pipeline,
                                                          final Class<ResultT> resultClass) {
        return osMongoCollection.aggregate(pipeline, resultClass);
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
     * @param options a {@link FindOneAndModifyOptions} struct
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
     * @param options     a {@link FindOneAndModifyOptions} struct
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the resulting document
     */
    public <ResultT> Task<ResultT> findOneAndUpdate(final Bson filter,
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
     * @param options     a {@link FindOneAndModifyOptions} struct
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
     * @param options     a {@link FindOneAndModifyOptions} struct
     * @param resultClass the class to decode each document into
     * @param <ResultT>   the target document type of the iterable.
     * @return a task containing the resulting document
     */
    public <ResultT> Task<ResultT> findOneAndReplace(final Bson filter,
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
     * @param options a {@link FindOneAndModifyOptions} struct
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
     * @param options     a {@link FindOneAndModifyOptions} struct
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
}
