package com.brimworks.databind;

public interface TypeVisitor {
    void visit(Object val);
    ObjectVisitor visitObject(int size);
    ArrayVisitor visitArray(int size);

    // PRIMITIVES:
    default void visit(boolean val) {
        visit(Boolean.valueOf(val));
    }
    default void visit(long val) {
        visit(Long.valueOf(val));
    }
    default void visit(int val) {
        visit(Integer.valueOf(val));
    }
    default void visit(short val) {
        visit(Short.valueOf(val));
    }
    default void visit(char val) {
        visit(Character.valueOf(val));
    }
    default void visit(byte val) {
        visit(Byte.valueOf(val));
    }
    default void visit(double val) {
        visit(Double.valueOf(val));
    }
    default void visit(float val) {
        visit(Float.valueOf(val));
    }
}