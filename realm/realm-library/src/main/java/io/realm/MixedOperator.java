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
    private MixedType type;

    public NativeMixed getNativeMixed(NativeContext context) {
        if (nativeMixed == null) { nativeMixed = createNativeMixed(context); }

        return nativeMixed;
    }

    protected abstract NativeMixed createNativeMixed(NativeContext context);

    protected MixedOperator(Object value, MixedType type, NativeMixed nativeMixed) {
        this(value, type);

        this.nativeMixed = nativeMixed;
    }

    protected MixedOperator(Object value, MixedType type) {
        this.value = value;
        this.type = type;
    }

    public <T> T getValue(Class<T> clazz) {
        return clazz.cast(value);
    }

    public MixedType getType() {
        return type;
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
