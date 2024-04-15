package com.up;

import java.net.Socket;

public class Connection {
    /// Ver `TipoConexion`
    private byte tipo;
    Socket socket;

    public boolean isValid() {
        return (tipo != ConnectionType.Unknown);
    }

    public byte getTipo() {
        return tipo;
    }

    public Connection(Socket socket, Message msg) {
        if (msg.tipo != MessageType.Identificate || msg.len() != 1 || !ConnectionType.ValorEnRango(msg.msg[0]))
            return;

        this.tipo = msg.msg[0];
        this.socket = socket;
    }

    @Override
    public String toString() {
        return "Conexion { "
                + "addr: " + this.socket.getInetAddress() + ":" + this.socket.getPort()
                + ", tipo: " + ConnectionType.toString(this.tipo)
                + " }";
    }

}
