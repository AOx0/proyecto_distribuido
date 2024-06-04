package com.up;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Connection {
    private byte tipo;
    Socket socket;
    int id;
    private AtomicInteger pkgID;

    public static final class ConnectionType {
        static final byte Unknown = 0;
        static final byte Node = 1;
        static final byte ClientRequester = 2;
        static final byte ClientSolver = 3;

        public static boolean ValorEnRango(byte valor) {
            return valor > Unknown && valor <= ClientSolver;
        }

        public static String toString(byte valor) {
            return switch (valor) {
                case ConnectionType.Unknown -> "Unknown";
                case ConnectionType.Node -> "Node";
                case ConnectionType.ClientSolver -> "ClientSolver";
                case ConnectionType.ClientRequester -> "ClientRequester";
                default -> "ERR";
            };
        }
    }

    public boolean isValid() {
        return (tipo != ConnectionType.Unknown);
    }

    public byte getTipo() {
        return tipo;
    }

    public byte[] getCID() {
        ByteBuffer b = ByteBuffer.allocate(8);
        b.putInt(this.id);
        b.putInt(this.getPkg());

        return b.array();
    }

    public byte[] getID() {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(this.id);

        return b.array();
    }

    public int getPkg() {
        return this.pkgID.get();
    }

    public int setPkg(int i) {
        return this.pkgID.compareAndExchange(i, i + 1);
    }

    public Connection(Socket socket, Message msg) {
        if (msg.tipo != Message.MessageType.Identificate)
            return;
        if (msg.msg_len() != 1 || !ConnectionType.ValorEnRango(msg.msg[0]))
            return;

        SecureRandom random = new SecureRandom();

        this.id = random.nextInt();
        this.tipo = msg.msg[0];
        this.socket = socket;
        this.pkgID = new AtomicInteger(0);
    }

    public static String displayID(byte id[]) {
        String ids = "";
        int i = 0;
        for (byte b: id) {
            ids += String.format("%02X", b);
            i += 1;

            if (i < id.length && i % 2 == 0) {
                ids += ":";
            }
        }

        return ids;
    }

    @Override
    public String toString() {
        return "Conexion { "
                + "addr: " + this.socket.getInetAddress() + ":" + this.socket.getPort()
                + ", pkg: " + String.format("%04X", this.pkgID.get())
                + ", id: " + displayID(this.getID())
                + ", tipo: " + ConnectionType.toString(this.tipo)
                + " }";
    }

}
