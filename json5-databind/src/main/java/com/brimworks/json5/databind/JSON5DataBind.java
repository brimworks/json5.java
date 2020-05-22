package com.brimworks.json5.databind;

import com.brimworks.json5.JSON5Parser;
import com.brimworks.json5.JSON5Location;
import com.brimworks.json5.JSON5ParseError;
import com.brimworks.databind.DataBind;
import com.brimworks.databind.VisitorBuilder;
import com.brimworks.databind.UnsupportedTypeError;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.io.IOException;

public class JSON5DataBind {
    private DataBind dataBind;

    public JSON5DataBind(DataBind dataBind) {
        this.dataBind = dataBind;
    }

    public <T> T parse(Path path, Class<T> type) throws IOException, JSON5ParseError, UnsupportedTypeError {
        JSON5TypeVisitor visitor = new JSON5TypeVisitor(dataBind, type);
        JSON5Parser parser = new JSON5Parser(visitor);
        parser.parse(path);
        return (T)visitor.pop();
    }

    public <T> T parse(String str, String sourceName, Class<T> type) throws JSON5ParseError {
        JSON5TypeVisitor visitor = new JSON5TypeVisitor(dataBind, type);
        JSON5Parser parser = new JSON5Parser(visitor);
        parser.parse(str, sourceName);
        return (T)visitor.pop();
    }

    public <T> T parse(ByteBuffer utf8, String sourceName, Class<T> type) throws JSON5ParseError {
        JSON5TypeVisitor visitor = new JSON5TypeVisitor(dataBind, type);
        JSON5Parser parser = new JSON5Parser(visitor);
        parser.parse(utf8, sourceName);
        return (T)visitor.pop();
    }

    public <T> T parse(ReadableByteChannel in, String sourceName, JSON5Location.Read readSource, Class<T> type) throws IOException, JSON5ParseError {
        JSON5TypeVisitor visitor = new JSON5TypeVisitor(dataBind, type);
        JSON5Parser parser = new JSON5Parser(visitor);
        parser.parse(in, sourceName, readSource);
        return (T)visitor.pop();
    }
}