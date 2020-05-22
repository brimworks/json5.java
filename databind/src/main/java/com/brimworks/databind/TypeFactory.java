package com.brimworks.databind;

import java.util.List;
import java.lang.reflect.Type;

public interface TypeFactory<T> {
    Type getRawType();
    default ArrayVisitorBuilder<T> createArray(TypeBuilderContext ctx) {
        throw ctx.unexpectedType(List.class, getRawType());
    }
    default ObjectVisitorBuilder<T> createObject(TypeBuilderContext ctx) {
        throw ctx.unexpectedType(Object.class, getRawType());
    }
    default T create(Number value, TypeBuilderContext ctx) {
        throw ctx.unexpectedType(Number.class, getRawType());
    }
    default T create(String value, TypeBuilderContext ctx) {
        throw ctx.unexpectedType(String.class, getRawType());
    }
    default T create(boolean value, TypeBuilderContext ctx) {
        throw ctx.unexpectedType(Boolean.class, getRawType());
    }

    default T createNull(TypeBuilderContext ctx) {
        return null;
    }

    // PRIMITIVES, default to delegation.
    default T create(long value, TypeBuilderContext ctx) {
        return create(Long.valueOf(value), ctx);
    }
    default T create(int value, TypeBuilderContext ctx) {
        return create(Integer.valueOf(value), ctx);
    }
    default T create(short value, TypeBuilderContext ctx) {
        return create(Short.valueOf(value), ctx);
    }
    default T create(char value, TypeBuilderContext ctx) {
        return create(Character.valueOf(value), ctx);
    }
    default T create(byte value, TypeBuilderContext ctx) {
        return create(Byte.valueOf(value), ctx);
    }
    default T create(double value, TypeBuilderContext ctx) {
        return create(Double.valueOf(value), ctx);
    }
    default T create(float value, TypeBuilderContext ctx) {
        return create(Float.valueOf(value), ctx);
    }
}
