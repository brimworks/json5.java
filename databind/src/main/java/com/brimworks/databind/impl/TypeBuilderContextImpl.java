package com.brimworks.databind.impl;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import com.brimworks.databind.TypeBuilderContext;
import com.brimworks.databind.TypeVisitor;
import com.brimworks.databind.TypeVisitorFactory;
import com.brimworks.databind.UnknownKeyError;
import com.brimworks.databind.UnsupportedTypeError;

public class TypeBuilderContextImpl implements TypeBuilderContext {
    private TypeVisitorFactory visitorFactory;
    private TypeBuilderContextImpl parent;
    private Type buildType;
    private String key;
    private int index;

    public TypeBuilderContextImpl(TypeVisitorFactory visitorFactory, Type buildType) {
        this(visitorFactory, buildType, null, null);
    }

    private TypeBuilderContextImpl(TypeVisitorFactory visitorFactory, Type buildType, String key,
            TypeBuilderContextImpl parent) {
        this.visitorFactory = visitorFactory;
        this.buildType = buildType;
        this.key = key;
        this.parent = parent;
    }

    private TypeBuilderContextImpl(TypeVisitorFactory visitorFactory, Type buildType, int index,
            TypeBuilderContextImpl parent) {
        this.visitorFactory = visitorFactory;
        this.buildType = buildType;
        this.index = index;
        this.parent = parent;
    }

    @Override
    public TypeBuilderContext child(String key) {
        return new TypeBuilderContextImpl(visitorFactory, buildType, key, this);
    }

    @Override
    public TypeBuilderContext child(int index) {
        return new TypeBuilderContextImpl(visitorFactory, buildType, index, this);
    }

    private void appendLocation(StringBuilder sb) {
        if (null != parent) {
            parent.appendLocation(sb);
            sb.append("/");
        } else {
            sb.append("/");
        }
        if (null != key) {
            sb.append(key.replaceAll("~", "~0").replaceAll("/", "~1"));
        } else {
            sb.append("" + index);
        }
    }

    @Override
    public UnsupportedTypeError unsupportedType() {
        StringBuilder sb = new StringBuilder("Attempt to convert ");
        appendLocation(sb);
        sb.append(" to type ");
        sb.append(buildType.toString());
        sb.append(" is not supported");
        return new UnsupportedTypeError(sb.toString());
    }

    @Override
    public UnsupportedTypeError unsupportedType(String msg) {
        StringBuilder sb = new StringBuilder("Attempt to convert ");
        appendLocation(sb);
        sb.append(" to type ");
        sb.append(buildType.toString());
        sb.append(" is not supported: ");
        sb.append(msg);
        return new UnsupportedTypeError(sb.toString());
    }

    @Override
    public UnsupportedTypeError unsupportedType(Throwable ex) {
        StringBuilder sb = new StringBuilder("Attempt to convert ");
        appendLocation(sb);
        sb.append(" to type ");
        sb.append(buildType.toString());
        sb.append(" is not supported");
        if (null != ex.getMessage()) {
            sb.append(": ");
            sb.append(ex.getMessage());
        }
        return new UnsupportedTypeError(sb.toString(), ex);
    }

    @Override
    public UnknownKeyError unexpectedKey(String key) {
        StringBuilder sb = new StringBuilder("Unknown key '");
        sb.append(key);
        sb.append("' was found at location ");
        appendLocation(sb);
        sb.append(" when tryint to convert to type ");
        sb.append(buildType.toString());
        return new UnknownKeyError(sb.toString());
    }

    @Override
    public <U> TypeVisitor createVisitor(Type type, Consumer<U> save) {
        TypeVisitor result = visitorFactory.createVisitor(type, save);
        if (null == result) {
            throw unsupportedType("No factory for " + type);
        }
        return result;
    }

    @Override
    public TypeVisitor createIntVisitor(IntConsumer save) {
        TypeVisitor result = visitorFactory.createIntVisitor(save);
        if (null == result) {
            throw unsupportedType("No factory for int");
        }
        return result;
    }

    @Override
    public TypeVisitor createLongVisitor(LongConsumer save) {
        TypeVisitor result = visitorFactory.createLongVisitor(save);
        if (null == result) {
            throw unsupportedType("No factory for long");
        }
        return result;
    }
}