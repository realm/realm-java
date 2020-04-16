package io.realm.mongodb;

/**
 * The remote MongoClient used for working with data in MongoDB remotely via Stitch.
 */
public class RemoteMongoClient {

    private RemoteMongoDatabase remoteMongoDatabase = new RemoteMongoDatabase();

    public RemoteMongoClient() {
        // TODO
    }

    /**
     * Gets a {@link RemoteMongoDatabase} instance for the given database name.
     *
     * @param databaseName the name of the database to retrieve
     * @return a {@code RemoteMongoDatabase} representing the specified database
     */
    public RemoteMongoDatabase getDatabase(final String databaseName) {
        return new RemoteMongoDatabase();
    }
}
