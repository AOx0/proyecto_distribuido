package com.up;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class Connection {
    private int id;
    private int port;
    private InetAddress addr;
    /// Ver `TipoConexion`
    private byte tipo;

    public int getId() {
        return id;
    }

    public boolean isValid() {
        return !(id == 0 || tipo == ConnectionType.Unknown);
    }

    public byte getTipo() {
        return tipo;
    }

    public Connection(Socket socket, Message msg) {
        if (msg.tipo != MessageType.Identificate || msg.len() != 5 || !ConnectionType.ValorEnRango(msg.msg[0]))
            return;

        this.port = socket.getPort();
        this.addr = socket.getInetAddress();
        this.tipo = msg.msg[0];
        this.id = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(msg.msg, 1, 5)).getInt();
    }

    @Override
    public String toString() {
        return "Conexion { "
                + "addr: " + this.addr + ":" + this.port
                + " id: " + this.getId()
                + ", tipo: " + ConnectionType.toString(this.tipo)
                + " }";
    }

}
