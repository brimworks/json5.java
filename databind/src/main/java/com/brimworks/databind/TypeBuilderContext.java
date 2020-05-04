package com.brimworks.databind;

import java.util.function.Consumer;
import java.lang.reflect.Type;

public interface TypeBuilderContext {
    UnsupportedTypeError unexpectedType(Type type);
    UnsupportedTypeError unexpectedKey(String key);
    
    default <T> TypeVisitor createVisitor(Class<T> type, Consumer<T> save) {
        return createVisitor((Type)type, save);
    }

    <T> TypeVisitor createVisitor(Type type, Consumer<T> save);
}