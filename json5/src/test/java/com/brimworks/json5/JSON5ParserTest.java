package com.brimworks.json5;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class JSON5ParserTest {
    private static Object NULL = new Object();
    private static String SOURCE = "JSON5ParserTest.java";

    public static List<String> json5Tests() throws IOException {
        // return
        // Collections.singletonList("src/test/resources/json5-tests/comments/inline-comment-following-top-level-value.json5");
        return Files.walk(Paths.get("src/test/resources/json5-tests")).filter(
                path -> !path.toFile().isDirectory() && path.getFileName().toString().matches(".*[.](json5?|js|txt)$"))
                .map(Path::toString).collect(Collectors.toList());
    }

    private JSON5Parser parser = new JSON5Parser();

    @Tag("unit")
    @ParameterizedTest
    @MethodSource
    public void json5Tests(String path) throws IOException {
        boolean expectSuccess = path.matches(".*[.]json5?$");
        boolean ok = false;
        try {
            parser.parse(Paths.get(path));
            ok = true;
        } catch (Exception ex) {
            if (expectSuccess)
                fail("Unexpected error in '" + path + "'", ex);
        }
        if (expectSuccess == ok) {
            return;
        }
        fail("Expected " + (expectSuccess ? "success" : "failure") + ", but got success when parsing " + path);
    }

    @Tag("unit")
    @Test
    public void parseConstants() {
        assertParsed("true", Boolean.TRUE);
        assertParsed("false", Boolean.FALSE);
        assertParsed("null", NULL);
    }

    private void assertParsed(String txt, Object expect) {
        Object[] got = new Object[1];
        parser.setVisitor(new JSON5Visitor() {
            @Override
            public void visit(Number val, int line, long offset) {
                got[0] = val;
            }

            @Override
            public void visit(String val, int line, long offset) {
                got[0] = val;
            }
            @Override
            public void visit(boolean val, int line, long offset) {
                got[0] = val;
            }
            @Override
            public void visitNull(int line, long offset) {
                got[0] = NULL;
            }
        });
        got[0] = null;
        parser.parse(txt, SOURCE);
        assertEquals(expect, got[0]);
    }

    @Tag("unit")
    @Test
    public void parseIntegers() throws IOException {
        assertParsed("0", 0L);
        assertParsed("123", 123L);
        assertParsed("12345", 12345L);
        assertParsed("1234567890", 1234567890L);

        // MIN/MAX (although note that true MAX is promoted to long)
        assertParsed("9223372036854775807", Long.valueOf(Long.MAX_VALUE));
        assertParsed("-9223372036854775807", Long.valueOf(-Long.MAX_VALUE));

        assertParsed("92233720368547758070", new BigInteger("92233720368547758070"));
        assertParsed("-92233720368547758070", new BigInteger("-92233720368547758070"));
    }

    @Tag("unit")
    @Test
    public void parseFloats() throws IOException {
        assertParsed("0.0", Double.valueOf(0.0));
        assertParsed("10.0", Double.valueOf(10.0));

        // Threshold we should be promoting to BigDecimal for floating values:
        assertParsed("9.007199254740991e+15", Double.valueOf(9.007199254740991e+15));
        assertParsed("9.007199254740992e+15", new BigDecimal("9.007199254740992e+15"));

        assertParsed("-9.007199254740991e+15", Double.valueOf(-9.007199254740991e+15));
        assertParsed("-9.007199254740992e+15", new BigDecimal("-9.007199254740992e+15"));

        assertParsed("9.1e+15", new BigDecimal("9.1e+15"));
        assertParsed("-9.1e+15", new BigDecimal("-9.1e+15"));

        // Threshold we should be promoting to BigInteger for integer values:
        assertParsed("9223372036854775807", Long.valueOf(9223372036854775807L));
        assertParsed("9223372036854775808", new BigInteger("9223372036854775808"));

        assertParsed("-9223372036854775807", Long.valueOf(-9223372036854775807L));
        assertParsed("-9223372036854775808", new BigInteger("-9223372036854775808"));

        // Ensure the decimal point causes BigDecimal representation.
        assertParsed("9223372036854775808.0", new BigDecimal("9223372036854775808.0"));
        assertParsed("-9223372036854775808.0", new BigDecimal("-9223372036854775808.0"));

        // Number of zeros threshold:
        assertParsed("9.000000000000000", Double.valueOf(9.0));
        assertParsed("9.0000000000000000", new BigDecimal("9.0000000000000000"));
        assertParsed("-9.000000000000000", Double.valueOf(-9.0));
        assertParsed("-9.0000000000000000", new BigDecimal("-9.0000000000000000"));

        // Special numbers:
        assertParsed("NaN", Double.valueOf(Double.NaN));
        assertParsed("-Infinity", Double.valueOf(Double.NEGATIVE_INFINITY));
        assertParsed("+Infinity", Double.valueOf(Double.POSITIVE_INFINITY));
    }

    @Tag("unit")
    @Test
    public void parseHex() throws IOException {
        assertParsed("0x0", Long.valueOf(0));
        assertParsed("0xf", Long.valueOf(0xf));
        assertParsed("0xF", Long.valueOf(0xF));
        assertParsed("0xDDD", Long.valueOf(0xDDD));

        // Moment of hex number threshold turning into big integer:
        assertParsed("0x7FFFFFFFFFFFFFFF", Long.valueOf(0x7FFFFFFFFFFFFFFFL));
        assertParsed("0x8000000000000000", new BigInteger("9223372036854775808"));

        assertParsed("-0x7FFFFFFFFFFFFFFF", Long.valueOf(-0x7FFFFFFFFFFFFFFFL));
        assertParsed("-0x8000000000000000", new BigInteger("-9223372036854775808"));
    }

    @Tag("unit")
    @Test
    public void parseString() throws IOException {
        assertParsed("\"hello\\\"world\"", "hello\"world");
        assertParsed("\"hello\\nworld\"", "hello\nworld");
        assertParsed("\"\"", "");
    }

    @Tag("unit")
    @Test
    public void treeBuilderExample() throws IOException {
        List<Object> stack = new ArrayList<>();
        parser.setVisitor(new JSON5Visitor() {
            @Override
            public void visit(Number val, int line, long offset) {
                stack.add(val);
            }

            @Override
            public void visit(String val, int line, long offset) {
                stack.add(val);
            }

            @Override
            public void visitNull(int line, long offset) {

                stack.add(null);
            }

            @Override
            public void visit(boolean val, int line, long offset) {
                stack.add(val);
            }

            @Override
            public void startObject(int line, long offset) {
                stack.add(new HashMap<>());
            }

            @Override
            public void endObjectPair(String key, int line, long offset) {
                Object val = stack.remove(stack.size() - 1);
                ((Map) stack.get(stack.size() - 1)).put(key, val);
            }

            @Override
            public void startArray(int line, long offset) {
                stack.add(new ArrayList<>());
            }

            @Override
            public void endArrayValue(int line, long offset) {
                Object val = stack.remove(stack.size() - 1);
                ((List) stack.get(stack.size() - 1)).add(val);
            }
        });

        parser.parse(Paths.get("src/test/resources/example1.json5"));
        Object value = stack.remove(0);
        assertEquals(map(person -> {
            person.put("fname", "George");
            person.put("lname", "Henderson");
            person.put("pets", list(pets -> {
                pets.add(map(pet -> {
                    pet.put("type", "duck");
                    pet.put("name", "Donald");
                }));
                pets.add(map(pet -> {
                    pet.put("type", "dog");
                    pet.put("name", "Oscar");
                }));
            }));
            person.put("favoriteFood", null);
            person.put("isNice", false);
            person.put("friends", list(friend -> {
                friend.add("Joe");
                friend.add("Bob");
                friend.add("Mary");
            }));
            person.put("age", 35L);
        }), value);

        parser.parse(ByteBuffer.wrap("{True:true,False:false,Null:null}".getBytes(UTF_8)), "string");
        value = stack.remove(0);
        assertEquals(map(map -> {
            map.put("True", Boolean.TRUE);
            map.put("False", Boolean.FALSE);
            map.put("Null", null);
        }), value);
    }

    private Map<String, Object> map(Consumer<Map<String, Object>> fn) {
        Map<String, Object> map = new HashMap<>();
        fn.accept(map);
        return map;
    }

    private List<Object> list(Consumer<List<Object>> fn) {
        List<Object> list = new ArrayList<>();
        fn.accept(list);
        return list;
    }
}