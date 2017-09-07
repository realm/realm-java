package io.realm;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.realmname.ClassNameOverrideModulePolicy;
import io.realm.entities.realmname.ClassWithPolicy;
import io.realm.entities.realmname.FieldNameOverrideClassPolicy;
import io.realm.entities.realmname.FieldWithPolicy;
import io.realm.entities.realmname.RealmNamePolicyModule;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RealmNameTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    private Realm realm;

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    // Check that the module policy is used as the default for class and field names
    @Test
    @Ignore("Not supported yet")
    public void modulePolicy_defaultPolicy() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .modules(new RealmNamePolicyModule())
                .build();
        realm = Realm.getInstance(config);

        assertTrue(realm.getSchema().contains("default-policy-from-module"));
        RealmObjectSchema classSchema = realm.getSchema().get("default-policy-from-module");
        assertTrue(classSchema.hasField("camel-case"));
    }

    @Test
    public void classPolicy_overrideModulePolicy() {
        // Default policy is IDENTITY -> FIXME: Not true anymore
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .schema(ClassWithPolicy.class)
                .build();
        realm = Realm.getInstance(config);

        assertTrue(realm.getSchema().contains(ClassWithPolicy.CLASS_NAME));
        RealmObjectSchema classSchema = realm.getSchema().get(ClassWithPolicy.CLASS_NAME);
        for (String field : ClassWithPolicy.ALL_FIELDS) {
            assertTrue(field + " was not found.", classSchema.hasField(field));
        }
    }

    @Test
    public void fieldPolicy_overrideClassPolicy() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .schema(FieldWithPolicy.class)
                .build();
        realm = Realm.getInstance(config);

        assertTrue(realm.getSchema().contains(FieldWithPolicy.CLASS_NAME));
        RealmObjectSchema classSchema = realm.getSchema().get(FieldWithPolicy.CLASS_NAME);
        assertTrue(classSchema.hasField(FieldWithPolicy.FIELD_CAMEL_CASE));
    }

    @Test
    public void className_overrideModulePolicy() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .modules(new RealmNamePolicyModule())
                .build();
        realm = Realm.getInstance(config);

        assertTrue(realm.getSchema().contains(ClassNameOverrideModulePolicy.CLASS_NAME));
    }

    @Test
    public void classNameOnly_doNotOverrideModulePolicy() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .modules(new RealmNamePolicyModule())
                .build();
        realm = Realm.getInstance(config);

        assertTrue(realm.getSchema().contains(ClassNameOverrideModulePolicy.CLASS_NAME));
    }

    @Test
    public void fieldName_overrideClassPolicy() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .modules(new RealmNamePolicyModule())
                .build();
        realm = Realm.getInstance(config);

        RealmObjectSchema classSchema = realm.getSchema().get(FieldNameOverrideClassPolicy.CLASS_NAME);
        assertTrue(classSchema.hasField(FieldNameOverrideClassPolicy.FIELD_CAMEL_CASE));
    }


    // FIXME Query tests

}
