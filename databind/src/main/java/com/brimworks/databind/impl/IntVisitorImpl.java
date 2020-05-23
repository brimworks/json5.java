package com.brimworks.databind.impl;

import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Consumer;
import java.util.List;
import java.lang.reflect.Type;
import com.brimworks.databind.*;

public class IntVisitorImpl implements TypeVisitor, TypeVisitorFactory {
    private TypeBuilderContext context;
    private IntFactory factory;
    private TypeRegistry registry;
    private IntConsumer consumer;
    public IntVisitorImpl(TypeBuilderContext context, TypeRegistry registry, IntConsumer consumer) {
        if ( null == registry ) throw new NullPointerException("expected non-null registry");
        if ( null == consumer ) throw new NullPointerException("expected non-null consumer");
        if ( null == context ) throw new NullPointerException("expected non-null context");

        IntFactory factory = registry.getIntFactory();
        if ( null == factory ) {
            throw context.unsupportedType("No TypeFactory for int");
        }

        this.context = context;
        this.factory = factory;
        this.consumer = consumer;
        this.registry = registry;
    }

    @Override
    public ObjectVisitor visitObject(int size) {
        throw context.unsupportedType("Unexpected object");
    }
    @Override
    public ArrayVisitor visitArray(int size) {
        throw context.unsupportedType("Unexpected object");
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

    @Override
    public <U> TypeVisitor createVisitor(Type type, Consumer<U> save) {
        return new TypeVisitorImpl<U>(context, registry, type, save);
    }

    @Override
    public TypeVisitor createIntVisitor(IntConsumer save) {
        return new IntVisitorImpl(context, registry, save);
    }

    @Override
    public TypeVisitor createLongVisitor(LongConsumer save) {
        return new LongVisitorImpl(context, registry, save);
    }
}