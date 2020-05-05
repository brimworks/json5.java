package com.brimworks.databind;

public interface LongFactory {
    default long create(Number value, TypeBuilderContext ctx) {
        throw ctx.unexpectedType(Number.class);
    }
    default long create(String value, TypeBuilderContext ctx) {
        throw ctx.unexpectedType(String.class);
    }
    default long create(boolean value, TypeBuilderContext ctx) {
        throw ctx.unexpectedType(Boolean.class);
    }

    default long createNull(TypeBuilderContext ctx) {
        throw ctx.unexpectedType(null);
    }

    // PRIMITIVES, default to delegation.
    default long create(long value, TypeBuilderContext ctx) {
        return create(Long.valueOf(value), ctx);
    }
    default long create(int value, TypeBuilderContext ctx) {
        return create(Integer.valueOf(value), ctx);
    }
    default long create(short value, TypeBuilderContext ctx) {
        return create(Short.valueOf(value), ctx);
    }
    default long create(char value, TypeBuilderContext ctx) {
        return create(Character.valueOf(value), ctx);
    }
    default long create(byte value, TypeBuilderContext ctx) {
        return create(Byte.valueOf(value), ctx);
    }
    default long create(double value, TypeBuilderContext ctx) {
        return create(Double.valueOf(value), ctx);
    }
    default long create(float value, TypeBuilderContext ctx) {
        return create(Float.valueOf(value), ctx);
    }
}
