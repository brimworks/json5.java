package com.brimworks.databind;

public interface IntFactory {
    default int create(Number value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected number");
    }
    default int create(String value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected string");
    }
    default int create(boolean value, TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected boolean");
    }
    default int createNull(TypeBuilderContext ctx) {
        throw ctx.unsupportedType("Unexpected null");
    }

    // PRIMITIVES, default to delegation.
    default int create(long value, TypeBuilderContext ctx) {
        return create(Long.valueOf(value), ctx);
    }
    default int create(int value, TypeBuilderContext ctx) {
        return create(Integer.valueOf(value), ctx);
    }
    default int create(short value, TypeBuilderContext ctx) {
        return create(Short.valueOf(value), ctx);
    }
    default int create(char value, TypeBuilderContext ctx) {
        return create(Character.valueOf(value), ctx);
    }
    default int create(byte value, TypeBuilderContext ctx) {
        return create(Byte.valueOf(value), ctx);
    }
    default int create(double value, TypeBuilderContext ctx) {
        return create(Double.valueOf(value), ctx);
    }
    default int create(float value, TypeBuilderContext ctx) {
        return create(Float.valueOf(value), ctx);
    }


}
