package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

open class MixedNotRequired : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var mixed: Mixed? = Mixed.valueOf(0)
}

@RunWith(AndroidJUnit4::class)
class MixedTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = RealmConfiguration
                .Builder(InstrumentationRegistry.getInstrumentation().targetContext)
                .directory(folder.newFolder())
                .schema(MixedNotRequired::class.java)
                .build()
        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun randomTest() {
        realm.executeTransaction {
            val a = realm.createObject<MixedNotRequired>(0)
            a.mixed = Mixed.valueOf(1)
        }

        val r = realm.where<MixedNotRequired>().findAll()
        val a = r.first()?.mixed?.asInteger()
    }
}