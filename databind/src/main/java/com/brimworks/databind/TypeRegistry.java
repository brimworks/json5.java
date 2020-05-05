package com.brimworks.databind;

public interface TypeRegistry extends TypeFactoryRegistry, VisitTypeRegistry {
    public interface Builder {
        Builder put(IntFactory adapter);
        Builder put(LongFactory adapter);
        <T> Builder put(TypeAdapter<T> adapter);
    }
}