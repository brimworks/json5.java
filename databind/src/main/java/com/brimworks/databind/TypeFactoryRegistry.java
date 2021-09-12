package com.brimworks.databind;

import java.lang.reflect.Type;

public interface TypeFactoryRegistry {
    @SuppressWarnings("unchecked")
    default <T> TypeFactory<T> getTypeFactory(Class<T> type) {
        return (TypeFactory<T>)getTypeFactory((Type)type);
    }
    default TypeFactory<?> getTypeFactory(Type type) {
        return null;
    }
    default BooleanFactory getBooleanFactory() {
        return null;
    }
    default LongFactory getLongFactory() {
        return null;
    }
    default IntFactory getIntFactory() {
        return null;
    }
    default ShortFactory getShortFactory() {
        return null;
    }
    default ByteFactory getByteFactory() {
        return null;
    }
    default CharFactory getCharFactory() {
        return null;
    }
    default DoubleFactory getDoubleFactory() {
        return null;
    }
    default FloatFactory getFloatFactory() {
        return null;
    }
}