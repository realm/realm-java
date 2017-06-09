/*
 * Copyright 2015 Realm Inc.
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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.entities.DogPrimaryKey;
import io.realm.entities.Owner;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmSchemaTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private DynamicRealm realm;
    private RealmSchema realmSchema;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schema(AllJavaTypes.class, Owner.class, PrimaryKeyAsString.class, Cat.class, Dog.class,
                        DogPrimaryKey.class)
                .build();
        Realm.getInstance(realmConfig).close(); // create Schema
        realm = DynamicRealm.getInstance(realmConfig);
        realmSchema = this.realm.getSchema();
        realm.beginTransaction();
    }

    @After
    public void tearDown() {
        realm.cancelTransaction();
        realm.close();
    }

    @Test
    public void getAll() {
        Set<RealmObjectSchema> objectSchemas = realmSchema.getAll();
        assertEquals(6, objectSchemas.size());

        List<String> expectedTables = Arrays.asList(
                AllJavaTypes.CLASS_NAME, "Owner", "Cat", "Dog", "DogPrimaryKey", "PrimaryKeyAsString");
        for (RealmObjectSchema objectSchema : objectSchemas) {
            if (!expectedTables.contains(objectSchema.getClassName())) {
                fail(objectSchema.getClassName() + " was not found");
            }
        }
    }

    @Test
    public void create() {
        realmSchema.create("Foo");
        assertTrue(realmSchema.contains("Foo"));
    }

    @Test
    public void create_invalidNameThrows() {
        String[] names = { null, "", TestHelper.getRandomString(57) };

        for (String name : names) {
            try {
                realmSchema.create(name);
            } catch (IllegalArgumentException ignored) {
            }
            assertFalse(String.format("'%s' failed", name), realmSchema.contains(name));
        }
    }

    @Test
    public void create_duplicatedNameThrows() {
        realmSchema.create("Foo");
        thrown.expect(IllegalArgumentException.class);
        realmSchema.create("Foo");
    }

    @Test
    public void get() {
        RealmObjectSchema objectSchema = realmSchema.get(AllJavaTypes.CLASS_NAME);
        assertNotNull(objectSchema);
        assertEquals(AllJavaTypes.CLASS_NAME, objectSchema.getClassName());
    }

    @Test
    public void get_unknownClass() {
        assertNull(realmSchema.get("Foo"));
    }

    @Test
    public void rename() {
        realmSchema.rename("Owner", "Owner2");
        assertFalse(realmSchema.contains("Owner"));
        assertTrue(realmSchema.contains("Owner2"));
    }

    @Test
    public void rename_invalidArgumentThrows() {
        String[] illegalNames = new String[] { null, "" };

        // Tests as first parameter.
        for (String illegalName : illegalNames) {
            try {
                realmSchema.rename(illegalName, AllJavaTypes.CLASS_NAME);
                fail(illegalName + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Tests as last parameters.
        for (String illegalName : illegalNames) {
            try {
                realmSchema.rename(AllJavaTypes.CLASS_NAME, illegalName);
                fail(illegalName + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void rename_shouldChangeInfoInPKTable() {
        final String NEW_NAME = "NewPrimaryKeyAsString";
        assertTrue(realmSchema.contains(PrimaryKeyAsString.CLASS_NAME));
        realmSchema.rename(PrimaryKeyAsString.CLASS_NAME, NEW_NAME);
        assertFalse(realmSchema.contains(PrimaryKeyAsString.CLASS_NAME));
        assertTrue(realmSchema.contains(NEW_NAME));
        RealmObjectSchema objectSchema = realmSchema.getSchemaForClass(NEW_NAME);

        assertEquals(PrimaryKeyAsString.FIELD_PRIMARY_KEY, objectSchema.getPrimaryKey());

        // Creates an object with the old name, and the PK should not exist after created.
        RealmObjectSchema oldObjectSchema = realmSchema.create(PrimaryKeyAsString.CLASS_NAME);
        oldObjectSchema.addField(PrimaryKeyAsString.FIELD_PRIMARY_KEY, String.class);

        try {
            // It should not have primary key anymore at this point.
            oldObjectSchema.getPrimaryKey();
            fail();
        } catch (IllegalStateException ignored) {
        }

        oldObjectSchema.addPrimaryKey(PrimaryKeyAsString.FIELD_PRIMARY_KEY);
        assertEquals(PrimaryKeyAsString.FIELD_PRIMARY_KEY, oldObjectSchema.getPrimaryKey());
    }

    @Test
    public void remove() {
        realmSchema.remove(AllJavaTypes.CLASS_NAME);
        assertFalse(realmSchema.contains(AllJavaTypes.CLASS_NAME));
    }

    @Test
    public void remove_invalidArgumentThrows() {
        try {
            realmSchema.remove("Foo");
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            realmSchema.remove(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Test that it if { A -> B  && B -> A } you should remove the individual fields first before removing the entire
    // class. This also include transitive dependencies.
    @Test
    public void remove_classWithReferencesThrows() {
        try {
            realmSchema.remove("Cat");
            fail();
        } catch (IllegalStateException ignored) {
        }

        RealmObjectSchema ownerSchema = realmSchema.get("Owner");
        RealmObjectSchema catSchema = realmSchema.get("Cat");
        ownerSchema.removeField("cat");
        catSchema.removeField("owner");
        realmSchema.remove("Cat");
        assertFalse(realmSchema.contains("Cat"));
    }

    @Test
    public void remove_shouldRemoveInfoFromPKTable() {
        assertTrue(realmSchema.contains(PrimaryKeyAsString.CLASS_NAME));
        realmSchema.remove(PrimaryKeyAsString.CLASS_NAME);
        assertFalse(realmSchema.contains(PrimaryKeyAsString.CLASS_NAME));

        RealmObjectSchema objectSchema = realmSchema.create(PrimaryKeyAsString.CLASS_NAME);
        objectSchema.addField(PrimaryKeyAsString.FIELD_PRIMARY_KEY, String.class);

        try {
            // It should not have primary key anymore at this point.
            objectSchema.getPrimaryKey();
            fail();
        } catch (IllegalStateException ignored) {
        }

        objectSchema.addPrimaryKey(PrimaryKeyAsString.FIELD_PRIMARY_KEY);
        assertEquals(PrimaryKeyAsString.FIELD_PRIMARY_KEY, objectSchema.getPrimaryKey());
    }
}
