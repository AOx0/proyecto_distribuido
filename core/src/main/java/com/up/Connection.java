package com.up;

import java.net.Socket;
import java.util.Arrays;

public class Connection {
    private int id;
    /// Ver `TipoConexion`
    private byte tipo;
    Socket socket;

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

        this.tipo = msg.msg[0];
        this.id = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(msg.msg, 1, 5)).getInt();
        this.socket = socket;
    }

    @Override
    public String toString() {
        return "Conexion { "
                + "addr: " + this.socket.getInetAddress() + ":" + this.socket.getPort()
                + " id: " + this.getId()
                + ", tipo: " + ConnectionType.toString(this.tipo)
                + " }";
    }

}
