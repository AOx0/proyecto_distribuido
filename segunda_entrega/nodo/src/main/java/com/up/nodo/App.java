package com.up.nodo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TipoConexion {
    static final byte Unknown = 0;
    static final byte Nodo = 1;
    static final byte CelulaServer = 2;
    static final byte CelulaConsumer = 3;

    public static boolean ValorEnRango(byte valor) {
        return valor > Unknown && valor <= CelulaConsumer;
    }
}

class TipoMensaje {
    /// Indicar al endpoint que se indica nuestra identidad
    static final short PushIdent = 1;
    /// Solicitar al endpoint su información de identidad
    static final short PullIdent = 2;
    static final short Solicitud = 3;
    static final short Resultado = 4;
}

class Message {
    /// Ver `TipoMensaje`
    short tipo;
    int len;
    byte msg[];

    public Message read(InputStream in) throws IOException {
        short tipo = java.nio.ByteBuffer.wrap(in.readNBytes(2)).getShort();
        int len = java.nio.ByteBuffer.wrap(in.readNBytes(4)).getInt();
        byte msg[] = in.readNBytes(len);

        return new Message(tipo, msg);
    }

    public Message(InputStream in) throws IOException {
        this.tipo = java.nio.ByteBuffer.wrap(in.readNBytes(2)).getShort();
        this.len = java.nio.ByteBuffer.wrap(in.readNBytes(4)).getInt();
        this.msg = in.readNBytes(len);
    }
    
    public Message(short tipo, byte msg[]) throws IOException {
        this.tipo = tipo;
        this.msg = msg;
        this.len = msg.length;
    }

    public byte[] toBytes() {
        ByteBuffer b = ByteBuffer.allocate(6 + this.len);
        b.putShort(this.tipo);
        b.putInt(this.len);
        b.put(msg);

        return b.array();
    }

    @Override
    public String toString() {
        String res = "Message { tipo: ";
        switch (this.tipo) {
            case TipoMensaje.PushIdent:
                res += "Identificación";
                break;
            case TipoMensaje.Solicitud:
                res += "Solicitud";
                break;
            case TipoMensaje.Resultado:
                res += "Resultado";
                break;
            default:
                res += "ERR";
                break;
        }

        res += ", len: " + len + ", msg: [ ";
        for (byte b : this.msg) {
            res += String.format("%x ", Byte.toUnsignedInt(b));
        }
        res += "] }";
        return res;
    }
}

class Conexion {
    private InetAddress addr;
    private int port;
    /// Ver `TipoConexion`
    private byte tipo;

    public void setTipo(byte tipo) {
        this.tipo = tipo;
    }

    public Conexion(Socket socket) {
        this.addr = socket.getInetAddress();
        this.port = socket.getPort();
        this.tipo = 0;
    }

    @Override
    public String toString() {
        String res = "Conexion { addr: " + this.addr + ":" + this.port + ", tipo: ";
        switch (this.tipo) {
            case TipoConexion.Unknown:
                res += "Unknown";
                break;
            case TipoConexion.Nodo:
                res += "Nodo";
                break;
            case TipoConexion.CelulaConsumer:
                res += "CelulaConsumer";
                break;
            case TipoConexion.CelulaServer:
                res += "CelulaServer";
                break;
            default:
                res += "ERR";
                break;
        }

        return res + " }";
    }

}

class KeyVal {
    String key;
    Vector<String> val;

    public KeyVal(String key, Vector<String> val) {
        this.key = key;
        this.val = val;
    }
}

class NoAvailablePort extends Exception {
    @Override
    public String toString() {
        return "NoAvailablePort [ No se pudo establecer un ServerSocket con ningún puerto ]";
    }
}

class ConfigError extends Exception {
    @Override
    public String toString() {
        return "ConfigError []";
    }
}

