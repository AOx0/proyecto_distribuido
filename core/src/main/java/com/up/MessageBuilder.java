package com.up;

import java.nio.ByteBuffer;

public class MessageBuilder {
    public static final Message Identificate(byte tipo, int id) {
        ByteBuffer b = ByteBuffer.allocate(5);
        b.put(tipo);
        b.putInt(id);
        return new Message(MessageType.Identificate, b.array());
    }

    public static final Message Request(byte payload[]) {
        return new Message(MessageType.Identificate, payload);
    }
    
    public static final Message Restultado(byte payload[]) {
        return new Message(MessageType.Identificate, payload);
    }
}
