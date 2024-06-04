package com.up;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class Messenger {
    public static void send(DataOutputStream out, Message msg) throws IOException {
        out.writeShort(msg.tipo);
        out.writeShort(msg.dest);
        out.writeShort(msg.evt_len());
        out.write(msg.event_id);
        out.writeShort(msg.msg_len());
        out.write(msg.msg);

        out.flush();
    }

    public static void send(Socket socket, Message msg) throws IOException {
        Messenger.send(new DataOutputStream(socket.getOutputStream()), msg);
    }

    public static Message read(DataInputStream in) throws IOException {
        short tipo = in.readShort();
        short dest = in.readShort();
        short e_len = in.readShort(); 
        byte[] id = in.readNBytes(e_len);
        short p_len = in.readShort();
        byte[] msg = in.readNBytes(p_len);

        Message message = new Message(tipo, id, msg, dest);
        return message;
    }

    public static Message read(Socket socket) throws IOException {
        return Messenger.read(new DataInputStream(socket.getInputStream()));
    }
}
