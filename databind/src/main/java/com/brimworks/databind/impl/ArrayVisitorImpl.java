package com.brimworks.databind.impl;

import java.util.Objects;
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
        Objects.requireNonNull(context);
        Objects.requireNonNull(builder);
        Objects.requireNonNull(consumer);
        this.context = context;
        this.builder = builder;
        this.consumer = consumer;
        this.index = 0;
    }
    @Override
    public TypeVisitor add() {
        TypeBuilderContext child = context.createContext(context.getLocation().addIndex(index++));
        return builder.add(child);
    }
    @Override
    public void done() {
        consumer.accept(builder.build());
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ArrayVisitor{");
        sb.append("context=");
        sb.append(context.toString());
        sb.append("}");
        return sb.toString();
    }

}