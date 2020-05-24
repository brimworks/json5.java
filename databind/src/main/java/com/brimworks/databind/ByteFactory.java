package com.brimworks.databind;

public interface ByteFactory {
    default byte create(Number value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected number");
    }
    default byte create(String value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected string");
    }
    default byte create(boolean value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected boolean");
    }
    default byte createNull(TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected null");
    }

    // PRIMITIVES, default to delegation.
    default byte create(long value, TypeBuilderContext ctx) {
        return create(Long.valueOf(value), ctx);
    }
    default byte create(int value, TypeBuilderContext ctx) {
        return create(Integer.valueOf(value), ctx);
    }
    default byte create(short value, TypeBuilderContext ctx) {
        return create(Short.valueOf(value), ctx);
    }
    default byte create(char value, TypeBuilderContext ctx) {
        return create(Character.valueOf(value), ctx);
    }
    default byte create(byte value, TypeBuilderContext ctx) {
        return create(Byte.valueOf(value), ctx);
    }
    default byte create(double value, TypeBuilderContext ctx) {
        return create(Double.valueOf(value), ctx);
    }
    default byte create(float value, TypeBuilderContext ctx) {
        return create(Float.valueOf(value), ctx);
    }
}
