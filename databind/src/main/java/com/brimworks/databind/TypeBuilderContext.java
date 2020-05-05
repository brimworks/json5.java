package com.brimworks.databind;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.lang.reflect.Type;

public interface TypeBuilderContext {
    UnsupportedTypeError unexpectedType(Type type);
    UnsupportedTypeError unexpectedKey(String key);
    
    default <T> TypeVisitor createVisitor(Class<T> type, Consumer<T> save) {
        return createVisitor((Type)type, save);
    }

    <T> TypeVisitor createVisitor(Type type, Consumer<T> save);

    // Primitives:
    TypeVisitor createIntVisitor(IntConsumer save);
    TypeVisitor createLongVisitor(LongConsumer save);
}