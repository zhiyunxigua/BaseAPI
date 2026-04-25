package com.xigua.baseAPI.api.protocol.packet;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageFormat;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PyRpcPacker {
    private static void packObject(MessageBufferPacker packer, Object obj) throws IOException {
        if (obj instanceof String) {
            PyRpcPacker.packString(packer, (String)obj);
        } else if (obj instanceof Long) {
            packer.packLong((Long)obj);
        } else if (obj instanceof Integer) {
            packer.packInt((Integer)obj);
        } else if (obj instanceof Map) {
            PyRpcPacker.packMap(packer, (Map)obj);
        } else if (obj instanceof List) {
            PyRpcPacker.packList(packer, (List)obj);
        } else if (obj instanceof Float) {
            packer.packDouble(((Float)obj).floatValue());
        } else if (obj instanceof Double) {
            packer.packDouble((Double)obj);
        } else if (obj instanceof Boolean) {
            packer.packBoolean((Boolean)obj);
        } else if (obj instanceof BigInteger) {
            packer.packBigInteger((BigInteger)obj);
        } else if (obj == null) {
            packer.packNil();
        } else {
            throw new IOException("can not pack type " + obj.getClass().getName());
        }
    }

    private static void packString(MessageBufferPacker packer, String str) throws IOException {
        byte[] b = str.getBytes(StandardCharsets.UTF_8);
        packer.packBinaryHeader(b.length);
        packer.addPayload(b);
    }

    private static void packList(MessageBufferPacker packer, List<Object> list) throws IOException {
        packer.packArrayHeader(list.size());
        for (Object obj : list) {
            PyRpcPacker.packObject(packer, obj);
        }
    }

    private static void packMap(MessageBufferPacker packer, Map<String, Object> map) throws IOException {
        packer.packMapHeader(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            PyRpcPacker.packString(packer, entry.getKey());
            PyRpcPacker.packObject(packer, entry.getValue());
        }
    }

    public static byte[] pack(String namespace, String system, String event, Map<String, Object> data) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packArrayHeader(3);
        packer.packString("ModEventS2C");
        packer.packArrayHeader(4);
        packer.packString(namespace);
        packer.packString(system);
        packer.packString(event);
        PyRpcPacker.packMap(packer, data);
        packer.packNil();
        packer.close();
        return packer.toByteArray();
    }

    public static Object unpackObject(MessageUnpacker unpacker) throws IOException {
        MessageFormat format = unpacker.getNextFormat();
        switch (format.getValueType()) {
            case NIL: {
                unpacker.unpackNil();
                return null;
            }
            case BOOLEAN: {
                return unpacker.unpackBoolean();
            }
            case INTEGER: {
                switch (format) {
                    case UINT64: {
                        BigInteger b = unpacker.unpackBigInteger();
                        BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
                        if (b.compareTo(maxLong) != 1) {
                            return b.longValue();
                        }
                        return b;
                    }
                    case INT64: {
                        return unpacker.unpackLong();
                    }
                    case UINT32: {
                        long l = unpacker.unpackLong();
                        if (l <= Integer.MAX_VALUE) {
                            return (int)l;
                        }
                        return l;
                    }
                }
                return unpacker.unpackInt();
            }
            case FLOAT: {
                return unpacker.unpackDouble();
            }
            case STRING: {
                int size = unpacker.unpackRawStringHeader();
                return new String(unpacker.readPayload(size), StandardCharsets.UTF_8);
            }
            case BINARY: {
                int size = unpacker.unpackBinaryHeader();
                return new String(unpacker.readPayload(size), StandardCharsets.UTF_8);
            }
            case ARRAY: {
                int size = unpacker.unpackArrayHeader();
                ArrayList<Object> list = new ArrayList<Object>(size);
                for (int i = 0; i < size; ++i) {
                    list.add(PyRpcPacker.unpackObject(unpacker));
                }
                return list;
            }
            case MAP: {
                int size = unpacker.unpackMapHeader();
                HashMap<String, Object> map = new HashMap<String, Object>(size);
                for (int i = 0; i < size; ++i) {
                    String key = (String)PyRpcPacker.unpackObject(unpacker);
                    map.put(key, PyRpcPacker.unpackObject(unpacker));
                }
                return map;
            }
        }
        throw new IOException("can not unpack type " + format.getValueType().name());
    }
}

