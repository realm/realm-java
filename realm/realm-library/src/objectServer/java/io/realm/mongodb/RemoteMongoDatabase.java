package io.realm.mongodb;

import org.bson.Document;

/**
 * The RemoteMongoDatabase interface.
 */
public class RemoteMongoDatabase {

    public RemoteMongoDatabase() {
        // TODO
    }

    /**
     * Gets the name of the database.
     *
     * @return the database name
     */
    public String getName() {
        // TODO
        return null;
    }

    /**
     * Gets a collection.
     *
     * @param collectionName the name of the collection to return
     * @return the collection
     */
    public RemoteMongoCollection<Document> getCollection(final String collectionName) {
        // TODO
        return null;
    }

    /**
     * Gets a collection, with a specific default document class.
     *
     * @param collectionName the name of the collection to return
     * @param documentClass  the default class to cast any documents returned from the database into.
     * @param <DocumentT>    the type of the class to use instead of {@code Document}.
     * @return the collection
     */
    public <DocumentT> RemoteMongoCollection<DocumentT> getCollection(
            final String collectionName,
            final Class<DocumentT> documentClass
    ) {
        // TODO
        return null;
    }
}
