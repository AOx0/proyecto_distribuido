package com.up;

import java.nio.ByteBuffer;

public class MessageBuilder {
    public static Message Identificacion(byte tipo, int id) {
            ByteBuffer b = ByteBuffer.allocate(5);
            b.put(tipo);
            b.putInt(id);
            return new Message(TipoMensaje.Identificacion, b.array());
    }
}
