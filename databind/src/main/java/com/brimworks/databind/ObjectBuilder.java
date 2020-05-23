package com.brimworks.databind;

public interface ObjectBuilder<T> extends Builder<T> {
    TypeVisitor put(String key, TypeBuilderContext ctx);
}