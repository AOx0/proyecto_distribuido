package com.up;

import java.util.Arrays;

public class Message {
    /// Ver `MessageType`
    short tipo;
    byte msg[];
    Connection from;
    Connection dest;

    public int len() {
        return msg.length;
    }

    public Message(short tipo, byte msg[]) {
        this.tipo = tipo;
        this.msg = msg;
        this.from = null;
        this.dest = null;
    }

    public void setOrigin(Connection from) {
        this.from = from;
    }
    
    public Connection getOrigin() {
        return this.from;
    }

    public void setDestiny(Connection dest) {
        this.dest = dest;
    }

    @Override
    public String toString() {
        String res = "Message { tipo: ";
        res += MessageType.toString(this.tipo);

        res += ", from: " + from;
        res += ", dest: " + dest;
        
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
