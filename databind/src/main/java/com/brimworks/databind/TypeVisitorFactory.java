package com.brimworks.databind;

import java.util.function.Consumer;
import java.lang.reflect.Type;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface TypeVisitorFactory {
    default <T> TypeVisitor createVisitor(Class<T> type, Consumer<T> save) {
        return createVisitor((Type)type, save);
    }
    <U> TypeVisitor createVisitor(Type type, Consumer<U> save);
    TypeVisitor createIntVisitor(IntConsumer save);
    TypeVisitor createLongVisitor(LongConsumer save);
}