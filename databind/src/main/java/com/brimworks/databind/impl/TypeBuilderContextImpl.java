package com.brimworks.databind.impl;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import com.brimworks.databind.BooleanConsumer;
import com.brimworks.databind.ByteConsumer;
import com.brimworks.databind.CharConsumer;
import com.brimworks.databind.FloatConsumer;
import com.brimworks.databind.Location;
import com.brimworks.databind.ShortConsumer;
import com.brimworks.databind.TypeBuilderContext;
import com.brimworks.databind.TypeVisitor;
import com.brimworks.databind.TypeVisitorFactory;
import com.brimworks.databind.UnknownKeyError;
import com.brimworks.databind.UnsupportedTypeError;

public class TypeBuilderContextImpl implements TypeBuilderContext {
    private TypeVisitorFactory visitorFactory;
    private Location location;

    public TypeBuilderContextImpl(TypeVisitorFactory visitorFactory, Location location) {
        Objects.requireNonNull(visitorFactory);
        Objects.requireNonNull(location);
        this.location = location;
        this.visitorFactory = visitorFactory.addContext(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TypeBuilderContext{");
        sb.append(location.toString());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public TypeBuilderContext createContext(Location newLocation) {
        return new TypeBuilderContextImpl(visitorFactory, newLocation);
    }

    @Override
    public UnsupportedTypeError unsupportedType(String msg) {
        StringBuilder sb = new StringBuilder("Attempt to convert ");
        sb.append(location.toString());
        sb.append(" is not supported: ");
        sb.append(msg);
        return new UnsupportedTypeError(sb.toString());
    }

    @Override
    public UnsupportedTypeError unsupportedType(Throwable ex) {
        StringBuilder sb = new StringBuilder("Attempt to convert ");
        sb.append(location.toString());
        sb.append(" is not supported");
        if (null != ex.getMessage()) {
            sb.append(": ");
            sb.append(ex.getMessage());
        }
        return new UnsupportedTypeError(sb.toString(), ex);
    }

    @Override
    public UnknownKeyError unknownKey() {
        Location locationWithKey = location.findElementOf(Location.KeyElement.class);
        StringBuilder sb = new StringBuilder("Unknown key '");
        sb.append(locationWithKey.getElement());
        sb.append("' was found at location ");
        sb.append(locationWithKey.getParent().toString());
        sb.append(" when trying to convert to type ");
        Location locationWithType = location.findElementOf(Location.TargetTypeElement.class);
        sb.append(locationWithType.getElement().toString());
        return new UnknownKeyError(sb.toString());
    }

    @Override
    public TypeVisitorFactory addContext(TypeBuilderContext ctx) {
        return visitorFactory.addContext(ctx);
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
    public TypeVisitor createBooleanVisitor(BooleanConsumer save) {
        TypeVisitor result = visitorFactory.createBooleanVisitor(save);
        if (null == result) {
            throw unsupportedType("No factory for boolean");
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

    @Override
    public TypeVisitor createIntVisitor(IntConsumer save) {
        TypeVisitor result = visitorFactory.createIntVisitor(save);
        if (null == result) {
            throw unsupportedType("No factory for int");
        }
        return result;
    }

    @Override
    public TypeVisitor createShortVisitor(ShortConsumer save) {
        TypeVisitor result = visitorFactory.createShortVisitor(save);
        if (null == result) {
            throw unsupportedType("No factory for short");
        }
        return result;
    }

    @Override
    public TypeVisitor createByteVisitor(ByteConsumer save) {
        TypeVisitor result = visitorFactory.createByteVisitor(save);
        if (null == result) {
            throw unsupportedType("No factory for byte");
        }
        return result;
    }

    @Override
    public TypeVisitor createCharVisitor(CharConsumer save) {
        TypeVisitor result = visitorFactory.createCharVisitor(save);
        if (null == result) {
            throw unsupportedType("No factory for char");
        }
        return result;
    }

    @Override
    public TypeVisitor createDoubleVisitor(DoubleConsumer save) {
        TypeVisitor result = visitorFactory.createDoubleVisitor(save);
        if (null == result) {
            throw unsupportedType("No factory for double");
        }
        return result;
    }

    @Override
    public TypeVisitor createFloatVisitor(FloatConsumer save) {
        TypeVisitor result = visitorFactory.createFloatVisitor(save);
        if (null == result) {
            throw unsupportedType("No factory for float");
        }
        return result;
    }
}