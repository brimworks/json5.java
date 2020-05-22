package com.brimworks.json5.databind;

import com.brimworks.databind.TypeRegistry;
import com.brimworks.databind.VisitorBuilder;
import com.brimworks.databind.TypeFactory;
import com.brimworks.databind.TypeVisitor;
import com.brimworks.databind.UnsupportedTypeError;
import com.brimworks.json5.JSON5Visitor;
import com.brimworks.databind.ObjectVisitor;
import com.brimworks.databind.ArrayVisitor;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;


public class JSON5TypeVisitor implements JSON5Visitor {
    private static class Frame {
        Frame(TypeVisitor visitor) {
            this.visitor = visitor;
        }
        Frame(ObjectVisitor objectVisitor) {
            this.objectVisitor = objectVisitor;
        }
        Frame(ArrayVisitor arrayVisitor) {
            this.arrayVisitor = arrayVisitor;
        }
        TypeVisitor visitor;
        ObjectVisitor objectVisitor;
        ArrayVisitor arrayVisitor;
    }
    private List<Frame> stack = new ArrayList<>();
    private Object result;

    public JSON5TypeVisitor(TypeRegistry typeRegistry, Type type) throws UnsupportedTypeError {
        TypeFactory<?> factory = typeRegistry.getTypeFactory(type);
        if (null == factory) {
            throw new UnsupportedTypeError("No registered VisitType for " + type);
        }
        stack.add(new Frame(new VisitorBuilder(factory, obj -> result = obj, typeRegistry)));
    }

    private TypeVisitor get() {
        Frame frame = stack.get(stack.size()-1);
        if ( frame.arrayVisitor != null ) {
            return frame.arrayVisitor.add();
        }
        return frame.visitor;
    }

    @Override
    public void visitNull(int line, long offset) {
        get().visit(null);
    }

    @Override
    public void visit(boolean val, int line, long offset) {
        get().visit(val);
    }

    @Override
    public void visit(String val, int line, long offset) {
        get().visit(val);
    }

    @Override
    public void visit(Number val, int line, long offset) {
        get().visit(val);
    }

    @Override
    public void visitNumber(BigInteger val, int line, long offset) {
        get().visit(val);
    }

    @Override
    public void visitNumber(BigDecimal val, int line, long offset) {
        get().visit(val);
    }

    @Override
    public void visitNumber(long val, int line, long offset) {
        get().visit(val);
    }

    @Override
    public void visitNumber(double val, int line, long offset) {
        get().visit(val);
    }

    @Override
    public void startObject(int line, long offset) {
        stack.add(new Frame(get().visitObject()));
    }

    @Override
    public void visitKey(String key, int line, long offset) {
        Frame frame = stack.get(stack.size()-1);
        frame.visitor = frame.objectVisitor.put(key);
    }

    @Override
    public void endObject(int line, long offset) {
        stack.remove(stack.size()-1);
    }

    @Override
    public void startArray(int line, long offset) {
        stack.add(new Frame(get().visitArray()));
    }

    @Override
    public void endArray(int line, long offset) {
        stack.remove(stack.size()-1);
    }

    public Object pop() {
        if ( stack.size() != 1 ) {
            throw new IllegalStateException("Expected stack to have exactly 1 element, got "+stack.size());
        }
        Frame frame = stack.remove(0);
        VisitorBuilder visitorBuilder = (VisitorBuilder)frame.visitor;
        visitorBuilder.visitFinish();
        return result;
    }
}