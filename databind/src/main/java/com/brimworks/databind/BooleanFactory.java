package com.brimworks.databind;

public interface BooleanFactory {
    default boolean create(Number value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected number");
    }
    default boolean create(String value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected string");
    }
    default boolean create(boolean value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected boolean");
    }
    default boolean createNull(TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected null");
    }

    // PRIMITIVES, default to delegation.
    default boolean create(long value, TypeBuilderContext ctx) {
        return create(Long.valueOf(value), ctx);
    }
    default boolean create(int value, TypeBuilderContext ctx) {
        return create(Integer.valueOf(value), ctx);
    }
    default boolean create(short value, TypeBuilderContext ctx) {
        return create(Short.valueOf(value), ctx);
    }
    default boolean create(char value, TypeBuilderContext ctx) {
        return create(Character.valueOf(value), ctx);
    }
    default boolean create(byte value, TypeBuilderContext ctx) {
        return create(Byte.valueOf(value), ctx);
    }
    default boolean create(double value, TypeBuilderContext ctx) {
        return create(Double.valueOf(value), ctx);
    }
    default boolean create(float value, TypeBuilderContext ctx) {
        return create(Float.valueOf(value), ctx);
    }
}
