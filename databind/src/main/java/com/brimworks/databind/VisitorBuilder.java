package com.brimworks.databind;

import java.util.function.Consumer;
import java.lang.reflect.Type;

public class VisitorBuilder<T> implements TypeVisitor, TypeBuilderContext {
    private TypeFactory<T> factory;
    private ObjectVisitorBuilder<T> objectBuilder;
    private ArrayVisitorBuilder<T> arrayBuilder;
    private TypeRegistry registry;
    private Consumer<T> consumer;
    public VisitorBuilder(TypeFactory<T> factory, Consumer<T> consumer, TypeRegistry registry) {
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

            // FIXME: Why do we need to cast this?
            VisitType<Object> visit = registry.getVisitType((Class<Object>)val.getClass());
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
    public UnsupportedTypeError unexpectedKey(String key) {
        // FIXME: Improved error handling...
        return new UnsupportedTypeError("Unexpected key: "+key);
    }

    @Override
    public <T> TypeVisitor createVisitor(Type type, Consumer<T> save) {
        TypeFactory<T> factory = (TypeFactory<T>)registry.getTypeFactory(type);
        if ( null == factory ) {
            throw new UnsupportedTypeError("No factory for "+type);
        }
        return new VisitorBuilder<>(factory, save, registry);
    }
}