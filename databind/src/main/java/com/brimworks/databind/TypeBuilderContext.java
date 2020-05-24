package com.brimworks.databind;

public interface TypeBuilderContext extends TypeVisitorFactory {
    UnsupportedTypeError unsupportedType(Throwable ex);
    UnsupportedTypeError unsupportedType(String msg);
    UnknownKeyError unknownKey();
    Location getLocation();
    TypeBuilderContext createContext(Location newLocation);
}