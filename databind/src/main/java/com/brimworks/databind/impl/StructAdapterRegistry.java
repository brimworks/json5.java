package com.brimworks.databind.impl;

import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.Map;
import java.util.HashMap;
import com.brimworks.databind.*;

/**
 * Can serialize/deserialize a java Object based on the public field names of the object. There must also be a default constructor for
 * the factory code to work.
 */
public class StructAdapterRegistry implements TypeAdapterRegistry {
    interface FieldConsumer {
        void accept(TypeVisitor visitor, Field field, Object inst) throws IllegalAccessException;
    }
    interface FieldFactory {
        TypeVisitor put(Object inst, TypeBuilderContext ctx) throws IllegalAccessException;
    }
    private static Map<Class<?>, FieldConsumer> FIELD_CONSUMERS = new HashMap<>();
    {{
        FIELD_CONSUMERS.put(Object.class, (v, f, o) -> v.visit(f.get(o)));
        FIELD_CONSUMERS.put(Boolean.TYPE, (v, f, o) -> v.visit(f.getBoolean(o)));
        FIELD_CONSUMERS.put(Long.TYPE, (v, f, o) -> v.visit(f.getLong(o)));
        FIELD_CONSUMERS.put(Integer.TYPE, (v, f, o) -> v.visit(f.getInt(o)));
        FIELD_CONSUMERS.put(Short.TYPE, (v, f, o) -> v.visit(f.getShort(o)));
        FIELD_CONSUMERS.put(Byte.TYPE, (v, f, o) -> v.visit(f.getByte(o)));
        FIELD_CONSUMERS.put(Character.TYPE, (v, f, o) -> v.visit(f.getChar(o)));
        FIELD_CONSUMERS.put(Double.TYPE, (v, f, o) -> v.visit(f.getDouble(o)));
        FIELD_CONSUMERS.put(Float.TYPE, (v, f, o) -> v.visit(f.getFloat(o)));
    }}
    private static Map<Class<?>, Function<Field, FieldFactory>> FIELD_FACTORIES = new HashMap<>();
    {{
        FIELD_FACTORIES.put(Object.class, field -> (obj, ctx) -> {
            return ctx.createVisitor(field.getGenericType(), val -> {
                try {
                    field.set(obj, val);
                } catch ( IllegalAccessException ex ) {
                    ctx.unsupportedType(ex);
                }
            });
        });
        FIELD_FACTORIES.put(Boolean.TYPE, field -> (obj, ctx) -> {
            return ctx.createBooleanVisitor(val -> {
                try {
                    field.setBoolean(obj, val);
                } catch ( IllegalAccessException ex ) {
                    ctx.unsupportedType(ex);
                }
            });
        });
        FIELD_FACTORIES.put(Long.TYPE, field -> (obj, ctx) -> {
            return ctx.createLongVisitor(val -> {
                try {
                    field.setLong(obj, val);
                } catch ( IllegalAccessException ex ) {
                    ctx.unsupportedType(ex);
                }
            });
        });
        FIELD_FACTORIES.put(Integer.TYPE, field -> (obj, ctx) -> {
            return ctx.createIntVisitor(val -> {
                try {
                    field.setInt(obj, val);
                } catch ( IllegalAccessException ex ) {
                    ctx.unsupportedType(ex);
                }
            });
        });
        FIELD_FACTORIES.put(Short.TYPE, field -> (obj, ctx) -> {
            return ctx.createShortVisitor(val -> {
                try {
                    field.setShort(obj, val);
                } catch ( IllegalAccessException ex ) {
                    ctx.unsupportedType(ex);
                }
            });
        });
        FIELD_FACTORIES.put(Byte.TYPE, field -> (obj, ctx) -> {
            return ctx.createByteVisitor(val -> {
                try {
                    field.setByte(obj, val);
                } catch ( IllegalAccessException ex ) {
                    ctx.unsupportedType(ex);
                }
            });
        });
        FIELD_FACTORIES.put(Character.TYPE, field -> (obj, ctx) -> {
            return ctx.createCharVisitor(val -> {
                try {
                    field.setChar(obj, val);
                } catch ( IllegalAccessException ex ) {
                    ctx.unsupportedType(ex);
                }
            });
        });
        FIELD_FACTORIES.put(Double.TYPE, field -> (obj, ctx) -> {
            return ctx.createDoubleVisitor(val -> {
                try {
                    field.setDouble(obj, val);
                } catch ( IllegalAccessException ex ) {
                    ctx.unsupportedType(ex);
                }
            });
        });
        FIELD_FACTORIES.put(Float.TYPE, field -> (obj, ctx) -> {
            return ctx.createFloatVisitor(val -> {
                try {
                    field.setFloat(obj, val);
                } catch ( IllegalAccessException ex ) {
                    ctx.unsupportedType(ex);
                }
            });
        });
    }}
    @Override
    public TypeFactory<?> getTypeFactory(Type type) {
        if ( !(type instanceof Class) ) {
            return null;
        }
        Class<?> ctype = (Class<?>)type;
        try {
            ctype.newInstance();
        } catch ( Exception ex ) {
            return null;
        }
        Map<String, FieldFactory> fieldFactories = new HashMap<>();
        for ( Field field : ctype.getFields() ) {
            try {
                field.setAccessible(true);
            } catch ( Exception ex ) {
                continue;
            }
            Function<Field, FieldFactory> factory = FIELD_FACTORIES.get(field.getType());
            if ( null == factory ) {
                factory = FIELD_FACTORIES.get(Object.class);
            }
            fieldFactories.put(field.getName(), factory.apply(field));
        }
        return new TypeFactory<Object>() {
            @Override
            public ObjectBuilder<Object> createObject(int size, TypeBuilderContext ctx1) {
                Object obj;
                try {
                    obj = ctype.newInstance();
                } catch ( Exception ex ) {
                    throw ctx1.unsupportedType(ex);
                }
                return new ObjectBuilder<Object>() {
                    @Override
                    public TypeVisitor put(String key, TypeBuilderContext ctx) {
                        FieldFactory factory = fieldFactories.get(key);
                        if ( null == factory ) {
                            throw ctx.unknownKey();
                        }
                        try {
                            return factory.put(obj, ctx);
                        } catch ( Exception ex ) {
                            throw ctx.unsupportedType(ex);
                        }
                    }
                    @Override
                    public Object build() {
                        return obj;
                    }
                };
            }
        
        };
    }
    @Override
    public VisitType<?> getVisitType(Type type) {
        if ( !(type instanceof Class) ) {
            return null;
        }
        Class<?> ctype = (Class<?>)type;
        return new VisitType<Object>() {
            @Override
            public void visit(Object obj, TypeVisitor visitor) {
                Field[] fields = ctype.getFields();
                ObjectVisitor objectVisitor = visitor.visitObject(fields.length);
                for ( Field field : fields ) {
                    if ( !field.isAccessible() ) continue;
                    TypeVisitor fieldVisitor = objectVisitor.put(field.getName());
                    FieldConsumer consumer = FIELD_CONSUMERS.get(field.getType());
                    if ( null == consumer ) {
                        consumer = FIELD_CONSUMERS.get(Object.class);
                    }
                    try {
                        consumer.accept(fieldVisitor, field, obj);
                    } catch ( IllegalAccessException ex ) {
                        continue;
                    }
                }
                objectVisitor.done();
            }
        };
    }
}