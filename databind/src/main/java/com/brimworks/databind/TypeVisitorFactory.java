package com.brimworks.databind;

import java.util.function.Consumer;
import java.lang.reflect.Type;
import java.util.function.*;

public interface TypeVisitorFactory {
    default <T> TypeVisitor createVisitor(Class<T> type, Consumer<T> save) {
        return createVisitor((Type)type, save);
    }
    <U> TypeVisitor createVisitor(Type type, Consumer<U> save);
    TypeVisitor createBooleanVisitor(BooleanConsumer save);
    TypeVisitor createLongVisitor(LongConsumer save);
    TypeVisitor createIntVisitor(IntConsumer save);
    TypeVisitor createShortVisitor(ShortConsumer save);
    TypeVisitor createByteVisitor(ByteConsumer save);
    TypeVisitor createCharVisitor(CharConsumer save);
    TypeVisitor createDoubleVisitor(DoubleConsumer save);
    TypeVisitor createFloatVisitor(FloatConsumer save);
    TypeVisitorFactory addContext(TypeBuilderContext ctx);
}