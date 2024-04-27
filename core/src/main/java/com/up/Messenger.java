package com.up;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

class Messenger {
    public static void send(DataOutputStream out, Message msg) throws IOException {
        out.writeShort(msg.tipo);
        out.writeInt(msg.len());
        out.writeLong(msg.id);
        out.writeLong(msg.from.getLeastSignificantBits());
        out.writeLong(msg.from.getMostSignificantBits());
        out.writeLong(msg.dest.getLeastSignificantBits());
        out.writeLong(msg.dest.getMostSignificantBits());
        out.write(msg.msg);

        out.flush();
    }

    public static void send(Socket socket, Message msg) throws IOException {
        Messenger.send(new DataOutputStream(socket.getOutputStream()), msg);
    }

    public static Message read(DataInputStream in) throws IOException {
        short tipo = in.readShort();
        int len = in.readInt();
        long id = in.readLong();
        long froml = in.readLong();
        long fromh = in.readLong();
        long destl = in.readLong();
        long desth = in.readLong();
        byte[] msg = in.readNBytes(len);

        Message message = new Message(tipo, id, msg, new UUID(desth, destl), new UUID(fromh, froml));
        return message;
    }

    public static Message read(Socket socket) throws IOException {
        return Messenger.read(new DataInputStream(socket.getInputStream()));
    }
}
