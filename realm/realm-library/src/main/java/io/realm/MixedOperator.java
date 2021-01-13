package io.realm;

import java.util.Collections;

import io.realm.internal.NativeContext;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.core.NativeMixed;


public abstract class MixedOperator {
    static MixedOperator fromNativeMixed(ProxyState<? extends RealmModel> proxyState, NativeMixed nativeMixed) {
        MixedType fieldType = nativeMixed.getType();

        switch (fieldType) {
            case BOOLEAN:
                return new BooleanMixedOperator(nativeMixed);
            case NULL:
                return new NullMixedOperator(nativeMixed);
            case OBJECT:
                return new RealmModelOperator(nativeMixed, proxyState);
            default:
                throw new ClassCastException("Couldn't cast to " + fieldType);
        }
    }

    private NativeMixed nativeMixed;

    private final Object value;
    private final MixedType type;

    private synchronized NativeMixed getNativeMixed() {
        if (nativeMixed == null) { nativeMixed = createNativeMixed(NativeContext.dummyContext); }

        return nativeMixed;
    }

    long getNativePtr() {
        return getNativeMixed().getNativePtr();
    }

    protected abstract NativeMixed createNativeMixed(NativeContext context);

    protected MixedOperator(Object value, MixedType type, NativeMixed nativeMixed) {
        this.value = value;
        this.type = type;
        this.nativeMixed = nativeMixed;
    }

    protected MixedOperator(Object value, MixedType type) {
        this(value, type, null);
    }

    protected MixedOperator(NativeMixed nativeMixed) {
        this(null, null, nativeMixed);
    }

    protected MixedOperator() {
        this(null, null, null);
    }

    <T> T getValue(Class<T> clazz) {
        return clazz.cast(value);
    }

    MixedType getType() {
        return type;
    }

    Class<?> getTypedClass() {
        return type.getTypedClass();
    }
}

final class BooleanMixedOperator extends MixedOperator {
    BooleanMixedOperator(Boolean value) {
        super(value, MixedType.BOOLEAN);
    }

    BooleanMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asBoolean(), MixedType.BOOLEAN, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed(NativeContext context) {
        return NativeMixed.newInstance(context, super.getValue(Boolean.class));
    }
}

final class NullMixedOperator extends MixedOperator {
    NullMixedOperator() {
        super(null, MixedType.NULL);
    }

    NullMixedOperator(NativeMixed nativeMixed) {
        super(null, MixedType.NULL, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed(NativeContext context) {
        return NativeMixed.newInstance(context);
    }

    @Override
    public <T> T getValue(Class<T> clazz) {
        return null;
    }
}

final class RealmModelOperator extends MixedOperator {
    private static <T extends RealmModel> Class<T> getModelClass(ProxyState<T> proxyState, NativeMixed nativeMixed) {
        OsSharedRealm sharedRealm = proxyState
                .getRealm$realm()
                .getSharedRealm();

        String className = Table.getClassNameForTable(nativeMixed.getRealmModelTableName(sharedRealm));

        return proxyState
                .getRealm$realm()
                .getConfiguration()
                .getSchemaMediator()
                .getClazz(className);
    }

    private static <T extends RealmModel> T getRealmModel(ProxyState<T> proxyState, Class<T> clazz, NativeMixed nativeMixed) {
        return proxyState
                .getRealm$realm()
                .get(clazz, nativeMixed.getRealmModelRowKey(), false, Collections.emptyList());
    }

    private final Class<? extends RealmModel> clazz;
    private final RealmModel value;

    RealmModelOperator(RealmModel realmModel) {
        this.value = realmModel;
        this.clazz = realmModel.getClass();
    }

    <T extends RealmModel> RealmModelOperator(NativeMixed nativeMixed, ProxyState<T> proxyState) {
        super(nativeMixed);

        Class<T> clazz = getModelClass(proxyState, nativeMixed);
        this.clazz = clazz;

        this.value = getRealmModel(proxyState, clazz, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed(NativeContext context) {
        return NativeMixed.newInstance(context, getValue(RealmObjectProxy.class));
    }

    @Override
    <T> T getValue(Class<T> clazz) {
        return clazz.cast(value);
    }

    @Override
    MixedType getType() {
        return MixedType.OBJECT;
    }

    @Override
    Class<?> getTypedClass() {
        return clazz;
    }
}
