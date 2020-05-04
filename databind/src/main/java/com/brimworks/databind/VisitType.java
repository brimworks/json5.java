package com.brimworks.databind;

@FunctionalInterface
public interface VisitType<T> {
    void visit(T obj, TypeVisitor visitor);
    default void visit(long num, TypeVisitor visitor) {
        visit(Long.valueOf(num), visitor);
    }
    default void visit(double num, TypeVisitor visitor) {
        visit(Double.valueOf(num), visitor);
    }
}