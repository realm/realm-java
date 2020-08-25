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

package io.realm.mongodb.mongo.events;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonElement;
import org.bson.BsonString;
import org.bson.BsonValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.mongodb.AppException;
import io.realm.mongodb.ErrorCode;

import static io.realm.internal.Util.checkContainsKey;

/**
 * Indicates which fields have been modified in a given update operation.
 */
public final class UpdateDescription {
    private static final String DOCUMENT_VERSION_FIELD = "__stitch_sync_version";

    private final BsonDocument updatedFields;
    private final Set<String> removedFields;

    /**
     * Creates an update description with the specified updated fields and removed field names.
     *
     * @param updatedFields Nested key-value pair representation of updated fields.
     * @param removedFields Collection of removed field names.
     */
    UpdateDescription(
            final BsonDocument updatedFields,
            final Collection<String> removedFields
    ) {
        this.updatedFields = (updatedFields == null) ? new BsonDocument() : updatedFields;
        this.removedFields = (removedFields == null) ? new HashSet<>() : new HashSet<>(removedFields);
    }

    /**
     * Returns a {@link BsonDocument} containing keys and values representing (respectively) the
     * fields that have changed in the corresponding update and their new values.
     *
     * @return the updated field names and their new values.
     */
    public BsonDocument getUpdatedFields() {
        return updatedFields;
    }

    /**
     * Returns a {@link List} containing the field names that have been removed in the corresponding
     * update.
     *
     * @return the removed fields names.
     */
    public Collection<String> getRemovedFields() {
        return removedFields;
    }

    /**
     * Convert this update description to an update document.
     *
     * @return an update document with the appropriate $set and $unset documents.
     */
    public BsonDocument toUpdateDocument() {
        final List<BsonElement> unsets = new ArrayList<>();
        for (final String removedField : this.removedFields) {
            unsets.add(new BsonElement(removedField, new BsonBoolean(true)));
        }
        final BsonDocument updateDocument = new BsonDocument();

        if (this.updatedFields.size() > 0) {
            updateDocument.append("$set", this.updatedFields);
        }

        if (unsets.size() > 0) {
            updateDocument.append("$unset", new BsonDocument(unsets));
        }

        return updateDocument;
    }

    /**
     * Converts this update description to its document representation as it would appear in a
     * MongoDB Change Event.
     *
     * @return the update description document as it would appear in a change event
     */
    public BsonDocument toBsonDocument() {
        final BsonDocument updateDescDoc = new BsonDocument();
        updateDescDoc.put(
                Fields.UPDATED_FIELDS_FIELD,
                this.getUpdatedFields());

        final BsonArray removedFields = new BsonArray();
        for (final String field : this.getRemovedFields()) {
            removedFields.add(new BsonString(field));
        }
        updateDescDoc.put(
                Fields.REMOVED_FIELDS_FIELD,
                removedFields);

        return updateDescDoc;
    }

    /**
     * Converts an update description BSON document from a MongoDB Change Event into an
     * UpdateDescription object.
     *
     * @param document the
     * @return the converted UpdateDescription
     */
    public static UpdateDescription fromBsonDocument(final BsonDocument document) {
        try {
            checkContainsKey(Fields.UPDATED_FIELDS_FIELD, document, "document");
            checkContainsKey(Fields.REMOVED_FIELDS_FIELD, document, "document");
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.EVENT_DESERIALIZING, exception);
        }

        final BsonArray removedFieldsArr =
                document.getArray(Fields.REMOVED_FIELDS_FIELD);
        final Set<String> removedFields = new HashSet<>(removedFieldsArr.size());
        for (final BsonValue field : removedFieldsArr) {
            removedFields.add(field.asString().getValue());
        }

