Latest Version: [ ![Download](https://api.bintray.com/packages/brimworks/brimworks.java/brimworks.java/images/download.svg?version=latest) ](https://bintray.com/brimworks/brimworks.java/brimworks.java/0.0.2/link)

Build Health: [![](https://jitci.com/gh/brimworks/brimworks.java/svg)](https://jitci.com/gh/brimworks/brimworks.java)

[Javadocs](https://www.javadoc.io/static/com.brimworks/json5/0.0.1/com/brimworks/json5/package-summary.html)


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
        public void endObjectPair(int line, long offset) {
            Object val = stack.remove(stack.size()-1);
            String key = (String)stack.remove(stack.size()-1);
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