public class App {
    public static void main(String[] args) throws IOException, NoAvailablePort {
        String ruta_config = getRuta_config(args);
        HashMap<String, Vector<String>> config = null;
        try (BufferedReader bin = new BufferedReader(new FileReader(ruta_config));) {
            try {
                config = parseConfig(bin.lines());
            } catch (ConfigError e) {
                System.out.println("Hubo un error al intentar leer la configuración");
                System.exit(1);
            }
        }

        HashMap<Conexion, Socket> conexiones = new HashMap<Conexion, Socket>();
        System.out.println("Config: " + config);
        List<Integer> ports = config.get("ports")
                .stream()
                .map(v -> Integer.valueOf(v, 10))
                .collect(Collectors.toList());

        ServerSocket server = createServerSocket(ports);

        for (Integer port: ports) {
            if (port == server.getLocalPort()) {
                continue;
            }
            try {
                Socket socket = new Socket("127.0.0.1", port);
                Conexion nodo = new Conexion(socket);

                byte bytes[] = new byte[] { TipoConexion.Nodo };
                Message msg = new Message(TipoMensaje.PushIdent, bytes);

                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                out.write(msg.toBytes());

                Message ack_msg = new Message(in);
                if (ack_msg.tipo != TipoMensaje.PushIdent || !TipoConexion.ValorEnRango(ack_msg.msg[0])) {
                    System.out.println(
                        "Descartando paquete: "
                            + "No es de identificación o no especifica un tipo de conexión válido:\n"
                            + "    " + msg
                    );
                    continue;
                }

                nodo.setTipo(ack_msg.msg[0]);

                System.out.println("Nueva conexión de " + nodo);
                conexiones.put(nodo, socket);
            } catch (IOException e) {
                
            }
        }

        while (true) {
            Socket client = server.accept();
            Conexion info_cliente = new Conexion(client);

            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            Message msg = new Message(in);
            if (msg.tipo != TipoMensaje.PushIdent || !TipoConexion.ValorEnRango(msg.msg[0])) {
                System.out.println(
                    "Descartando paquete: "
                        + "No es de identificación o no especifica un tipo de conexión válido:\n"
                        + "    " + msg
                );
                continue;
            }

            info_cliente.setTipo(msg.msg[0]);
            System.out.println("Nueva conexión de " + info_cliente);
            conexiones.put(info_cliente, client);

            
            byte bytes[] = new byte[] { TipoConexion.Nodo };
            Message ack_msg = new Message(TipoMensaje.PushIdent, bytes);
            out.write(ack_msg.toBytes());
        }
    }

    private static ServerSocket createServerSocket(List<Integer> ports) throws NoAvailablePort {
        ServerSocket server = null;
        for (Integer port : ports) {
            try {
                server = new ServerSocket(port);
                System.out.println("Servidor escuchando en 127.0.0.1:" + port);
                break;
            } catch (IOException e) {
                continue;
            }
        }

        if (server == null) {
            throw new NoAvailablePort();
        }

        return server;
    }

    private static HashMap<String, Vector<String>> parseConfig(Stream<String> contents) throws ConfigError {
        final boolean collect_error[] = new boolean[] { false };
        final int line_num[] = new int[] { 1 };

        HashMap<String, Vector<String>> config = contents
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("["))
                .map(line -> {
                    long num_eq = line.chars().filter(x -> x == '=').count();
                    if (num_eq != 1) {
                        System.err.println(
                                "Error en la línea " + line_num[0] + " de la configuración:\n"
                                        + " " + line);
                        line_num[0]++;
                        collect_error[0] = true;
                        return new KeyVal("ERR", new Vector<String>());
                    }

                    String[] keyval = line.split("=");

                    String key = keyval[0].trim().replace("\"", "");
                    Vector<String> values = new Vector<String>();
                    if (keyval[1].contains("[")) {
                        String vals[] = keyval[1]
                            .replace("[", "")
                            .replace("]", "")
                            .split(",");

                        for (String value : vals) {
                            values.add(value.trim().replace("\"", ""));
                        }
                    } else {
                        values.add(keyval[1].trim().replace("\"", ""));
                    }

                    return new KeyVal(key, values);
                })
                .collect(Collectors.toMap(
                    v -> v.key,
                    v -> v.val,
                    (prev, next) -> {
                        prev.addAll(next);
                        return prev;
                    },
                    HashMap::new)
                );

        if (collect_error[0]) {
            throw new ConfigError();
        }

        return config;
    }

    private static String getRuta_config(String[] args) {
        String ruta_config = System.getenv("NODO_CONF");

        if (ruta_config == null || ruta_config.isEmpty()) {
            try {
                ruta_config = args[1];
            } catch (IndexOutOfBoundsException e) {
                System.out.println(
                        "Error: No se especificó una ruta de configuración.\n"
                                + "Debe especificarse como argumento del programa o en la variable de entorno NODO_CONF");
                System.exit(1);
            }
        }

        ruta_config = ruta_config.trim();
        return ruta_config;
    }
}
