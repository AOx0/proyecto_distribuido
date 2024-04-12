package com.up;

public class MessageBuilder {
    public static Message Identificacion(byte tipo) {
        return new Message(TipoMensaje.Identificacion, new byte[] { tipo });
    }
}
