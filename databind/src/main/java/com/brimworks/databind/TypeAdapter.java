package com.brimworks.databind;

import java.lang.reflect.Type;

public interface TypeAdapter<T> extends TypeFactory<T>, VisitType<T> {
    Type getRawType();
}