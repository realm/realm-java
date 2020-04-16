package io.realm.mongodb.remote.internal.find;

import org.bson.conversions.Bson;

import javax.annotation.Nullable;

/**
 * Iterable for find.
 *
 * @param <ResultT> The type of the result.
 */
//public interface RemoteFindIterable<ResultT> extends RemoteMongoIterable<ResultT> {
public class RemoteFindIterable<ResultT> {

  /**
   * Sets the query filter to apply to the query.
   *
   * @param filter the filter, which may be null.
   * @return this
   */
  RemoteFindIterable<ResultT> filter(@Nullable final Bson filter) {
    throw new RuntimeException("Not Implemented");
  }

  /**
   * Sets the limit to apply.
   *
   * @param limit the limit, which may be 0
   * @return this
   */
  RemoteFindIterable<ResultT> limit(final int limit) {
    throw new RuntimeException("Not Implemented");
  }

  /**
   * Sets a document describing the fields to return for all matching documents.
   *
   * @param projection the project document, which may be null.
   * @return this
   */
  RemoteFindIterable<ResultT> projection(@Nullable final Bson projection) {
    throw new RuntimeException("Not Implemented");
  }

  /**
   * Sets the sort criteria to apply to the query.
   *
   * @param sort the sort criteria, which may be null.
   * @return this
   */
  RemoteFindIterable<ResultT> sort(@Nullable final Bson sort) {
    throw new RuntimeException("Not Implemented");
  }
}