package com.up;

import java.net.Socket;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

public class Connection {
    private byte tipo;
    Socket socket;
    UUID id;

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

    public Connection(Socket socket, Message msg) {
        if (msg.tipo != Message.MessageType.Identificate || msg.len() != 1 || !ConnectionType.ValorEnRango(msg.msg[0]))
            return;

        this.id = UuidCreator.getRandomBased();
        this.tipo = msg.msg[0];
        this.socket = socket;
    }

    @Override
    public String toString() {
        return "Conexion { "
                + "addr: " + this.socket.getInetAddress() + ":" + this.socket.getPort()
                + ", id: " + this.id
                + ", tipo: " + ConnectionType.toString(this.tipo)
                + " }";
    }

}
