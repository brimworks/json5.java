package com.brimworks.databind.impl;

import java.util.function.*;
import java.util.List;
import java.lang.reflect.Type;
import com.brimworks.databind.*;

public abstract class AbstractVisitorImpl implements TypeVisitor, TypeVisitorFactory {
    protected TypeBuilderContext context;
    protected TypeRegistry registry;

    public AbstractVisitorImpl(TypeBuilderContext context, TypeRegistry registry) {
        if ( null == registry ) throw new NullPointerException("expected non-null registry");

        // Hacky: Can't init the context in TypeVisitorImpl since it requires referencing `this`.
        if ( null == context && !(this instanceof TypeVisitorImpl)) {
            throw new NullPointerException("expected non-null context");
        }

        this.context = context;
        this.registry = registry;
    }

    @Override
    public ObjectVisitor visitObject(int size) {
        throw context.unsupportedType("Unexpected object");
    }

    @Override
    public ArrayVisitor visitArray(int size) {
        throw context.unsupportedType("Unexpected array");
    }

    @Override
    public <U> TypeVisitor createVisitor(Type type, Consumer<U> save) {
        return new TypeVisitorImpl<U>(context, registry, type, save);
    }

    @Override
    public TypeVisitor createBooleanVisitor(BooleanConsumer save) {
        return new BooleanVisitorImpl(context, registry, save);
    }

    @Override
    public TypeVisitor createLongVisitor(LongConsumer save) {
        return new LongVisitorImpl(context, registry, save);
    }

    @Override
    public TypeVisitor createIntVisitor(IntConsumer save) {
        return new IntVisitorImpl(context, registry, save);
    }

    @Override
    public TypeVisitor createShortVisitor(ShortConsumer save) {
        return new ShortVisitorImpl(context, registry, save);
    }

    @Override
    public TypeVisitor createByteVisitor(ByteConsumer save) {
        return new ByteVisitorImpl(context, registry, save);
    }

    @Override
    public TypeVisitor createCharVisitor(CharConsumer save) {
        return new CharVisitorImpl(context, registry, save);
    }

    @Override
    public TypeVisitor createDoubleVisitor(DoubleConsumer save) {
        return new DoubleVisitorImpl(context, registry, save);
    }

    @Override
    public TypeVisitor createFloatVisitor(FloatConsumer save) {
        return new FloatVisitorImpl(context, registry, save);
    }

}