package com.up;

public class ConnectionType {
    static final byte Unknown = 0;
    static final byte Node = 1;
    static final byte ClientServer = 2;
    static final byte ClientConsumer = 3;

    public static boolean ValorEnRango(byte valor) {
        return valor > Unknown && valor <= ClientConsumer;
    }

    public static String toString(byte valor) {
        switch (valor) {
            case ConnectionType.Unknown:
                return "Unknown";
            case ConnectionType.Node:
                return "Node";
            case ConnectionType.ClientConsumer:
                return "ClientConsumer";
            case ConnectionType.ClientServer:
                return "ClientServer";
            default:
                return "ERR";
        }
    }
}
