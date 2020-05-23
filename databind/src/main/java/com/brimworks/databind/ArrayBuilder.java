package com.brimworks.databind;

public interface ArrayBuilder<T> extends Builder<T> {
    TypeVisitor add(TypeBuilderContext ctx);
}