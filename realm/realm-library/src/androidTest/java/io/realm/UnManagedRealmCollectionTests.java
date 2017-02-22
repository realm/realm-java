/*
 * Copyright 2016 Realm Inc.
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

package io.realm;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.entities.AllJavaTypes;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for all methods part of the the {@link RealmCollection} interface, that have a different behavior
 * than managed RealmCollection classes.
 *
 * See {@link ManagedRealmCollectionTests} for similar tests for the managed behavior.
 */
@RunWith(Parameterized.class)
public class UnManagedRealmCollectionTests extends CollectionTests {

    private static final int TEST_SIZE = 10;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private final UnManagedCollection collectionClass;
    private Realm realm;
    private RealmCollection<AllJavaTypes> collection;

    @Parameterized.Parameters(name = "{0}")
    public static List<UnManagedCollection> data() {
        return Arrays.asList(UnManagedCollection.values());
    }

    public UnManagedRealmCollectionTests(UnManagedCollection collectionType) {
        this.collectionClass = collectionType;
    }

    @Before
    public void setup() {
        realm = Realm.getInstance(configFactory.createConfiguration());
        collection = createCollection(collectionClass);
    }

    private RealmCollection<AllJavaTypes> createCollection(UnManagedCollection collectionClass) {
        switch (collectionClass) {
            case UNMANAGED_REALMLIST:
                return populateInMemoryList(TEST_SIZE);

            default:
                throw new AssertionError("Unsupported class: " + collectionClass);
        }
    }

    @After
    public void tearDown() {
        realm.close();
    }

    @Test
    public void unsupportedMethods_unManagedCollections() {
        // RealmCollection methods.
        for (RealmCollectionMethod method : RealmCollectionMethod.values()) {
            try {
                switch (method) {
                    // Unsupported methods.
                    case WHERE: collection.where(); break;
                    case MIN: collection.min(AllJavaTypes.FIELD_LONG); break;
                    case MAX: collection.max(AllJavaTypes.FIELD_LONG); break;
                    case SUM: collection.sum(AllJavaTypes.FIELD_LONG); break;
                    case AVERAGE: collection.average(AllJavaTypes.FIELD_LONG); break;
                    case MIN_DATE: collection.minDate(AllJavaTypes.FIELD_DATE); break;
                    case MAX_DATE: collection.maxDate(AllJavaTypes.FIELD_DATE); break;
                    case DELETE_ALL_FROM_REALM: collection.deleteAllFromRealm(); break;

                    // Supported methods.
                    case IS_VALID: assertTrue(collection.isValid()); continue;
                    case IS_MANAGED: assertFalse(collection.isManaged()); continue;
                }
                fail(method + " should have thrown an exception.");
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    @Test
    public void isLoaded() {
        assertTrue(collection.isLoaded());
    }

    @Test
    public void load() {
        assertTrue(collection.load());
    }

    @Test
    public void isValid() {
        assertTrue(collection.isValid());
    }

    @Test
    public void isManaged() {
        assertFalse(collection.isManaged());
    }

    @Test
    public void contains() {
        AllJavaTypes obj = collection.iterator().next();
        assertTrue(collection.contains(obj));
    }

    @Test
    public void equals_sameRealmObjectsDifferentCollection() {
        List<AllJavaTypes> list = new ArrayList<AllJavaTypes>();
        list.addAll(collection);
        assertTrue(collection.equals(list));
    }
}
