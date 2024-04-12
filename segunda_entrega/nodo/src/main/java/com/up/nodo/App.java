package com.up.nodo;

import com.up.Config;
import com.up.ConfigBuilder;
import com.up.ConfigError;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

class TipoConexion {
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

class TipoMensaje {
                static final short Identificacion = 1;
                static final short Solicitud = 2;
                static final short Resultado = 3;
}

class MessageBuilder {
                public static Message Identificacion(byte tipo) {
                                return new Message(TipoMensaje.Identificacion, new byte[] { tipo });
                }
}

class Message {
                /// Ver `TipoMensaje`
                short tipo;
                int len;
                byte msg[];

                public Message(InputStream in) throws IOException {
                                this.tipo = java.nio.ByteBuffer.wrap(in.readNBytes(2)).getShort();
                                this.len = java.nio.ByteBuffer.wrap(in.readNBytes(4)).getInt();
                                this.msg = in.readNBytes(len);
                }

                public Message(short tipo, byte msg[]) {
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
                                                case TipoMensaje.Identificacion:
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

class NoAvailablePort extends Exception {
                @Override
                public String toString() {
                                return "NoAvailablePort [ No se pudo establecer un ServerSocket con ningún puerto ]";
                }
}

class Conexiones {
                HashMap<Conexion, Socket> nodos;
                HashMap<Conexion, Socket> clientes;

                public Conexiones() {
                                this.nodos = new HashMap<Conexion, Socket>();
                                this.clientes = new HashMap<Conexion, Socket>();
                }

                public boolean addContection(Conexion con, Socket socket) {
                                switch (con.getTipo()) {
                                                case TipoConexion.Nodo:
                                                                System.out.println("Nuevo nodo: " + con);
                                                                this.nodos.put(con, socket);
                                                                break;
                                                case TipoConexion.CelulaConsumer:
                                                case TipoConexion.CelulaServer:
                                                                System.out.println("Nueva celula: " + con);
                                                                this.clientes.put(con, socket);
                                                                break;
                                                default:
                                                                System.out.println("Descartando conexión no identificada\n:    " + con);
                                                                return false;
                                }

                                return true;
                }
}

public class App {
                public static String getRutaConfigArgs(String[] args) {
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

                public static void main(String[] args) throws IOException, NoAvailablePort {
                                String ruta_config = getRutaConfigArgs(args);

                                Config config = null;
                                try {
                                                config = ConfigBuilder.parseFromFileConfig(ruta_config);
                                } catch (ConfigError e) {
                                                System.out.println("Hubo un error al intentar leer la configuración");
                                                System.exit(1);
                                } catch (FileNotFoundException e) {
                                                System.out.println("El archivo de configuración \"" + ruta_config + "\" no existe");
                                                System.exit(1);
                                }

                                Conexiones conexiones = new Conexiones();

                                System.out.println("Config: " + config);
                                List<Integer> ports = config.get_section_value("nodes", "ports")
                                                                .stream()
                                                                .map(v -> Integer.valueOf(v, 10))
                                                                .collect(Collectors.toList());

                                ServerSocket server = createServerSocket(ports);
                                for (Integer port : ports) {
                                                if (port == server.getLocalPort())
                                                                continue;
                                                try {
                                                                Socket socket = new Socket("127.0.0.1", port);
                                                                Conexion conexion = new Conexion(socket);

                                                                socket
                                                                                                .getOutputStream()
                                                                                                .write(
                                                                                                                                MessageBuilder
                                                                                                                                                                .Identificacion(TipoConexion.Nodo)
                                                                                                                                                                .toBytes());

                                                                InputStream in = socket.getInputStream();
                                                                if (!conexion.setTipo(new Message(in)) ||
                                                                                                !conexiones.addContection(conexion, socket)) {
                                                                                socket.close();
                                                                                continue;
                                                                }
                                                } catch (IOException e) {
                                                }
                                }

                                Thread acceptor = new Thread(() -> {
                                                while (true) {
                                                                try {
                                                                                Socket socket = server.accept();
                                                                                Conexion conexion = new Conexion(socket);
                                                                                InputStream in = socket.getInputStream();
                                                                                if (!conexion.setTipo(new Message(in)) ||
                                                                                                                !conexiones.addContection(conexion, socket)) {
                                                                                                socket.close();
                                                                                                continue;
                                                                                }

                                                                                socket
                                                                                                                .getOutputStream()
                                                                                                                .write(
                                                                                                                                                MessageBuilder
                                                                                                                                                                                .Identificacion(TipoConexion.Nodo)
                                                                                                                                                                                .toBytes());
                                                                } catch (IOException e) {
                                                                                e.printStackTrace();
                                                                }
                                                }
                                });
                                acceptor.start();

                                try {
                                                acceptor.join();
                                } catch (InterruptedException e) {
                                                e.printStackTrace();
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

                                if (server == null)
                                                throw new NoAvailablePort();
                                return server;
                }

}
