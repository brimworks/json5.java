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
        public int carCount;
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
                    public TypeVisitor put(String key) {
                        switch (key) {
                            case "firstName":
                                return ctx.createVisitor(String.class, str -> person.fname = str);
                            case "lastName":
                                return ctx.createVisitor(String.class, str -> person.lname = str);
                            case "birthday":
                                return ctx.createVisitor(Date.class, date -> person.birthday = date);
                            case "carCount":
                                return ctx.createIntVisitor(count -> person.carCount = count);
                            default:
                                throw ctx.unexpectedKey(key);
                        }
                    }
                };
            }

            @Override
            public void visit(Person person, TypeVisitor visitor) {
                ObjectVisitor objectVisitor = visitor.visitObject();
                objectVisitor.put("firstName").visit(person.fname);
                objectVisitor.put("lastName").visit(person.lname);
                objectVisitor.put("birthday").visit(person.birthday);
                objectVisitor.put("carCount").visit(person.carCount);
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
        input.carCount = 5;
        Person output = binder.transform(input, Person.class);
        assertTrue(output != input, "Transform should create a new Person");
        assertEquals(input.fname, output.fname);
        assertEquals(input.lname, output.lname);
        assertEquals(input.birthday, output.birthday);
        assertEquals(input.carCount, output.carCount);
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
                    TypeVisitor elmVisitor = ctx.createIntVisitor(num -> output[length++] = num);

                    @Override
                    public int[] build() {
                        return Arrays.copyOf(output, length);
                    }

                    @Override
                    public TypeVisitor add() {
                        return elmVisitor;
                    }
                };
            }

            @Override
            public void visit(int[] value, TypeVisitor visitor) {
                ArrayVisitor arrayVisitor = visitor.visitArray();
                for (int num : value) {
                    arrayVisitor.add().visit(num);
                }
            }
        }).build();
        int[] ints = new int[] { 0, 1, 2, 3, 4, 5, 6 };
        long[] longs = binder.transform(ints, long[].class);
        assertEquals(ints.length, longs.length);
        for (int i = 0; i < ints.length; i++) {
            assertEquals((long) ints[i], longs[i], "index=" + i);
        }
        longs = new long[] { 99, 98, 97, 96, 95 };
        ints = binder.transform(longs, int[].class);
        assertEquals(longs.length, ints.length);
        for (int i = 0; i < longs.length; i++) {
            assertEquals((long) longs[i], ints[i], "index=" + i);
        }
    }
}