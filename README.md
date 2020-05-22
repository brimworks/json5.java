Latest Version: [ ![Download](https://api.bintray.com/packages/brimworks/json5.java/json5.java/images/download.svg?version=latest) ](https://bintray.com/brimworks/json5.java/json5.java/0.0.3/link)

Build Health: [![](https://jitci.com/gh/brimworks/json5.java/svg)](https://jitci.com/gh/brimworks/json5.java)

[Javadocs](https://www.javadoc.io/doc/com.brimworks/json5)


# JSON5 Library

```java
// Create a parser instance:
JSON5Parser parser = new JSON5Parser();

// Simple stack used for tree building.
List<Object> stack = new ArrayList<>();

// Specify a visitor used to build your AST (Abstract Syntax Tree),
// or perform validation. Note that all tokens of input are visited,
// which enables comments and spaces to be preserved.
parser.setVisitor(
    new JSON5Visitor() {
        @Override
        public void visit(Number val, int line, long offset) {
            if ( !(num instanceof Double) && !(num instanceof Long) ) {
                // Example of obtaining line-precise error messages:
                throw new JSON5ParseError("Unsupported numeric type", parser.getLocation(line, offset));
            }
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
            Object val = stack.remove(stack.size()-1);
            ((Map)stack.get(stack.size()-1)).put(key, val);
        }

        @Override
        public void startArray(int line, long offset) {
            stack.add(new ArrayList<>());
        }

        @Override
        public void endArrayValue(int line, long offset) {
            Object val = stack.remove(stack.size()-1);
            ((List)stack.get(stack.size()-1)).add(val);
        }
    });

// Parse a file:
parser.parse(Paths.get("example.json5"));

// Obtain the results of visiting the tree:
Object value = stack.remove(0);

```

# Databind (coming soon)

I'm currently working on a "universal databind" core model that can be used to convert between types, perform a deep copy of a type, and/or serialize/deserialize a type.

Once I have the core working models, I plan to create adapters to serialize/deserialize with the json5 library above.

Longer term, having adapters for the "gson" and "jackson" serialize/deserialize ecosystem are possibilities. Feel free to file an "issue" if you are interested in helping out.
