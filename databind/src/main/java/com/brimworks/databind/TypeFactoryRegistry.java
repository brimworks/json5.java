package com.brimworks.databind;

import java.lang.reflect.Type;

public interface TypeFactoryRegistry {
    default <T> TypeFactory<T> getTypeFactory(Class<T> type) {
        return (TypeFactory<T>)getTypeFactory((Type)type);
    }
    TypeFactory<?> getTypeFactory(Type type);
}