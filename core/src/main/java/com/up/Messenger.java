package com.up;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class Messenger {
    public static void send(DataOutputStream out, Message msg) throws IOException {
        out.writeShort(msg.tipo);
        out.writeInt(msg.len());
        out.write(msg.msg);
    }

    public static Message read(DataInputStream in) throws IOException {
        return new Message(in.readShort(), in.readNBytes(in.readInt()));
    }
}
