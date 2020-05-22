package com.brimworks.databind;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.lang.reflect.Type;

public class VisitorBuilder<T> implements TypeVisitor, TypeBuilderContext {
    private TypeFactory<T> factory;
    private ObjectVisitorBuilder<T> objectBuilder;
    private ArrayVisitorBuilder<T> arrayBuilder;
    private TypeRegistry registry;
    private Consumer<T> consumer;
    public VisitorBuilder(TypeFactory<T> factory, Consumer<T> consumer, TypeRegistry registry) {
        if ( null == factory ) throw new NullPointerException("expected non-null factory");
        if ( null == consumer ) throw new NullPointerException("expected non-null consumer");
        if ( null == registry ) throw new NullPointerException("expected non-null registry");
        this.factory = factory;
        this.consumer = consumer;
        this.registry = registry;
    }
    @Override
    public ObjectVisitor visitObject() {
        objectBuilder = factory.createObject(this);
        return objectBuilder;
    }
    @Override
    public ArrayVisitor visitArray() {
        arrayBuilder = factory.createArray(this);
        return arrayBuilder;
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
            VisitorBuilder<T> builder = new VisitorBuilder<>(factory, consumer, registry);
            visit.visit(val, builder);
            builder.visitFinish();
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
    public void visitFinish() {
        if ( null != objectBuilder ) {
            consumer.accept(objectBuilder.build());
        } else if ( null != arrayBuilder ) {
            consumer.accept(arrayBuilder.build());
        }
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
        // FIXME: Build lineage so we get better error tracing.
        TypeFactory<U> factory = (TypeFactory<U>)registry.getTypeFactory(type);
        if ( null == factory ) {
            throw new UnsupportedTypeError("No factory for "+type);
        }
        return new VisitorBuilder<U>(factory, save, registry);
    }

    @Override
    public TypeVisitor createIntVisitor(IntConsumer save) {
        // FIXME: Build lineage so we get better error tracing.
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