package com.brimworks.json5;

public class JSON5Key {
    private String key;
    private int index;
    public JSON5Key(String key) {
        if ( null == key ) {
            throw new NullPointerException("Attempt to construct a null path element");
        }
        this.key = key;
    }
    public JSON5Key(int index) {
        this.index = index;
    }
    public String asKey() {
        return key;
    }
    public int asIndex() {
        return index;
    }
    public boolean isObject() {
        return key != null;
    }
    public boolean isArray() {
        return key == null;
    }
    @Override
    public String toString() {
        return null == key ? Integer.toString(index) : key.replaceAll("[~/]", "~$0");
    }
    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof JSON5Key ) ) return false;
        JSON5Key other = (JSON5Key)obj;
        if ( null != key ) return key.equals(other.key);
        return null == other.key && index == other.index;
    }
    @Override
    public int hashCode() {
        return null == key ? Integer.hashCode(index) : key.hashCode();
    }
}