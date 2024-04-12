package com.up;

import java.net.InetAddress;
import java.net.Socket;

public class Conexion {
    private InetAddress addr;
    private int port;
    /// Ver `TipoConexion`
    private byte tipo;

    public InetAddress getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }

    public byte getTipo() {
        return tipo;
    }

    public boolean setTipo(Message msg) {
        if (msg.tipo != TipoMensaje.Identificacion)
            return false;
        if (msg.len != 1)
            return false;
        if (!TipoConexion.ValorEnRango(msg.msg[0]))
            return false;

        this.tipo = msg.msg[0];
        return true;
    }

    public Conexion(Socket socket) {
        this.addr = socket.getInetAddress();
        this.port = socket.getPort();
        this.tipo = 0;
    }

    @Override
    public String toString() {
        return "Conexion { "
                + "addr: " + this.addr + ":" + this.port
                + ", tipo: " + TipoConexion.toString(this.tipo)
                + " }";
    }

}
