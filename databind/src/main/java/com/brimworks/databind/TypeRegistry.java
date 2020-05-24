package com.brimworks.databind;

public interface TypeRegistry extends TypeFactoryRegistry, VisitTypeRegistry {
    public interface Builder {
        Builder put(BooleanFactory adapter);
        Builder put(LongFactory adapter);
        Builder put(IntFactory adapter);
        Builder put(ShortFactory adapter);
        Builder put(ByteFactory adapter);
        Builder put(CharFactory adapter);
        Builder put(DoubleFactory adapter);
        Builder put(FloatFactory adapter);
        <T> Builder put(TypeAdapter<T> adapter);
        Builder add(TypeAdapterRegistry registry);
    }
}