package com.brimworks.databind;

public interface ObjectVisitorBuilder<T> extends ObjectVisitor {
    public T build();
}