package com.brimworks.databind;

import java.util.function.Consumer;

public interface ObjectVisitor {
    ObjectVisitor put(String key, Consumer<TypeVisitor> consumer);
}