package com.brimworks.databind.impl;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import com.brimworks.databind.BooleanFactory;
import com.brimworks.databind.ByteFactory;
import com.brimworks.databind.CharFactory;
import com.brimworks.databind.DoubleFactory;
import com.brimworks.databind.FloatFactory;
import com.brimworks.databind.IntFactory;
import com.brimworks.databind.LongFactory;
import com.brimworks.databind.ShortFactory;
import com.brimworks.databind.TypeAdapter;
import com.brimworks.databind.TypeBuilderContext;
import com.brimworks.databind.TypeRegistry;
import com.brimworks.databind.TypeVisitor;
import java.math.BigInteger;
import java.math.BigDecimal;

public enum PrimitiveAdapter {
    NULL(builder -> builder.put(new TypeAdapter<Object>() {
        @Override
        public Type getRawType() {
            return null;
        }

        @Override
        public void visit(Object val, TypeVisitor visitor) {
            visitor.visit(val);
        }
    })), STRING(builder -> builder.put(new TypeAdapter<String>() {
        @Override
        public Type getRawType() {
            return String.class;
        }

        @Override
        public String create(String string, TypeBuilderContext ctx) {
            return string;
        }

        @Override
        public String create(Number value, TypeBuilderContext ctx) {
            return value.toString();
        }

        @Override
        public String create(boolean value, TypeBuilderContext ctx) {
            return Boolean.toString(value);
        }

        @Override
        public void visit(String val, TypeVisitor visitor) {
            visitor.visit(val);
        }
    })), BOOLEAN(builder -> builder.put(new BooleanFactory() {
        @Override
        public boolean create(String string, TypeBuilderContext ctx) {
            return Boolean.parseBoolean(string);
        }

        @Override
        public boolean create(Number value, TypeBuilderContext ctx) {
            return value.intValue() != 0;
        }

        @Override
        public boolean create(long value, TypeBuilderContext ctx) {
            return value != 0;
        }

        @Override
        public boolean create(int value, TypeBuilderContext ctx) {
            return value != 0;
        }

        @Override
        public boolean create(short value, TypeBuilderContext ctx) {
            return value != 0;
        }

        @Override
        public boolean create(char value, TypeBuilderContext ctx) {
            return value != 0;
        }

        @Override
        public boolean create(byte value, TypeBuilderContext ctx) {
            return value != 0;
        }

        @Override
        public boolean create(double value, TypeBuilderContext ctx) {
            return value != 0.0;
        }

        @Override
        public boolean create(float value, TypeBuilderContext ctx) {
            return value != 0.0f;
        }

        @Override
        public boolean create(boolean value, TypeBuilderContext ctx) {
            return value;
        }
    })), LONG(builder -> builder.put(new LongFactory() {
        @Override
        public long create(String string, TypeBuilderContext ctx) {
            return Long.parseLong(string);
        }

        @Override
        public long create(Number value, TypeBuilderContext ctx) {
            try {
                if ( value instanceof BigInteger ) {
                    return ((BigInteger)value).longValueExact();
                } else if ( value instanceof BigDecimal ) {
                    return ((BigDecimal)value).longValueExact();
                } else {
                    return value.longValue();
                }
            } catch ( ArithmeticException ex ) {
                throw ctx.unsupportedType("number="+value+" out of range for long");
            }
        }

        @Override
        public long create(long value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public long create(int value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public long create(short value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public long create(char value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public long create(byte value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public long create(double value, TypeBuilderContext ctx) {
            return Math.round(value);
        }

        @Override
        public long create(float value, TypeBuilderContext ctx) {
            return Math.round(value);
        }

        @Override
        public long create(boolean value, TypeBuilderContext ctx) {
            return value ? 1 : 0;
        }
    })), INTEGER(builder -> builder.put(new IntFactory() {
        @Override
        public int create(String string, TypeBuilderContext ctx) {
            return Integer.parseInt(string);
        }

        @Override
        public int create(Number value, TypeBuilderContext ctx) {
            try {
                if ( value instanceof BigInteger ) {
                    return ((BigInteger)value).intValueExact();
                } else if ( value instanceof BigDecimal ) {
                    return ((BigDecimal)value).intValueExact();
                } else {
                    return create(value.longValue(), ctx);
                }
            } catch (ArithmeticException ex) {
                throw ctx.unsupportedType("number="+value+" out of range for int");
            }
        }

        @Override
        public int create(long value, TypeBuilderContext ctx) {
            if ( value < Integer.MIN_VALUE || value > Integer.MAX_VALUE ) {
                throw ctx.unsupportedType("number="+value+" out of range for int");
            }
            return (int)value;
        }

        @Override
        public int create(int value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public int create(short value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public int create(char value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public int create(byte value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public int create(double value, TypeBuilderContext ctx) {
            return create(Math.round(value), ctx);
        }

        @Override
        public int create(float value, TypeBuilderContext ctx) {
            return Math.round(value);
        }

        @Override
        public int create(boolean value, TypeBuilderContext ctx) {
            return value ? 1 : 0;
        }
    })), SHORT(builder -> builder.put(new ShortFactory() {
        @Override
        public short create(String string, TypeBuilderContext ctx) {
            return Short.parseShort(string);
        }

        @Override
        public short create(Number value, TypeBuilderContext ctx) {
            try {
                if ( value instanceof BigInteger ) {
                    return ((BigInteger)value).shortValueExact();
                } else if ( value instanceof BigDecimal ) {
                    return ((BigDecimal)value).shortValueExact();
                } else {
                    return create(value.longValue(), ctx);
                }
            } catch (ArithmeticException ex) {
                throw ctx.unsupportedType("number="+value+" out of range for short");
            }
        }

        @Override
        public short create(long value, TypeBuilderContext ctx) {
            if ( value < Short.MIN_VALUE || value > Short.MAX_VALUE ) {
                throw ctx.unsupportedType("number="+value+" out of range for short");
            }
            return (short)value;
        }

        @Override
        public short create(int value, TypeBuilderContext ctx) {
            return create((long)value, ctx);
        }

        @Override
        public short create(short value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public short create(char value, TypeBuilderContext ctx) {
            return create((int)value, ctx);
        }

        @Override
        public short create(byte value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public short create(double value, TypeBuilderContext ctx) {
            return create(Math.round(value), ctx);
        }

        @Override
        public short create(float value, TypeBuilderContext ctx) {
            return create(Math.round(value), ctx);
        }

        @Override
        public short create(boolean value, TypeBuilderContext ctx) {
            return (short)(value ? 1 : 0);
        }
    })), BYTE(builder -> builder.put(new ByteFactory() {
        @Override
        public byte create(String string, TypeBuilderContext ctx) {
            return Byte.parseByte(string);
        }

        @Override
        public byte create(Number value, TypeBuilderContext ctx) {
            try {
                if ( value instanceof BigInteger ) {
                    return ((BigInteger)value).byteValueExact();
                } else if ( value instanceof BigDecimal ) {
                    return ((BigDecimal)value).byteValueExact();
                } else {
                    return create(value.longValue(), ctx);
                }
            } catch (ArithmeticException ex) {
                throw ctx.unsupportedType("number="+value+" out of range for byte");
            }
        }

        @Override
        public byte create(long value, TypeBuilderContext ctx) {
            if ( value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                throw ctx.unsupportedType("number="+value+" out of range for byte");
            }
            return (byte)value;
        }

        @Override
        public byte create(int value, TypeBuilderContext ctx) {
            return create((long)value, ctx);
        }

        @Override
        public byte create(short value, TypeBuilderContext ctx) {
            return create((long)value, ctx);
        }

        @Override
        public byte create(char value, TypeBuilderContext ctx) {
            return create((long)value, ctx);
        }

        @Override
        public byte create(byte value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public byte create(double value, TypeBuilderContext ctx) {
            return create(Math.round(value), ctx);
        }

        @Override
        public byte create(float value, TypeBuilderContext ctx) {
            return create(Math.round(value), ctx);
        }

        @Override
        public byte create(boolean value, TypeBuilderContext ctx) {
            return (byte)(value ? 1 : 0);
        }
    })), CHAR(builder -> builder.put(new CharFactory() {
        @Override
        public char create(Number value, TypeBuilderContext ctx) {
            try {
                if ( value instanceof BigInteger ) {
                    return create(((BigInteger)value).intValueExact(), ctx);
                } else if ( value instanceof BigDecimal ) {
                    return create(((BigDecimal)value).intValueExact(), ctx);
                } else {
                    return create(value.intValue(), ctx);
                }
            } catch (ArithmeticException ex) {
                throw ctx.unsupportedType("number="+value+" out of range for char");
            }
        }

        @Override
        public char create(long value, TypeBuilderContext ctx) {
            if ( value < Character.MIN_VALUE || value > Character.MAX_VALUE ) {
                throw ctx.unsupportedType("number="+value+" out of range for char");
            }
            return (char)value;
        }

        @Override
        public char create(int value, TypeBuilderContext ctx) {
            return create((long)value, ctx);
        }

        @Override
        public char create(short value, TypeBuilderContext ctx) {
            return create((long)value, ctx);
        }

        @Override
        public char create(char value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public char create(byte value, TypeBuilderContext ctx) {
            return create((long)value, ctx);
        }

        @Override
        public char create(double value, TypeBuilderContext ctx) {
            return create(Math.round(value), ctx);
        }

        @Override
        public char create(float value, TypeBuilderContext ctx) {
            return create(Math.round(value), ctx);
        }

    })), DOUBLE(builder -> builder.put(new DoubleFactory() {
        @Override
        public double create(String string, TypeBuilderContext ctx) {
            return Double.parseDouble(string);
        }

        @Override
        public double create(Number value, TypeBuilderContext ctx) {
            return value.doubleValue();
        }

        @Override
        public double create(long value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public double create(int value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public double create(short value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public double create(char value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public double create(byte value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public double create(double value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public double create(float value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public double create(boolean value, TypeBuilderContext ctx) {
            return value ? 1.0 : 0.0;
        }
    })), FLOAT(builder -> builder.put(new FloatFactory() {
        @Override
        public float create(String string, TypeBuilderContext ctx) {
            return Float.parseFloat(string);
        }

        @Override
        public float create(Number value, TypeBuilderContext ctx) {
            return value.floatValue();
        }

        @Override
        public float create(long value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public float create(int value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public float create(short value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public float create(char value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public float create(byte value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public float create(double value, TypeBuilderContext ctx) {
            if ( value < Float.MIN_VALUE || value > Float.MAX_VALUE ) {
                throw ctx.unsupportedType("number="+value+" out of range for float");
            }
            return (float)value;
        }

        @Override
        public float create(float value, TypeBuilderContext ctx) {
            return value;
        }

        @Override
        public float create(boolean value, TypeBuilderContext ctx) {
            return value ? 1.0f : 0.0f;
        }
    })),

    // Lowest priority is first:
    STRUCT(builder -> builder.add(new StructAdapterRegistry())),
    ARRAY(builder -> builder.add(new ArrayAdapterRegistry())),
    COLLECTION(builder -> builder.add(new CollectionAdapterRegistry())),
    ;

    private PrimitiveAdapter(Consumer<TypeRegistry.Builder> consumer) {
        this.consumer = consumer;
    }

    private Consumer<TypeRegistry.Builder> consumer;

    public void apply(TypeRegistry.Builder builder) {
        consumer.accept(builder);
    }
}