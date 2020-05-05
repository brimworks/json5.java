package com.brimworks.databind;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public enum PrimitiveAdapter {
    NULL(builder -> builder.put(new TypeAdapter<Object>() {
        @Override
        public Type getRawType() {
            return null;
        }

        @Override
        public Object createNull(TypeBuilderContext ctx) {
            return null;
        }

        @Override
        public void visit(Object val, TypeVisitor visitor) {
            visitor.visit(null);
        }
    })),
    STRING(builder -> builder.put(new TypeAdapter<String>() {
        @Override
        public Type getRawType() {
            return String.class;
        }

        @Override
        public String create(String string, TypeBuilderContext ctx) {
            return string;
        }

        @Override
        public String create(Number value, TypeBuilderContext ctx) {
            return value.toString();
        }

        @Override
        public String create(boolean value, TypeBuilderContext ctx) {
            return Boolean.toString(value);
        }

        @Override
        public void visit(String val, TypeVisitor visitor) {
            visitor.visit(val);
        }
    })),
    INTEGER(builder -> builder.put(new IntFactory() {
        @Override
        public int create(String string, TypeBuilderContext ctx) {
            return Integer.parseInt(string);
        }

        @Override
        public int create(Number value, TypeBuilderContext ctx) {
            return value.intValue();
        }

        @Override
        public int create(boolean value, TypeBuilderContext ctx) {
            return value ? 1 : 0;
        }
    })),
    LONG(builder -> builder.put(new LongFactory() {
        @Override
        public long create(String string, TypeBuilderContext ctx) {
            return Long.parseLong(string);
        }

        @Override
        public long create(Number value, TypeBuilderContext ctx) {
            return value.longValue();
        }

        @Override
        public long create(boolean value, TypeBuilderContext ctx) {
            return value ? 1L : 0L;
        }
    })),
    ;

    private PrimitiveAdapter(Consumer<TypeRegistry.Builder> consumer) {
        this.consumer = consumer;
    }
    private Consumer<TypeRegistry.Builder> consumer;
    public void apply(TypeRegistry.Builder builder) {
        consumer.accept(builder);
    }
}