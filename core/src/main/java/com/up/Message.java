package com.up;

import java.nio.ByteBuffer;

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

    public byte[] toBytes() {
        ByteBuffer b = ByteBuffer.allocate(6 + msg.length);
        b.putShort(this.tipo);
        b.putInt(msg.length);
        b.put(msg);

        return b.array();
    }

    @Override
    public String toString() {
        String res = "Message { tipo: ";
        switch (this.tipo) {
            case MessageType.Identificate:
                res += "Identificación";
                break;
            case MessageType.Request:
                res += "Request";
                break;
            case MessageType.Response:
                res += "Response";
                break;
            default:
                res += "ERR";
                break;
        }

        res += ", msg: [ ";
        for (byte b : this.msg) {
            res += String.format("%x ", Byte.toUnsignedInt(b));
        }
        res += "] }";
        return res;
    }
}

