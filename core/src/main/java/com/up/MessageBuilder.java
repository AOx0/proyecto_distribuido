package com.up;

import java.nio.ByteBuffer;

public class MessageBuilder {
    public static final Message Identificate(byte tipo, int id) {
        ByteBuffer b = ByteBuffer.allocate(5);
        b.put(tipo);
        b.putInt(id);
        return new Message(MessageType.Identificate, b.array());
    }

    public static final Message Request(byte type, double lhs, double rhs) {
        ByteBuffer b = ByteBuffer.allocate(1 + (8 * 2));
        b.put(type);
        b.putDouble(lhs);
        b.putDouble(rhs);
        return new Message(MessageType.Request, b.array());
    }
    
    public static final Message Restultado(Message req, double result) {
        ByteBuffer b = ByteBuffer.allocate(8);
        b.putDouble(result);
        Message msg =  new Message(MessageType.Response, b.array());
        msg.setOrigin(req.from);
        return msg;
    }
}
