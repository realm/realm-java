package io.realm;

import android.content.Context;


public class RealmDebugConfigurationBuilder extends RealmConfiguration.Builder {
    public RealmDebugConfigurationBuilder() {
        super();
    }

    public RealmDebugConfigurationBuilder(Context context) {
        super(context);
    }

    public RealmConfiguration.Builder setSchema(Class<? extends RealmModel> firstClass, Class<? extends RealmModel>... additionalClasses){
        this.schema(firstClass, additionalClasses);
        return this;
    }
}
