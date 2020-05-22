package com.brimworks.json5.databind;

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
import com.brimworks.databind.DataBind;
import com.brimworks.databind.TypeAdapter;
import com.brimworks.databind.TypeBuilderContext;
import com.brimworks.databind.ObjectVisitorBuilder;
import com.brimworks.databind.ObjectVisitor;
import com.brimworks.databind.ArrayVisitorBuilder;
import com.brimworks.databind.ArrayVisitor;
import com.brimworks.databind.TypeVisitor;
import java.util.function.Consumer;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import com.google.common.reflect.TypeToken;

public class JSON5DataBindTest {
    private static String SOURCE = "JSON5ParserTest.java";

    public static class Person {
        public String fname;
        public String lname;
    }

    public static class Users {

        public List<User> users;

        @Override
        public String toString() {
            return "Users{" + "users=" + users + '}';
        }

        public static final class User {
            public String _id;
            public int index;
            public String guid;
            public boolean isActive;
            public String balance;
            public String picture;
            public int age;
            public String eyeColor;
            public String name;
            public String gender;
            public String company;
            public String email;
            public String phone;
            public String address;
            public String about;
            public String registered;
            public double latitude;
            public double longitude;
            public List<String> tags;
            public List<Friend> friends;
            public String greeting;
            public String favoriteFruit;

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof User)) {
                    return false;
                }
                User user = (User) o;
                return index == user.index && isActive == user.isActive && age == user.age
                        && Math.abs(Double.doubleToLongBits(user.latitude) - Double.doubleToLongBits(latitude)) < 3
                        && Math.abs(Double.doubleToLongBits(user.longitude) - Double.doubleToLongBits(longitude)) < 3
                        && Objects.equals(_id, user._id) && Objects.equals(guid, user.guid)
                        && Objects.equals(balance, user.balance) && Objects.equals(picture, user.picture)
                        && Objects.equals(eyeColor, user.eyeColor) && Objects.equals(name, user.name)
                        && Objects.equals(gender, user.gender) && Objects.equals(company, user.company)
                        && Objects.equals(email, user.email) && Objects.equals(phone, user.phone)
                        && Objects.equals(address, user.address) && Objects.equals(about, user.about)
                        && Objects.equals(registered, user.registered) && Objects.equals(tags, user.tags)
                        && Objects.equals(friends, user.friends) && Objects.equals(greeting, user.greeting)
                        && Objects.equals(favoriteFruit, user.favoriteFruit);
            }

            @Override
            public int hashCode() {
                return Objects.hash(_id, index, guid, isActive, balance, picture, age, eyeColor, name, gender, company,
                        email, phone, address, about, registered, tags, friends, greeting, favoriteFruit);
            }

            @Override
            public String toString() {
                return "JsonDataObj{" + "_id=" + _id + ", index=" + index + ", guid=" + guid + ", isActive=" + isActive
                        + ", balance=" + balance + ", picture=" + picture + ", age=" + age + ", eyeColor=" + eyeColor
                        + ", name=" + name + ", gender=" + gender + ", company=" + company + ", email=" + email
                        + ", phone=" + phone + ", address=" + address + ", about=" + about + ", registered="
                        + registered + ", latitude=" + latitude + ", longitude=" + longitude + ", tags=" + tags
                        + ", friends=" + friends + ", greeting=" + greeting + ", favoriteFruit=" + favoriteFruit + '}';
            }
        }

        public static final class Friend {
            public String id;
            public String name;

            public Friend() {
            }

            public static Friend create(String id, String name) {
                Friend friend = new Friend();
                friend.id = id;
                friend.name = name;
                return friend;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof Friend))
                    return false;

                Friend friend = (Friend) o;

                if (id != null ? !id.equals(friend.id) : friend.id != null)
                    return false;
                return name != null ? name.equals(friend.name) : friend.name == null;
            }

            @Override
            public int hashCode() {
                int result = id != null ? id.hashCode() : 0;
                result = 31 * result + (name != null ? name.hashCode() : 0);
                return result;
            }

            @Override
            public String toString() {
                return "Friend{" + "id=" + id + ", name=" + name + '}';
            }

        }
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
            }
        }).build();

        JSON5DataBind json5 = new JSON5DataBind(binder);
        Person jim = json5.parse("{\"firstName\":\"Jim\",\"lastName\":\"Bean\"}", SOURCE, Person.class);
        assertEquals(jim.fname, "Jim");
        assertEquals(jim.lname, "Bean");
        Person tina = json5.parse("{\"firstName\":\"Tina\",\"lastName\":\"French\"}", SOURCE, Person.class);
        assertEquals(tina.fname, "Tina");
        assertEquals(tina.lname, "French");
    }

    @Tag("unit")
    @Test
    public void testUser() throws IOException {
        JSON5DataBind json5 = new JSON5DataBind(new DataBind.Builder().put(new TypeAdapter<Users>() {
            @Override
            public Type getRawType() {
                return Users.class;
            }

            @Override
            public ObjectVisitorBuilder<Users> createObject(TypeBuilderContext ctx) {
                return new ObjectVisitorBuilder<Users>() {
                    Users users = new Users();

                    @Override
                    public Users build() {
                        return users;
                    }

                    @Override
                    public TypeVisitor put(String key) {
                        switch (key) {
                            case "users":
                                return ctx.createVisitor(new TypeToken<List<Users.User>>() {
                                }.getType(), list -> users.users = (List<Users.User>) list);
                            default:
                                throw ctx.unexpectedKey(key);
                        }
                    }
                };
            }

            @Override
            public void visit(Users users, TypeVisitor visitor) {
                visitor.visitObject().put("users").visit(users.users);
            }
        }).put(new TypeAdapter<List<Users.User>>() {
            @Override
            public Type getRawType() {
                return new TypeToken<List<Users.User>>() {
                }.getType();
            }

            @Override
            public ArrayVisitorBuilder<List<Users.User>> createArray(TypeBuilderContext ctx) {
                return new ArrayVisitorBuilder<List<Users.User>>() {
                    List<Users.User> users = new ArrayList<>();
                    TypeVisitor elmVisitor = ctx.createVisitor(Users.User.class, user -> users.add(user));

                    @Override
                    public List<Users.User> build() {
                        return users;
                    }

                    @Override
                    public TypeVisitor add() {
                        return elmVisitor;
                    }
                };
            }

            @Override
            public void visit(List<Users.User> users, TypeVisitor visitor) {
                ArrayVisitor dst = visitor.visitArray();
                for (Users.User user : users) {
                    dst.add().visit(user);
                }
            }
        }).put(new TypeAdapter<Users.User>() {
            @Override
            public Type getRawType() {
                return Users.User.class;
            }

            @Override
            public ObjectVisitorBuilder<Users.User> createObject(TypeBuilderContext ctx) {
                return new ObjectVisitorBuilder<Users.User>() {
                    Users.User user = new Users.User();

                    @Override
                    public Users.User build() {
                        return user;
                    }

                    @Override
                    public TypeVisitor put(String key) {
                        switch (key) {
                            case "_id":
                                return ctx.createVisitor(String.class, str -> user._id = str);
                            case "name":
                                return ctx.createVisitor(String.class, str -> user.name = str);
                            default:
                                throw ctx.unexpectedKey(key);
                        }
                    }
                };
            }

            @Override
            public void visit(Users.User user, TypeVisitor visitor) {
            }
        }).build());
        Users users = json5.parse("{\"users\":[{\"_id\":\"Jim\",\"name\":\"Bean\"}]}", SOURCE, Users.class);
    }
}