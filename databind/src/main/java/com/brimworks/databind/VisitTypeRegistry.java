package com.brimworks.databind;

import java.lang.reflect.Type;

public interface VisitTypeRegistry {
    @SuppressWarnings("unchecked")
    default <T> VisitType<T> getVisitType(Class<T> type) {
        return (VisitType<T>)getVisitType((Type)type);
    }
    VisitType<?> getVisitType(Type type);
}