package com.brimworks.databind;

public interface ObjectVisitor {
    TypeVisitor put(String key);
}