package com.brimworks.databind.impl;

import java.lang.reflect.Type;
import java.lang.reflect.GenericArrayType;
import java.util.function.Consumer;

import com.brimworks.databind.TypeFactory;
import com.brimworks.databind.TypeFactoryRegistry;
import com.brimworks.databind.VisitType;
import com.brimworks.databind.IntFactory;
import com.brimworks.databind.LongFactory;
import com.brimworks.databind.TypeAdapter;
import com.brimworks.databind.TypeAdapterRegistry;
import com.brimworks.databind.TypeBuilderContext;
import com.brimworks.databind.TypeRegistry;
import com.brimworks.databind.TypeVisitor;

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
    })), STRING(builder -> builder.put(new TypeAdapter<String>() {
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
    })), INTEGER(builder -> builder.put(new IntFactory() {
        @Override
        public int create(String string, TypeBuilderContext ctx) {
            return Integer.parseInt(string);
        }

        @Override
        public int create(Number value, TypeBuilderContext ctx) {
            return value.intValue();
        }

        @Override
        public int create(long value, TypeBuilderContext ctx) {
            try {
                return Math.toIntExact(value);
            } catch ( ArithmeticException ex ) {
                throw ctx.unsupportedType(ex);
            }
        }

        @Override
        public int create(int value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public int create(short value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public int create(char value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public int create(byte value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public int create(double value, TypeBuilderContext ctx) {
            try {
                return Math.toIntExact(Math.round(value));
            } catch ( ArithmeticException ex ) {
                throw ctx.unsupportedType(ex);
            }
        }

        @Override
        public int create(float value, TypeBuilderContext ctx) {
            return Math.round(value);
        }

        @Override
        public int create(boolean value, TypeBuilderContext ctx) {
            return value ? 1 : 0;
        }
    })), LONG(builder -> builder.put(new LongFactory() {
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
    // Lowest priority is first:
    STRUCT(builder -> builder.add(new StructAdapterRegistry())),
    ARRAY(builder -> builder.add(new ArrayAdapterRegistry())),
    COLLECTION(builder -> builder.add(new CollectionAdapterRegistry())),
    ;

    private PrimitiveAdapter(Consumer<TypeRegistry.Builder> consumer) {
        this.consumer = consumer;
    }

    private Consumer<TypeRegistry.Builder> consumer;

    public void apply(TypeRegistry.Builder builder) {
        consumer.accept(builder);
    }
}