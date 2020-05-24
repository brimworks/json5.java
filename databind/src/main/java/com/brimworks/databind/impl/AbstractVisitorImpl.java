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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String name = getClass().getName();
        int dot = name.lastIndexOf('.');
        sb.append(name.substring(dot+1));
        sb.append("{");
        sb.append("context=");
        sb.append(context.toString());
        sb.append("}");
        return sb.toString();
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
        return new TypeVisitorImpl<U>(context.createContext(context.getLocation().addTargetType(type)), registry, type, save);
    }

    @Override
    public TypeVisitor createBooleanVisitor(BooleanConsumer save) {
        return new BooleanVisitorImpl(context.createContext(context.getLocation().addTargetType(Boolean.TYPE)), registry, save);
    }

    @Override
    public TypeVisitor createLongVisitor(LongConsumer save) {
        return new LongVisitorImpl(context.createContext(context.getLocation().addTargetType(Long.TYPE)), registry, save);
    }

    @Override
    public TypeVisitor createIntVisitor(IntConsumer save) {
        return new IntVisitorImpl(context.createContext(context.getLocation().addTargetType(Integer.TYPE)), registry, save);
    }

    @Override
    public TypeVisitor createShortVisitor(ShortConsumer save) {
        return new ShortVisitorImpl(context.createContext(context.getLocation().addTargetType(Short.TYPE)), registry, save);
    }

    @Override
    public TypeVisitor createByteVisitor(ByteConsumer save) {
        return new ByteVisitorImpl(context.createContext(context.getLocation().addTargetType(Byte.TYPE)), registry, save);
    }

    @Override
    public TypeVisitor createCharVisitor(CharConsumer save) {
        return new CharVisitorImpl(context.createContext(context.getLocation().addTargetType(Character.TYPE)), registry, save);
    }

    @Override
    public TypeVisitor createDoubleVisitor(DoubleConsumer save) {
        return new DoubleVisitorImpl(context.createContext(context.getLocation().addTargetType(Double.TYPE)), registry, save);
    }

    @Override
    public TypeVisitor createFloatVisitor(FloatConsumer save) {
        return new FloatVisitorImpl(context.createContext(context.getLocation().addTargetType(Float.TYPE)), registry, save);
    }

}