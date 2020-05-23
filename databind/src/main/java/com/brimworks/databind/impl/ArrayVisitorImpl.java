package com.brimworks.databind.impl;

import com.brimworks.databind.ArrayBuilder;
import com.brimworks.databind.ArrayVisitor;
import com.brimworks.databind.TypeVisitor;
import com.brimworks.databind.TypeBuilderContext;
import java.util.function.Consumer;

public class ArrayVisitorImpl<T> implements ArrayVisitor {
    private Consumer<T> consumer;
    private TypeBuilderContext context;
    private ArrayBuilder<T> builder;
    private int index;
    public ArrayVisitorImpl(TypeBuilderContext context, ArrayBuilder<T> builder, Consumer<T> consumer) {
        this.context = context;
        this.builder = builder;
        this.consumer = consumer;
        this.index = 0;
    }
    @Override
    public TypeVisitor add() {
        return builder.add(context.child(index++));
    }
    @Override
    public void done() {
        consumer.accept(builder.build());
    }
}