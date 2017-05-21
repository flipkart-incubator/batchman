package com.flipkart.batching.flatbuffer;

public interface FlatBufferParser<T> {
    byte[] serialize(T t);
    T deserialize(byte[] bytes);
}