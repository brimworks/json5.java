package com.brimworks.databind;

public interface TypeFactory<T> {
    default ArrayBuilder<T> createArray(int size, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected array");
    }
    default ObjectBuilder<T> createObject(int size, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected object");
    }
    default T create(Number value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected number");
    }
    default T create(String value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected string");
    }
    default T create(boolean value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected boolean");
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
