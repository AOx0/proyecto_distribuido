package com.up;

import java.nio.ByteBuffer;

public class BytesBuilder {
    public static final Message Identificate(byte tipo) {
        ByteBuffer b = ByteBuffer.allocate(1);
        b.put(tipo);
        return new Message(Message.MessageType.Identificate, b.array());
    }

    public static final Message Request(byte type, double lhs, double rhs) {
        ByteBuffer b = ByteBuffer.allocate(1 + (8 * 2));
        b.put(type);
        b.putDouble(lhs);
        b.putDouble(rhs);
        return new Message(Message.MessageType.Request, b.array());
    }

    public static final Message Restultado(Message req, double result) {
        ByteBuffer b = ByteBuffer.allocate(8);
        b.putDouble(result);
        Message msg = new Message(Message.MessageType.Response, b.array());
        msg.from = req.from;
        return msg;
    }
}
