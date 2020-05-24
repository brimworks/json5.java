package com.brimworks.databind;

public interface ShortFactory {
    default short create(Number value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected number");
    }
    default short create(String value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected string");
    }
    default short create(boolean value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected boolean");
    }
    default short createNull(TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected null");
    }

    // PRIMITIVES, default to delegation.
    default short create(long value, TypeBuilderContext ctx) {
        return create(Long.valueOf(value), ctx);
    }
    default short create(int value, TypeBuilderContext ctx) {
        return create(Integer.valueOf(value), ctx);
    }
    default short create(short value, TypeBuilderContext ctx) {
        return create(Short.valueOf(value), ctx);
    }
    default short create(char value, TypeBuilderContext ctx) {
        return create(Character.valueOf(value), ctx);
    }
    default short create(byte value, TypeBuilderContext ctx) {
        return create(Byte.valueOf(value), ctx);
    }
    default short create(double value, TypeBuilderContext ctx) {
        return create(Double.valueOf(value), ctx);
    }
    default short create(float value, TypeBuilderContext ctx) {
        return create(Float.valueOf(value), ctx);
    }
}
