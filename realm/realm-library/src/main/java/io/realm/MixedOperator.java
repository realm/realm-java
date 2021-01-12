package io.realm;

import io.realm.internal.NativeContext;
import io.realm.internal.core.NativeMixed;


public abstract class MixedOperator {
    public static MixedOperator fromNativeMixed(NativeMixed nativeMixed) {
        MixedType fieldType = nativeMixed.getType();

        switch (fieldType) {
            case BOOLEAN:
                return new BooleanMixedOperator(nativeMixed);
            default:
                throw new ClassCastException("Couldn't cast to " + fieldType);
        }
    }

    private NativeMixed nativeMixed;
    private Object value;

    public NativeMixed getNativeMixed(NativeContext context) {
        if (nativeMixed == null) { nativeMixed = createNativeMixed(context); }

        return nativeMixed;
    }

    protected abstract NativeMixed createNativeMixed(NativeContext context);

    public <T> T getValue(Class<T> clazz) {
        return clazz.cast(value);
    }

    protected MixedOperator(Object value, NativeMixed nativeMixed) {
        this(value);
        this.nativeMixed = nativeMixed;
    }

    protected MixedOperator(Object value) {
        this.value = value;
    }
}

final class BooleanMixedOperator extends MixedOperator {
    BooleanMixedOperator(Boolean value) {
        super(value);
    }

    BooleanMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asBoolean(), nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed(NativeContext context) {
        return NativeMixed.newInstance(context, super.getValue(Boolean.class));
    }
}

final class NullMixedOperator extends MixedOperator {
    NullMixedOperator() {
        super(null);
    }

    NullMixedOperator(NativeMixed nativeMixed) {
        super(null, nativeMixed);
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
