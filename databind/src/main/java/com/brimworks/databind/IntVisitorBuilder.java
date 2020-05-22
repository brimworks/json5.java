package com.brimworks.databind;

import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Consumer;
import java.util.List;
import java.lang.reflect.Type;

public class IntVisitorBuilder implements TypeVisitor, TypeBuilderContext {
    private IntFactory factory;
    private TypeRegistry registry;
    private IntConsumer consumer;
    public IntVisitorBuilder(IntFactory factory, IntConsumer consumer, TypeRegistry registry) {
        this.factory = factory;
        this.consumer = consumer;
        this.registry = registry;
    }
    @Override
    public ObjectVisitor visitObject() {
        throw unexpectedType(Object.class);
    }
    @Override
    public ArrayVisitor visitArray() {
        throw unexpectedType(List.class);
    }
    @Override
    public void visit(Object val) {
        if ( val instanceof String ) {
            consumer.accept(factory.create((String)val, this));
        } else if ( val instanceof Number ) {
            consumer.accept(factory.create((Number)val, this));
        } else if ( null == val ) {
            consumer.accept(factory.createNull(this));
        } else { // Must adapt:
            VisitType visit = registry.getVisitType(val.getClass());
            IntVisitorBuilder builder = new IntVisitorBuilder(factory, consumer, registry);
            visit.visit(val, builder);
        }
    }

    @Override
    public void visit(boolean val) {
        consumer.accept(factory.create(val, this));
    }

    @Override
    public void visit(long val) {
        consumer.accept(factory.create(val, this));
    }

    @Override
    public void visit(int val) {
        consumer.accept(factory.create(val, this));
    }

    @Override
    public void visit(short val) {
        consumer.accept(factory.create(val, this));
    }

    @Override
    public void visit(char val) {
        consumer.accept(factory.create(val, this));
    }

    @Override
    public void visit(byte val) {
        consumer.accept(factory.create(val, this));
    }

    @Override
    public void visit(double val) {
        consumer.accept(factory.create(val, this));
    }

    @Override
    public void visit(float val) {
        consumer.accept(factory.create(val, this));
    }

    @Override
    public UnsupportedTypeError unexpectedType(Type type) {
        // FIXME: Improved error handling...
        return new UnsupportedTypeError("Unexpected type: "+type);
    }
    @Override
    public UnsupportedTypeError unexpectedType(Type type, Type src) {
        // FIXME: Improved error handling...
        return new UnsupportedTypeError("Unexpected type: "+type+" when trying to build "+src);
    }
    @Override
    public UnsupportedTypeError unexpectedKey(String key) {
        // FIXME: Improved error handling...
        return new UnsupportedTypeError("Unexpected key: "+key);
    }

    @Override
    public <U> TypeVisitor createVisitor(Type type, Consumer<U> save) {
        TypeFactory<U> factory = (TypeFactory<U>)registry.getTypeFactory(type);
        if ( null == factory ) {
            throw new UnsupportedTypeError("No factory for "+type);
        }
        return new VisitorBuilder<U>(factory, save, registry);
    }

    @Override
    public TypeVisitor createIntVisitor(IntConsumer save) {
        IntFactory factory = registry.getIntFactory();
        if ( null == factory ) {
            throw new UnsupportedTypeError("No factory for int");
        }
        return new IntVisitorBuilder(factory, save, registry);
    }

    @Override
    public TypeVisitor createLongVisitor(LongConsumer save) {
        // FIXME: Build lineage so we get better error tracing.
        LongFactory factory = registry.getLongFactory();
        if ( null == factory ) {
            throw new UnsupportedTypeError("No factory for long");
        }
        return new LongVisitorBuilder(factory, save, registry);
    }
}