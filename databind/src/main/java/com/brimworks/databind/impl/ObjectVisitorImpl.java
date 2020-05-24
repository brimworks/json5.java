package com.brimworks.databind.impl;

import java.util.Objects;
import java.util.function.Consumer;

import com.brimworks.databind.ObjectBuilder;
import com.brimworks.databind.ObjectVisitor;
import com.brimworks.databind.TypeBuilderContext;
import com.brimworks.databind.TypeVisitor;

public class ObjectVisitorImpl<T> implements ObjectVisitor {
    private TypeBuilderContext context;
    private ObjectBuilder<T> builder;
    private Consumer<T> consumer;
    public ObjectVisitorImpl(TypeBuilderContext context, ObjectBuilder<T> builder, Consumer<T> consumer) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(builder);
        Objects.requireNonNull(consumer);
        this.context = context;
        this.builder = builder;
        this.consumer = consumer;
    }
    @Override
    public TypeVisitor put(String key) {
        TypeBuilderContext child = context.createContext(context.getLocation().addKey(key));
        return builder.put(key, child);
    }
    @Override
    public void done() {
        consumer.accept(builder.build());
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ObjectVisitor{");
        sb.append(context.getLocation().toString());
        sb.append("}");
        return sb.toString();
    }
}