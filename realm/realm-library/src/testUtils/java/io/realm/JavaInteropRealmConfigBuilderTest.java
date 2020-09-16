package io.realm;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import io.realm.kotlin.config.builder.ImmutableNamedParametersRealmConfigBuilder;
import io.realm.kotlin.config.builder.MutableNamedParametersRealmConfigBuilder;
import io.realm.kotlin.config.builder.TraditionalRealmConfigBuilder;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class JavaInteropRealmConfigBuilderTest {

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void javaLikeBuilderTest() {
        byte[] key = TestHelper.getRandomKey();

        // Create a config with name, key, schemaVersion, migration and deleteRealmIfMigrationNeeded
        RealmConfiguration config = new TraditionalRealmConfigBuilder(context)
                .name("foo.realm")
                .encryptionKey(key)
                .schemaVersion(42)
                .migration((realm, oldVersion, newVersion) -> { /* no-op */ })
                .deleteRealmIfMigrationNeeded()
                .build();

        assertEquals("foo.realm", config.getRealmFileName());
        assertEquals(42L, config.getSchemaVersion());
        assertEquals(Arrays.hashCode(key), Arrays.hashCode(config.getEncryptionKey()));
        assertNotNull(config.getMigration());
        assertTrue(config.shouldDeleteRealmIfMigrationNeeded());
    }

    @Test
    public void immutableBuilderTest() {
        byte[] key = TestHelper.getRandomKey();

        // Create a config with name, key, schemaVersion, migration and deleteRealmIfMigrationNeeded
        RealmConfiguration config = new ImmutableNamedParametersRealmConfigBuilder(
                context,
                context.getFilesDir(),          // need to actively pass this value since defaults don't work
                "foo.realm",
                null,                           // need to actively pass this value since defaults don't work
                key,
                42,
                (realm, oldVersion, newVersion) -> { /* no-op */ },
                true
        ).build();

        assertEquals("foo.realm", config.getRealmFileName());
        assertEquals(42L, config.getSchemaVersion());
        assertEquals(key.length, config.getEncryptionKey().length);
        assertNotNull(config.getMigration());
        assertTrue(config.shouldDeleteRealmIfMigrationNeeded());
    }

    @Test
    public void dslBuilderTest() {
        // Create a config with name, key, schemaVersion, migration and deleteRealmIfMigrationNeeded
        // using anonymous inner classes
        byte[] key = TestHelper.getRandomKey();
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                // no-op
            }
        };

        RealmConfiguration config =
                MutableNamedParametersRealmConfigBuilder.Companion.mutableRealmConfigBuilder(
                        context,
                        new Function1<MutableNamedParametersRealmConfigBuilder, Unit>() {
                            @Override
                            public Unit invoke(MutableNamedParametersRealmConfigBuilder builder) {
                                builder.fileName(new Function0<String>() {
                                    @Override
                                    public String invoke() {
                                        return "foo.realm";
                                    }
                                });
                                builder.key(new Function0<byte[]>() {
                                    @Override
                                    public byte[] invoke() {
                                        return key;
                                    }
                                });
                                builder.schemaVersion(new Function0<Long>() {
                                    @Override
                                    public Long invoke() {
                                        return 42L;
                                    }
                                });
                                builder.migration(new Function0<RealmMigration>() {
                                    @Override
                                    public RealmMigration invoke() {
                                        return migration;
                                    }
                                });
                                builder.deleteRealmIfMigrationNeeded(new Function0<Boolean>() {
                                    @Override
                                    public Boolean invoke() {
                                        return true;
                                    }
                                });
                                return null;
                            }
                        }
                );

        // Create a config with name, key, schemaVersion, migration and deleteRealmIfMigrationNeeded
        // using lambdas
        RealmConfiguration configLambdas =
                MutableNamedParametersRealmConfigBuilder.Companion.mutableRealmConfigBuilder(
                        context,
                        (Function1<MutableNamedParametersRealmConfigBuilder, Unit>) builder -> {
                            builder.fileName(() -> "foo.realm");
                            builder.key(() -> key);
                            builder.schemaVersion(() -> 42L);
                            builder.migration(() -> migration);
                            builder.deleteRealmIfMigrationNeeded(() -> true);
                            return null;
                        }
                );

        assertEquals(config, configLambdas);

        assertEquals("foo.realm", config.getRealmFileName());
        assertEquals(42L, config.getSchemaVersion());
        assertEquals(key.length, config.getEncryptionKey().length);
        assertNotNull(config.getMigration());
        assertTrue(config.shouldDeleteRealmIfMigrationNeeded());

        assertEquals("foo.realm", configLambdas.getRealmFileName());
        assertEquals(42L, configLambdas.getSchemaVersion());
        assertEquals(key.length, configLambdas.getEncryptionKey().length);
        assertNotNull(configLambdas.getMigration());
        assertTrue(configLambdas.shouldDeleteRealmIfMigrationNeeded());
    }
}
