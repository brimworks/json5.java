package com.brimworks.databind.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.brimworks.databind.ArrayVisitor;
import com.brimworks.databind.ArrayVisitorBuilder;
import com.brimworks.databind.TypeAdapterRegistry;
import com.brimworks.databind.TypeBuilderContext;
import com.brimworks.databind.TypeFactory;
import com.brimworks.databind.TypeVisitor;
import com.brimworks.databind.VisitType;

public class ArrayTypeFactoryRegistry implements TypeAdapterRegistry {
    private static final int DEFAULT_ALLOC = 100;

    private static abstract class ArrayVisitorBuilderImpl implements ArrayVisitorBuilder<Object> {
        private int alloc;
        private Class<?> elmType;
        private int size;
        protected Object array;

        ArrayVisitorBuilderImpl(Class<?> elmType, int alloc) {
            this.elmType = elmType;
            this.alloc = alloc;
            array = Array.newInstance(elmType, alloc);
        }

        @Override
        public Object build() {
            if (size != alloc) {
                Object result = Array.newInstance(elmType, size);
                System.arraycopy(array, 0, result, 0, size);
                return result;
            }
            return array;
        }

        protected int alloc() {
            if (++size <= alloc)
                return size - 1;
            alloc *= 2;
            Object copy = Array.newInstance(elmType, alloc);
            System.arraycopy(array, 0, copy, 0, size - 1);
            array = copy;
            return size - 1;
        }

        @Override
        abstract public TypeVisitor add();
    }

    private Map<Class<?>, TypeFactory<?>> PRIMATIVE_TYPE_FACTORY = new HashMap<>();
    {
        {
            PRIMATIVE_TYPE_FACTORY.put(Integer.TYPE, new TypeFactory<Object>() {
                @Override
                public Type getRawType() {
                    return int[].class;
                }

                @Override
                public ArrayVisitorBuilder<Object> createArray(TypeBuilderContext ctx) {
                    return new ArrayVisitorBuilderImpl(Integer.TYPE, DEFAULT_ALLOC) {
                        @Override
                        public TypeVisitor add() {
                            return ctx.createIntVisitor(elm -> Array.setInt(array, alloc(), elm));
                        }
                    };
                }
            });
            PRIMATIVE_TYPE_FACTORY.put(Long.TYPE, new TypeFactory<Object>() {
                @Override
                public Type getRawType() {
                    return long[].class;
                }

                @Override
                public ArrayVisitorBuilder<Object> createArray(TypeBuilderContext ctx) {
                    return new ArrayVisitorBuilderImpl(Long.TYPE, DEFAULT_ALLOC) {
                        @Override
                        public TypeVisitor add() {
                            return ctx.createLongVisitor(elm -> Array.setLong(array, alloc(), elm));
                        }
                    };
                }
            });
        }
    }
    private Map<Class<?>, VisitType<?>> PRIMATIVE_VISIT_TYPE = new HashMap<>();
    {
        {
            PRIMATIVE_VISIT_TYPE.put(Integer.TYPE, new VisitType<Object>() {
                @Override
                public void visit(Object array, TypeVisitor visitor) {
                    ArrayVisitor arrayVisitor = visitor.visitArray();
                    int len = Array.getLength(array);
                    for (int i = 0; i < len; i++) {
                        arrayVisitor.add().visit(Array.getInt(array, i));
                    }
                }
            });
        }
    }

    @Override
    public TypeFactory<?> getTypeFactory(Type type) {
        Class<?> elmType = getComponentType(type);
        if (null == elmType) {
            return null;
        }
        TypeFactory<?> result = PRIMATIVE_TYPE_FACTORY.get(elmType);
        if (null != result)
            return result;

        return new TypeFactory<Object>() {
            @Override
            public Type getRawType() {
                return type;
            }

            @Override
            public ArrayVisitorBuilder<Object> createArray(TypeBuilderContext ctx) {
                return new ArrayVisitorBuilderImpl(elmType, DEFAULT_ALLOC) {
                    @Override
                    public TypeVisitor add() {
                        return ctx.createVisitor(elmType, elm -> Array.set(array, alloc(), elm));
                    }
                };
            }
        };
    }

    @Override
    public VisitType<?> getVisitType(Type type) {
        Class<?> elmType = getComponentType(type);
        if (null == elmType) {
            return null;
        }
        VisitType<?> result = PRIMATIVE_VISIT_TYPE.get(elmType);
        if (null != result)
            return result;

        return new VisitType<Object>() {
            @Override
            public void visit(Object array, TypeVisitor visitor) {
                ArrayVisitor arrayVisitor = visitor.visitArray();
                int len = Array.getLength(array);
                for (int i = 0; i < len; i++) {
                    arrayVisitor.add().visit(Array.get(array, i));
                }
            }
        };
    }

    private static Class<?> getComponentType(Type type) {
        if (!(type instanceof Class)) {
            return null;
        }
        return ((Class<?>) type).getComponentType();
    }
}