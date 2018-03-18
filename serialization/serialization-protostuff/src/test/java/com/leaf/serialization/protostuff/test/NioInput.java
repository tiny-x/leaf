package com.leaf.serialization.protostuff.test;

import io.protostuff.ByteString;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NioInput implements Input {

    @Override
    public <T> void handleUnknownField(int i, Schema<T> schema) throws IOException {

    }

    @Override
    public <T> int readFieldNumber(Schema<T> schema) throws IOException {
        return 0;
    }

    @Override
    public int readInt32() throws IOException {
        return 0;
    }

    @Override
    public int readUInt32() throws IOException {
        return 0;
    }

    @Override
    public int readSInt32() throws IOException {
        return 0;
    }

    @Override
    public int readFixed32() throws IOException {
        return 0;
    }

    @Override
    public int readSFixed32() throws IOException {
        return 0;
    }

    @Override
    public long readInt64() throws IOException {
        return 0;
    }

    @Override
    public long readUInt64() throws IOException {
        return 0;
    }

    @Override
    public long readSInt64() throws IOException {
        return 0;
    }

    @Override
    public long readFixed64() throws IOException {
        return 0;
    }

    @Override
    public long readSFixed64() throws IOException {
        return 0;
    }

    @Override
    public float readFloat() throws IOException {
        return 0;
    }

    @Override
    public double readDouble() throws IOException {
        return 0;
    }

    @Override
    public boolean readBool() throws IOException {
        return false;
    }

    @Override
    public int readEnum() throws IOException {
        return 0;
    }

    @Override
    public String readString() throws IOException {
        return null;
    }

    @Override
    public ByteString readBytes() throws IOException {
        return null;
    }

    @Override
    public byte[] readByteArray() throws IOException {
        return new byte[0];
    }

    @Override
    public ByteBuffer readByteBuffer() throws IOException {
        return null;
    }

    @Override
    public <T> T mergeObject(T t, Schema<T> schema) throws IOException {
        return null;
    }

    @Override
    public void transferByteRangeTo(Output output, boolean b, int i, boolean b1) throws IOException {

    }
}
