package com.up;

public class ConnectionType {
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
