package com.brimworks.databind;

public interface DoubleFactory {
    default double create(Number value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected number");
    }
    default double create(String value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected string");
    }
    default double create(boolean value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected boolean");
    }
    default double createNull(TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected null");
    }

    // PRIMITIVES, default to delegation.
    default double create(long value, TypeBuilderContext ctx) {
        return create(Long.valueOf(value), ctx);
    }
    default double create(int value, TypeBuilderContext ctx) {
        return create(Integer.valueOf(value), ctx);
    }
    default double create(short value, TypeBuilderContext ctx) {
        return create(Short.valueOf(value), ctx);
    }
    default double create(char value, TypeBuilderContext ctx) {
        return create(Character.valueOf(value), ctx);
    }
    default double create(byte value, TypeBuilderContext ctx) {
        return create(Byte.valueOf(value), ctx);
    }
    default double create(double value, TypeBuilderContext ctx) {
        return create(Double.valueOf(value), ctx);
    }
    default double create(float value, TypeBuilderContext ctx) {
        return create(Float.valueOf(value), ctx);
    }
}
