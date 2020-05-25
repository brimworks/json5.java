Latest Version: [ ![Download](https://api.bintray.com/packages/brimworks/json5.java/json5.java/images/download.svg?version=latest) ](https://bintray.com/brimworks/json5.java/json5.java/2.0.0/link)

Build Health: [![](https://jitci.com/gh/brimworks/json5.java/svg)](https://jitci.com/gh/brimworks/json5.java)

[Javadocs](https://www.javadoc.io/doc/com.brimworks/json5)

# Performance Note

I have a fork of the [java-json-benchmark](https://github.com/brimworks/java-json-benchmark) that adds support for Deserializing JSON with this library. Currently, this library is about 20x SLOWER than using Jackson. I'm investigating ways to improve the performance. Initial profiling reveals that a little over 10% of the time is spent in `Ragel.appendStringBufferUTF8()` which performs UTF-8 transcoding.

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

# Databind

If you prefer to avoid writing a visitor, you can use the databind library to parse a file as such:

```java
import com.brimworks.json5.databind.JSON5DataBind;
import com.brimworks.databind.DataBind;

public class Config {
    public String string;
    public double number;
}

JSON5DataBind json5 = new JSON5DataBind(new DataBind.Builder().build());
Config config = json5.parse(Paths.get("config.json5"), Config.class);
```

...and if `config.json5` looks like this:

```javascript
{
    /* Greetings */
    string: "Hello",
    // PI:
    number: 3.14,
}
```

Then the `Config` class will be populated with the "Hello" string and 3.14 number as expected.

# Future Plans?

When writing this, I thought the visitor/builder pattern would make type transformations easier. However,
after working with this pattern for a bit I am coming around to the more prevalent builder/reader paradigm.
One of the problems with visitor/builder pattern is all the lambdas necessary to set the types. It is also
harder to adapt this visitor/buidler pattern to existing json ecosystems.

What does this mean?

I'll experiment with adding new APIs to `JSON5Parser` which return a `JSON5Reader` and then tap into
the gson and jackson databind ecosystems by writing simple adaptors. If you are interested in helping out,
feel free to file an issue.

# Release Notes

## Version 2.0.0

More backwards incompatible changes in the databind library, but the only changes to the json5 parser was adding support for a new `visitIndex()` method which is the array analog to `visitKey()`. Also, a major bug was fixed where the string buffer was not cleared after visiting the "constant" tokens (true, false, null).

The databind library is more complete at this point, supporting all the primitive types.

## Version 1.0.0

This was a major version bump since there was several backwards incompatible changes made. Specifically:

- JSON5Visitor endObjectPair() is now passed in the key of the object pair, and added a visitKey() method which is called INSTEAD of visit(String). This should make it easier to implement custom visitors, sorry if this change broke your code, but it is the right thing to do in the long term.
- Remove the Consumer wrapping of visitors. Specifically, ArrayVisitor.add() and ObjectVisitor.put(String) now return a TypeVisitor rather than taking a Consumer<TypeVisitor> as input.
- Added some methods to various interfaces: TypeBuilderContext and TypeRegistry. This is part of the databind code which is still early "alpha" code, so expect more changes here in the future.
- Added a new json5-databind module. This will eventually make it easier to use JSON5 from arbitrary types, but this is still a work in progress.
