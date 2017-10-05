package io.realm.entities.realmname;

import io.realm.annotations.RealmModule;
import io.realm.annotations.RealmNamingPolicy;

@RealmModule(classes = {
        ClassNameOverrideModulePolicy.class,
        ClassWithPolicy.class,
        DefaultPolicyFromModule.class,
        FieldNameOverrideClassPolicy.class },
        classNamingPolicy = RealmNamingPolicy.LOWER_CASE_WITH_UNDERSCORES,
        fieldNamingPolicy = RealmNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
)
public class CustomRealmNamesModule {

}
