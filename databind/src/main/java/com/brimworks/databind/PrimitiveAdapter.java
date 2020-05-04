package com.brimworks.databind;

import java.lang.reflect.Type;

public enum PrimitiveAdapter {
    NULL(new TypeAdapter<Object>() {
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
    }),
    STRING(new TypeAdapter<String>() {
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
    }),
    /*
    INTEGER(new TypeAdapter<int>() {
        @Override
        public Type getRawType() {
            return Integer.TYPE;
        }

        @Override
        public int create(String string, TypeBuilderContext ctx) {
            return string;
        }

        @Override
        public int create(Number value, TypeBuilderContext ctx) {
            return value.toString();
        }

        @Override
        public int create(boolean value, TypeBuilderContext ctx) {
            return Boolean.toString(value);
        }

        @Override
        public void visit(String val, TypeVisitor visitor) {
            visitor.visit(val);
        }
    }),
    */
    ;

    private PrimitiveAdapter(TypeAdapter<?> adapter) {
        this.adapter = adapter;
    }
    private TypeAdapter<?> adapter;
    public TypeAdapter<?> getAdapter() {
        return adapter;
    }
}