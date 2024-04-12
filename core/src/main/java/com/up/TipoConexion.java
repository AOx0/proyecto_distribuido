package com.up;

public class TipoConexion {
    static final byte Unknown = 0;
    static final byte Nodo = 1;
    static final byte CelulaServer = 2;
    static final byte CelulaConsumer = 3;

    public static boolean ValorEnRango(byte valor) {
        return valor > Unknown && valor <= CelulaConsumer;
    }

    public static String toString(byte valor) {
        switch (valor) {
            case TipoConexion.Unknown:
                return "Unknown";
            case TipoConexion.Nodo:
                return "Nodo";
            case TipoConexion.CelulaConsumer:
                return "CelulaConsumer";
            case TipoConexion.CelulaServer:
                return "CelulaServer";
            default:
                return "ERR";
        }
    }
}
