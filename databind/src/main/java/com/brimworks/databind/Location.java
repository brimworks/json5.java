package com.brimworks.databind;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Objects;

public class Location {
    public interface Element {}
    public static class KeyElement implements Element {
        private String key;
        private KeyElement(String key) {
            Objects.requireNonNull(key);
            this.key = key;
        }
        @Override
        public String toString() {
            return "key=" + escapePath(key);
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeyElement)) {
                return false;
            }
            KeyElement other = (KeyElement)obj;
            return key.equals(other.key);
        }
        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }
    public static class IndexElement implements Element {
        private int index;
        private IndexElement(int index) {
            this.index = index;
        }
        @Override
        public String toString() {
            return "index=" + index;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IndexElement)) {
                return false;
            }
            IndexElement other = (IndexElement)obj;
            return index == other.index;
        }
        @Override
        public int hashCode() {
            return Objects.hash(index);
        }
    }
    public static class TargetTypeElement implements Element {
        private Type type;
        private TargetTypeElement(Type type) {
            Objects.requireNonNull(type);
            this.type = type;
        }
        @Override
        public String toString() {
            return "targetType="+type;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TargetTypeElement)) {
                return false;
            }
            TargetTypeElement other = (TargetTypeElement)obj;
            return type.equals(other.type);
        }
        @Override
        public int hashCode() {
            return Objects.hash(type);
        }
    }
    private Location parent;
    private Element element;
    public Location(Type targetType) {
        Objects.requireNonNull(targetType);
        parent = null;
        element = new TargetTypeElement(targetType);
    }
    public Location(Location parent, Element element) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(element);
        this.parent = parent;
        this.element = element;
    }

    public List<Element> getElements() {
        List<Element> elements = new ArrayList<>();
        Location cur = this;
        while ( cur != null ) {
            elements.add(cur.getElement());
            cur = cur.parent;
        }
        Collections.reverse(elements);
        return elements;
    }

    public Location getParent() {
        return parent;
    }
    public Location findElementOf(Class<? extends Element> type) {
        Location cur = this;
        while ( null != cur ) {
            if ( type.isInstance(cur.getElement()) ) {
                break;
            }
            cur = cur.parent;
        }
        return cur;
    }

    public Element getElement() {
        return element;
    }

    public Location addKey(String key) {
        return new Location(this, new KeyElement(key));
    }

    public Location addIndex(int index) {
        return new Location(this, new IndexElement(index));
    }

    public Location addTargetType(Type targetType) {
        return new Location(this, new TargetTypeElement(targetType));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for ( Element elm : getElements() ) {
            if ( isFirst ) {
                isFirst = false;
            } else {
                // Blue raquo:
                sb.append(" \u001b[32m\u00BB\u001b[0m ");
            }
            sb.append(elm.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Location)) {
            return false;
        }
        Location other = (Location)obj;
        return Objects.equals(parent, other.getParent()) &&
            Objects.equals(element, other.getElement());
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, element);
    }

    private static Pattern PATH_UNSAFE = Pattern.compile("[\\p{C}\\p{Z}/=<>\\\\]");
    private static String escapePath(String str) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = PATH_UNSAFE.matcher(str);
        while ( matcher.find() ) {
            matcher.appendReplacement(sb, String.format("\\u%4x", matcher.group().charAt(0)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}