package com.brimworks.databind;

public interface ArrayVisitorBuilder<T> extends ArrayVisitor {
    T build();
}