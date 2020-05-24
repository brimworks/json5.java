package com.brimworks.databind;

public interface CharFactory {
    default char create(Number value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected number");
    }
    default char create(String value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected string");
    }
    default char create(boolean value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected boolean");
    }
    default char createNull(TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected null");
    }

    // PRIMITIVES, default to delegation.
    default char create(long value, TypeBuilderContext ctx) {
        return create(Long.valueOf(value), ctx);
    }
    default char create(int value, TypeBuilderContext ctx) {
        return create(Integer.valueOf(value), ctx);
    }
    default char create(short value, TypeBuilderContext ctx) {
        return create(Short.valueOf(value), ctx);
    }
    default char create(char value, TypeBuilderContext ctx) {
        return create(Character.valueOf(value), ctx);
    }
    default char create(byte value, TypeBuilderContext ctx) {
        return create(Byte.valueOf(value), ctx);
    }
    default char create(double value, TypeBuilderContext ctx) {
        return create(Double.valueOf(value), ctx);
    }
    default char create(float value, TypeBuilderContext ctx) {
        return create(Float.valueOf(value), ctx);
    }
}
