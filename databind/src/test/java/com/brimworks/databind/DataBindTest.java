package com.brimworks.databind;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.Date;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.Arrays;

public class DataBindTest {
    public static class Person {
        public String fname;
        public String lname;
        public Date birthday;
    }

    @Tag("unit")
    @Test
    public void testBasic() throws IOException {
        DataBind binder = new DataBind.Builder().put(new TypeAdapter<Person>() {
            @Override
            public Type getRawType() {
                return Person.class;
            }

            @Override
            public ObjectVisitorBuilder<Person> createObject(TypeBuilderContext ctx) {
                return new ObjectVisitorBuilder<Person>() {
                    Person person = new Person();

                    @Override
                    public Person build() {
                        return person;
                    }

                    @Override
                    public ObjectVisitor put(String key, Consumer<TypeVisitor> consumer) {
                        switch (key) {
                            case "firstName":
                                consumer.accept(ctx.createVisitor(String.class, str -> person.fname = str));
                                break;
                            case "lastName":
                                consumer.accept(ctx.createVisitor(String.class, str -> person.lname = str));
                                break;
                            case "birthday":
                                consumer.accept(ctx.createVisitor(Date.class, date -> person.birthday = date));
                                break;
                            default:
                                throw ctx.unexpectedKey(key);
                        }
                        return this;
                    }
                };
            }

            @Override
            public void visit(Person person, TypeVisitor visitor) {
                visitor.visitObject().put("firstName", v -> v.visit(person.fname))
                        .put("lastName", v -> v.visit(person.lname)).put("birthday", v -> v.visit(person.birthday));

            }
        }).put(new TypeAdapter<Date>() {
            @Override
            public Type getRawType() {
                return Date.class;
            }

            @Override
            public Date create(String value, TypeBuilderContext ctx) {
                return Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(value)));
            }

            @Override
            public void visit(Date date, TypeVisitor visitor) {
                visitor.visit(DateTimeFormatter.ISO_INSTANT.format(date.toInstant()));
            }
        }).build();
        Person input = new Person();
        input.fname = "George";
        input.lname = "Smith";
        input.birthday = new Date(1588547922000L);
        Person output = binder.transform(input, Person.class);
        assertTrue(output != input, "Transform should create a new Person");
        assertEquals(input.fname, output.fname);
        assertEquals(input.lname, output.lname);
        assertEquals(input.birthday, output.birthday);
    }

    @Tag("unit")
    @Test
    public void testPrimitives() throws IOException {
        DataBind binder = new DataBind.Builder().put(new TypeAdapter<int[]>() {
            @Override
            public Type getRawType() {
                return int[].class;
            }

            @Override
            public ArrayVisitorBuilder<int[]> createArray(TypeBuilderContext ctx) {
                return new ArrayVisitorBuilder<int[]>() {
                    int[] output = new int[256];
                    int length = 0;

                    @Override
                    public int[] build() {
                        return Arrays.copyOf(output, length);
                    }

                    @Override
                    public ArrayVisitor add(Consumer<TypeVisitor> consumer) {
                        // FIXME: Ensure capacity...
                        consumer.accept(ctx.createIntVisitor(num -> output[length++] = num));
                        return this;
                    }
                };
            }

            @Override
            public void visit(int[] value, TypeVisitor visitor) {
                ArrayVisitor arrayVisitor = visitor.visitArray();
                for ( int num : value) {
                    arrayVisitor.add(v -> v.visit(num));
                }
            }
        }).put(new TypeAdapter<long[]>() {
            @Override
            public Type getRawType() {
                return long[].class;
            }

            @Override
            public ArrayVisitorBuilder<long[]> createArray(TypeBuilderContext ctx) {
                return new ArrayVisitorBuilder<long[]>() {
                    long[] output = new long[256];
                    int length = 0;

                    @Override
                    public long[] build() {
                        return Arrays.copyOf(output, length);
                    }

                    @Override
                    public ArrayVisitor add(Consumer<TypeVisitor> consumer) {
                        // FIXME: Ensure capacity...
                        consumer.accept(ctx.createLongVisitor(num -> output[length++] = num));
                        return this;
                    }
                };
            }

            @Override
            public void visit(long[] value, TypeVisitor visitor) {
                ArrayVisitor arrayVisitor = visitor.visitArray();
                for ( long num : value) {
                    arrayVisitor.add(v -> v.visit(num));
                }
            }
        })
        .build();
        int[] input = new int[]{0, 1, 2, 3, 4, 5, 6};
        long[] output = binder.transform(input, long[].class);
        assertEquals(input.length, output.length);
        for ( int i=0; i < input.length; i++ ) {
            assertEquals((long)input[i], output[i], "index="+i);
        }
    }
}