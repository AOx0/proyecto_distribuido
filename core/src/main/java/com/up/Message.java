package com.up;

import java.util.Arrays;

public class Message {
    /// Ver `MessageType`
    short tipo;
    byte msg[];

    public int len() {
        return msg.length;
    }

    public Message(short tipo, byte msg[]) {
        this.tipo = tipo;
        this.msg = msg;

        // System.out.println("Creating " + this);
    }

    @Override
    public String toString() {
        String res = "Message { tipo: ";
        res += MessageType.toString(this.tipo);
        
        switch (this.tipo) {
            case MessageType.Identificate:
                res += ", connection: ";
                res += ConnectionType.toString(this.msg[0]);
                res += ", id: ";
                res += java.nio.ByteBuffer.wrap(Arrays.copyOfRange(msg, 1, 5)).getInt();
                break;
            case MessageType.Request:
            case MessageType.Response:
            default:
                res += ", msg: [ ";
                for (byte b : this.msg) {
                    res += String.format("%x ", Byte.toUnsignedInt(b));
                }
                res += "]";
        }

        res += " }";
        return res;
    }
}
