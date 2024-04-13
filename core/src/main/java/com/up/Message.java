package com.up;

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Message {
    /// Ver `TipoMensaje`
    short tipo;
    int len;
    byte msg[];

    public Message(InputStream in) throws IOException {
        this.tipo = java.nio.ByteBuffer.wrap(in.readNBytes(2)).getShort();
        this.len = java.nio.ByteBuffer.wrap(in.readNBytes(4)).getInt();
        this.msg = in.readNBytes(len);

        // System.out.println("Reading" + this);
    }

    public Message(short tipo, byte msg[]) {
        this.tipo = tipo;
        this.msg = msg;
        this.len = msg.length;

        // System.out.println("Creating " + this);
    }

    public byte[] toBytes() {
        ByteBuffer b = ByteBuffer.allocate(6 + this.len);
        b.putShort(this.tipo);
        b.putInt(this.len);
        b.put(msg);

        return b.array();
    }

    @Override
    public String toString() {
        String res = "Message { tipo: ";
        switch (this.tipo) {
            case TipoMensaje.Identificacion:
                res += "Identificación";
                break;
            case TipoMensaje.Solicitud:
                res += "Solicitud";
                break;
            case TipoMensaje.Resultado:
                res += "Resultado";
                break;
            default:
                res += "ERR";
                break;
        }

        res += ", len: " + len + ", msg: [ ";
        for (byte b : this.msg) {
            res += String.format("%x ", Byte.toUnsignedInt(b));
        }
        res += "] }";
        return res;
    }
}

