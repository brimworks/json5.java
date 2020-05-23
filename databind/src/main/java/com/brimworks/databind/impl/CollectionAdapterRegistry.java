package com.brimworks.databind.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

import com.brimworks.databind.ArrayBuilder;
import com.brimworks.databind.ArrayVisitor;
import com.brimworks.databind.TypeAdapterRegistry;
import com.brimworks.databind.TypeBuilderContext;
import com.brimworks.databind.TypeFactory;
import com.brimworks.databind.TypeVisitor;
import com.brimworks.databind.VisitType;

public class CollectionAdapterRegistry implements TypeAdapterRegistry {
    private Map<Class<?>, Supplier<? extends Collection<Object>>> COLLECTION_FACTORY = new HashMap<>();
    {{
        COLLECTION_FACTORY.put(List.class, ArrayList::new);
        COLLECTION_FACTORY.put(Set.class, HashSet::new);
    }}
    @Override
    public TypeFactory<?> getTypeFactory(Type type) {
        Class<?> collectionType = getCollectionType(type);
        if ( null == collectionType ) {
            return null;
        }
        Supplier<? extends Collection<Object>> newInstance = COLLECTION_FACTORY.get(collectionType);
        if ( null == newInstance ) {
            return null;
        }
        Type elmType = getComponentType(type);
        if (null == elmType) {
            return null;
        }
        return new TypeFactory<Collection<Object>>() {
            @Override
            public ArrayBuilder<Collection<Object>> createArray(int size, TypeBuilderContext ctx1) {
                return new ArrayBuilder<Collection<Object>>() {
                    Collection<Object> result = newInstance.get();
                    @Override
                    public Collection<Object> build() {
                        return result;
                    }
            
                    @Override
                    public TypeVisitor add(TypeBuilderContext ctx) {
                        return ctx.createVisitor(elmType, elm -> result.add(elm));
                    }
                };
            }
        };
    }

    @Override
    public VisitType<?> getVisitType(Type type) {
        Type elmType = getComponentType(type);
        if (null == elmType) {
            return null;
        }
        return new VisitType<Collection<Object>>() {
            @Override
            public void visit(Collection<Object> collection, TypeVisitor visitor) {
                ArrayVisitor arrayVisitor = visitor.visitArray(collection.size());
                Iterator<Object> it = collection.iterator();
                while(it.hasNext()) {
                    arrayVisitor.add().visit(it.next());
                }
                arrayVisitor.done();
            }
        };
    }

    private static Class<?> getCollectionType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType ptype = (ParameterizedType)type;
        if ( !(ptype.getRawType() instanceof Class) ) {
            return null;
        }
        Class<?> ctype = (Class<?>)ptype.getRawType();
        if ( ctype.isAssignableFrom(Collection.class) ) {
            return null;
        }
        return ctype;
    }

    private static Type getComponentType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType ptype = (ParameterizedType)type;
        if ( !(ptype.getRawType() instanceof Class) ) {
            return null;
        }
        Class<?> ctype = (Class<?>)ptype.getRawType();
        if ( ctype.isAssignableFrom(Collection.class) ) {
            return null;
        }
        Type[] args = ptype.getActualTypeArguments();
        if ( args.length != 1 ) {
            return null;
        }
        return args[0];
    }
}