        return new UpdateDescription(document.getDocument(Fields.UPDATED_FIELDS_FIELD), removedFields);
    }

    /**
     * Unilaterally merge an update description into this update description.
     *
     * @param otherDescription the update description to merge into this
     * @return this merged update description
     */
    public UpdateDescription merge(@Nullable final UpdateDescription otherDescription) {
        if (otherDescription != null) {
            for (final Map.Entry<String, BsonValue> entry : this.updatedFields.entrySet()) {
                if (otherDescription.removedFields.contains(entry.getKey())) {
                    this.updatedFields.remove(entry.getKey());
                }
            }
            for (final String removedField : this.removedFields) {
                if (otherDescription.updatedFields.containsKey(removedField)) {
                    this.removedFields.remove(removedField);
                }
            }

            this.removedFields.addAll(otherDescription.removedFields);
            this.updatedFields.putAll(otherDescription.updatedFields);
        }

        return this;
    }

    /**
     * Find the diff between two documents.
     *
     * <p>NOTE: This does not do a full diff on {@link BsonArray}. If there is
     * an inequality between the old and new array, the old array will
     * simply be replaced by the new one.
     *
     * @param beforeDocument original document
     * @param afterDocument  document to diff on
     * @param onKey          the key for our depth level
     * @param updatedFields  contiguous document of updated fields,
     *                       nested or otherwise
     * @param removedFields  contiguous list of removedFields,
     *                       nested or otherwise
     * @return a description of the updated fields and removed keys between the documents
     */
    private static UpdateDescription diff(
            final BsonDocument beforeDocument,
            final BsonDocument afterDocument,
            final @Nullable String onKey,
            final BsonDocument updatedFields,
            final Set<String> removedFields) {
        // for each key in this document...
        for (final Map.Entry<String, BsonValue> entry : beforeDocument.entrySet()) {
            final String key = entry.getKey();
            // don't worry about the _id or version field for now
            if (key.equals("_id") || key.equals(DOCUMENT_VERSION_FIELD)) {
                continue;
            }
            final BsonValue oldValue = entry.getValue();

            final String actualKey = onKey == null ? key : String.format("%s.%s", onKey, key);
            // if the key exists in the other document AND both are BsonDocuments
            // diff the documents recursively, carrying over the keys to keep
            // updatedFields and removedFields flat.
            // this will allow us to reference whole objects as well as nested
            // properties.
            // else if the key does not exist, the key has been removed.
            if (afterDocument.containsKey(key)) {
                final BsonValue newValue = afterDocument.get(key);
                if ((oldValue instanceof BsonDocument) && (newValue instanceof BsonDocument)) {
                    diff((BsonDocument) oldValue,
                            (BsonDocument) newValue,
                            actualKey,
                            updatedFields,
                            removedFields);
                } else if (!oldValue.equals(newValue)) {
                    updatedFields.put(actualKey, newValue);
                }
            } else {
                removedFields.add(actualKey);
            }
        }

        // for each key in the other document...
        for (final Map.Entry<String, BsonValue> entry : afterDocument.entrySet()) {
            final String key = entry.getKey();
            // don't worry about the _id or version field for now
            if (key.equals("_id") || key.equals(DOCUMENT_VERSION_FIELD)) {
                continue;
            }

            final BsonValue newValue = entry.getValue();
            // if the key is not in the this document,
            // it is a new key with a new value.
            // updatedFields will included keys that must
            // be newly created.
            final String actualKey = (onKey == null) ? key : String.format("%s.%s", onKey, key);
            if (!beforeDocument.containsKey(key)) {
                updatedFields.put(actualKey, newValue);
            }
        }

        return new UpdateDescription(updatedFields, removedFields);
    }

    /**
     * Find the diff between two documents.
     *
     * <p>NOTE: This does not do a full diff on [BsonArray]. If there is
     * an inequality between the old and new array, the old array will
     * simply be replaced by the new one.
     *
     * @param beforeDocument original document
     * @param afterDocument  document to diff on
     * @return a description of the updated fields and removed keys between the documents.
     */
    public static UpdateDescription diff(
            @Nullable final BsonDocument beforeDocument,
            @Nullable final BsonDocument afterDocument) {
        if ((beforeDocument == null) || (afterDocument == null)) {
            return new UpdateDescription(new BsonDocument(), new HashSet<>());
        }

        return UpdateDescription.diff(
                beforeDocument,
                afterDocument,
                null,
                new BsonDocument(),
                new HashSet<>()
        );
    }

    /**
     * Determines whether this update description is empty.
     *
     * @return true if the update description is empty, false otherwise
     */
    public boolean isEmpty() {
        return (this.updatedFields.isEmpty()) && (this.removedFields.isEmpty());
    }

    @Override
    public boolean equals(final Object obj) {
        if ((obj == null) || !obj.getClass().equals(UpdateDescription.class)) {
            return false;
        }
        final UpdateDescription other = (UpdateDescription) obj;

        return other.getRemovedFields().equals(this.removedFields)
                && other.getUpdatedFields().equals(this.updatedFields);
    }

    @Override
    public int hashCode() {
        return removedFields.hashCode() + (31 * updatedFields.hashCode());
    }

    private static final class Fields {
        static final String UPDATED_FIELDS_FIELD = "updatedFields";
        static final String REMOVED_FIELDS_FIELD = "removedFields";
    }
}
