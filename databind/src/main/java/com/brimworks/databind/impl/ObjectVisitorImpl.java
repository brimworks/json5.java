package com.brimworks.databind.impl;

import com.brimworks.databind.ObjectBuilder;
import com.brimworks.databind.ObjectVisitor;
import com.brimworks.databind.TypeVisitor;
import com.brimworks.databind.TypeBuilderContext;
import java.util.function.Consumer;

public class ObjectVisitorImpl<T> implements ObjectVisitor {
    private TypeBuilderContext context;
    private ObjectBuilder<T> builder;
    private Consumer<T> consumer;
    public ObjectVisitorImpl(TypeBuilderContext context, ObjectBuilder<T> builder, Consumer<T> consumer) {
        this.context = context;
        this.builder = builder;
        this.consumer = consumer;
    }
    @Override
    public TypeVisitor put(String key) {
        TypeBuilderContext child = context.child(key);
        return builder.put(key, child);
    }
    @Override
    public void done() {
        consumer.accept(builder.build());
    }
}