package com.brimworks.databind.impl;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import com.brimworks.databind.ArrayVisitor;
import com.brimworks.databind.ArrayBuilder;
import com.brimworks.databind.ObjectVisitor;
import com.brimworks.databind.ObjectBuilder;
import com.brimworks.databind.Builder;
import com.brimworks.databind.TypeBuilderContext;
import com.brimworks.databind.TypeFactory;
import com.brimworks.databind.TypeRegistry;
import com.brimworks.databind.TypeVisitor;
import com.brimworks.databind.TypeVisitorFactory;
import com.brimworks.databind.VisitType;

public class TypeVisitorImpl<T> extends AbstractVisitorImpl {
    private Consumer<T> consumer;
    private TypeFactory<T> factory;
    private Builder<T> builder;
    public TypeVisitorImpl(TypeRegistry registry, Type buildType, Consumer<T> consumer) {
        this(null, registry, buildType, consumer);
    }
    public TypeVisitorImpl(TypeBuilderContext context, TypeRegistry registry, Type buildType, Consumer<T> consumer) {
        super(context, registry);
        if ( null == registry ) throw new NullPointerException("expected non-null registry");
        if ( null == buildType ) throw new NullPointerException("expected non-null buildType");
        if ( null == consumer ) throw new NullPointerException("expected non-null consumer");
        if ( null == context ) {
            context = new TypeBuilderContextImpl(this, buildType);
        }
        TypeFactory<T> factory = (TypeFactory<T>)registry.getTypeFactory(buildType);
        if ( null == factory ) {
            throw context.unsupportedType("No TypeFactory for "+buildType);
        }

        this.context = context;
        this.factory = factory;
        this.consumer = consumer;
        this.registry = registry;
    }
    @Override
    public ObjectVisitor visitObject(int size) {
        ObjectBuilder<T> builder = factory.createObject(size, context);
        this.builder = builder;
        return new ObjectVisitorImpl<T>(context, builder, consumer);
    }
    @Override
    public ArrayVisitor visitArray(int size) {
        ArrayBuilder<T> builder = factory.createArray(size, context);
        this.builder = builder;
        return new ArrayVisitorImpl<T>(context, builder, consumer);
    }
    @Override
    public void visit(Object val) {
        if ( val instanceof String ) {
            consumer.accept(factory.create((String)val, context));
        } else if ( val instanceof Number ) {
            consumer.accept(factory.create((Number)val, context));
        } else if ( null == val ) {
            consumer.accept(factory.createNull(context));
        } else { // Must adapt via a different visitor:
            VisitType<Object> visit = (VisitType<Object>)registry.getVisitType(val.getClass());
            visit.visit(val, this);
        }
    }
    @Override
    public void visit(boolean val) {
        consumer.accept(factory.create(val, context));
    }
    @Override
    public void visit(long val) {
        consumer.accept(factory.create(val, context));
    }
    @Override
    public void visit(int val) {
        consumer.accept(factory.create(val, context));
    }
    @Override
    public void visit(short val) {
        consumer.accept(factory.create(val, context));
    }
    @Override
    public void visit(char val) {
        consumer.accept(factory.create(val, context));
    }
    @Override
    public void visit(byte val) {
        consumer.accept(factory.create(val, context));
    }
    @Override
    public void visit(double val) {
        consumer.accept(factory.create(val, context));
    }
    @Override
    public void visit(float val) {
        consumer.accept(factory.create(val, context));
    }
}