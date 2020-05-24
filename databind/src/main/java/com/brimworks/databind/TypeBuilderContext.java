package com.brimworks.databind;

public interface TypeBuilderContext extends TypeVisitorFactory {
    UnsupportedTypeError unsupportedType(Throwable ex);
    UnsupportedTypeError unsupportedType(String msg);
    UnsupportedTypeError unsupportedType();
    UnknownKeyError unknownKey();


    TypeBuilderContext child(String key);
    TypeBuilderContext child(int index);
}