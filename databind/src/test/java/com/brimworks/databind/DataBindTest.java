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

public class DataBindTest {
    public static class Person {
        public String fname;
        public String lname;
        public Date birthday;
    }

    @Tag("unit")
    @Test
    public void testBasic() throws IOException {
        DataBind binder = new DataBind.Builder()
        .put(new TypeAdapter<Person>() {
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
                        System.err.println("put("+key+")");
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
        })
        .put(new TypeAdapter<Date>() {
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
}