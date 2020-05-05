package com.brimworks.databind;

@FunctionalInterface
public interface VisitType<T> {
    void visit(T obj, TypeVisitor visitor);
}