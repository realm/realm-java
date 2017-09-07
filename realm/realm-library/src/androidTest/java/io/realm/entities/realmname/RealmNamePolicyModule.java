package io.realm.entities.realmname;

import io.realm.annotations.RealmModule;
import io.realm.annotations.RealmName;
import io.realm.annotations.RealmNamePolicy;

@RealmModule(classes = {
        ClassNameOverrideModulePolicy.class,
        ClassWithPolicy.class,
        DefaultPolicyFromModule.class,
        FieldNameOverrideClassPolicy.class })
@RealmName(policy = RealmNamePolicy.LOWER_CASE_WITH_UNDERSCORES)
public class RealmNamePolicyModule {

}
