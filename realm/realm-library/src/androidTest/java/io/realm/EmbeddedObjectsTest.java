package io.realm;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import io.realm.entities.embedded.EmbeddedSimpleChild;
import io.realm.entities.embedded.EmbeddedSimpleListParent;
import io.realm.entities.embedded.EmbeddedTreeNode;
import io.realm.entities.embedded.EmbeddedTreeParent;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Class testing Embedded Objects
 */
@RunWith(AndroidJUnit4.class)
public class EmbeddedObjectsTest {

    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    private RealmConfiguration realmConfig;
    private Realm realm;

    @Before
    public void setUp() {
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        realm.close();
    }

    @Test
    public void queryEmbeddedObjectsThrows() {
        try {
            realm.where(EmbeddedSimpleChild.class);
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void createObject_noParentThrows() {
        // When using createObject, the parent Object must be provided
        realm.beginTransaction();
        try {
            realm.createObject(EmbeddedSimpleChild.class);
        } catch (IllegalArgumentException ignore) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void createObject_throwsIfParentHasMultipleFields() {
        // createObject is an akward API to use for Embedded Objects, so it doesn't support
        // parent objects which has multiple properties linking to it.
        realm.beginTransaction();
        try {
            EmbeddedTreeParent parent = realm.createObject(EmbeddedTreeParent.class, UUID.randomUUID().toString());
            realm.createObject(EmbeddedTreeNode.class, parent);
        } catch (IllegalArgumentException ignore) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void createObject_simpleSingleChild() {

    }

    @Test
    public void createObject_simpleChildList() {
    }

    @Test
    public void createObject_addToEndOfParentList() {
        // If the only link a parent has to an embedded child, the child is added to the end of
        // the children
        realm.beginTransaction();
        try {
            EmbeddedSimpleListParent parent = realm.createObject(EmbeddedSimpleListParent.class, UUID.randomUUID().toString());
            parent.children.add(new EmbeddedSimpleChild("1"));
            realm.createObject(EmbeddedSimpleChild.class, parent);
            assertEquals(2, parent.children.size());
            assertNotEquals("1", parent.children.last());
        } catch (IllegalArgumentException ignore) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void createObject_treeSchema() {

    }

    @Test
    public void createObject_circularSchema() {

    }

    @Test
    public void copyToRealm_noParentThrows() {

    }

    @Test
    public void copyToRealmOrUpdate_throws() {

    }

    @Test
    public void copyToRealm_simpleSingleChild() {
        realm.beginTransaction();
        realm.commitTransaction();
    }

    @Test
    public void copyToRealm_simpleChildList() {
        realm.beginTransaction();
        realm.commitTransaction();
    }

    @Test
    public void copyToRealm_treeSchema() {

    }

    @Test
    public void copyToRealm_circularSchema() {

    }

    @Test
    public void copyToRealmOrUpdate_deletesOldEmbeddedObject() {

    }

    @Test
    public void insert_noParentThrows() {

    }

    @Test
    public void insertOrUpdate_throws() {

    }

    @Test
    public void insert_simpleSingleChild() {
        realm.beginTransaction();
        realm.commitTransaction();
    }

    @Test
    public void insert_simpleChildList() {
        realm.beginTransaction();
        realm.commitTransaction();
    }

    @Test
    public void insert_treeSchema() {

    }

    @Test
    public void insert_circularSchema() {

    }

    @Test
    public void insertOrUpdate_deletesOldEmbeddedObject() {

    }


    @Test
    public void settingParentFieldDeletesChild() {

    }

    @Test
    public void realmObjectSchema_setEmbedded() {

    }

    @Test
    public void realmObjectSchema_isEmbedded() {

    }
}
