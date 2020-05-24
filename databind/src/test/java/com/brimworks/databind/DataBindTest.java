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
        public Person spouse;
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
            public ObjectBuilder<Person> createObject(int size, TypeBuilderContext ctx1) {
                return new ObjectBuilder<Person>() {
                    Person person = new Person();

                    @Override
                    public Person build() {
                        return person;
                    }

                    @Override
                    public TypeVisitor put(String key, TypeBuilderContext ctx) {
                        switch (key) {
                            case "firstName":
                                return ctx.createVisitor(String.class, str -> person.fname = str);
                            case "lastName":
                                return ctx.createVisitor(String.class, str -> person.lname = str);
                            case "birthday":
                                return ctx.createVisitor(Date.class, date -> person.birthday = date);
                            case "carCount":
                                return ctx.createIntVisitor(count -> person.carCount = count);
                            case "spouse":
                                return ctx.createVisitor(Person.class, spouse -> person.spouse = spouse);
                            default:
                                throw ctx.unknownKey();
                        }
                    }
                };
            }

            @Override
            public void visit(Person person, TypeVisitor visitor) {
                ObjectVisitor objectVisitor = visitor.visitObject(4);
                objectVisitor.put("firstName").visit(person.fname);
                objectVisitor.put("lastName").visit(person.lname);
                objectVisitor.put("birthday").visit(person.birthday);
                objectVisitor.put("carCount").visit(person.carCount);
                objectVisitor.put("spouse").visit(person.spouse);
                objectVisitor.done();
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
        input.spouse = new Person();
        input.spouse.fname = "Alissa";
        input.spouse.lname = "Smith";
        Person output = binder.transform(input, Person.class);
        assertTrue(output != input, "Transform should create a new Person");
        assertEquals(input.fname, output.fname);
        assertEquals(input.lname, output.lname);
        assertEquals(input.birthday, output.birthday);
        assertEquals(input.carCount, output.carCount);
        assertEquals(input.spouse.fname, output.spouse.fname);
        assertEquals(input.spouse.lname, output.spouse.lname);
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
            public ArrayBuilder<int[]> createArray(int size, TypeBuilderContext ctx1) {
                return new ArrayBuilder<int[]>() {
                    int[] output = new int[size];
                    int length = 0;

                    @Override
                    public int[] build() {
                        return output;
                    }

                    @Override
                    public TypeVisitor add(TypeBuilderContext ctx) {
                        return ctx.createIntVisitor(num -> output[length++] = num);
                    }
                };
            }

            @Override
            public void visit(int[] value, TypeVisitor visitor) {
                ArrayVisitor arrayVisitor = visitor.visitArray(value.length);
                for (int num : value) {
                    arrayVisitor.add().visit(num);
                }
                arrayVisitor.done();
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