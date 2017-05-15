//package io.realm;
//
//import android.support.test.runner.AndroidJUnit4
//import io.realm.entities.AllKotlinTypes
//import org.junit.Assert.assertFalse
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class RealmObjectTests{
//
//    @Test
//    fun supportKotlinNullability() {
//        val realm = Realm.getDefaultInstance()
//        var schema = realm.getSchema().get(AllKotlinTypes::class.simpleName)
//        try {
//            assertFalse(schema.isNullable("requiredString"))
//        } finally {
//            realm.close()
//        }
//    }
//}
