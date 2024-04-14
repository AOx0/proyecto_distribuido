package com.up;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) throws IOException, NoAvailablePort, InterruptedException {
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

        List<Integer> ports = config.get_section_value("nodes", "ports")
                .stream()
                .map(v -> Integer.valueOf(v, 10))
                .collect(Collectors.toList());
        String addr = config.get_first_section_value("nodes", "addr");

        /* Random delay to enable Node sync on startup */
        long delay = Math.abs(new Random().nextLong()) % 5000;
        System.out.println("Sleeping " + delay);
        Thread.sleep(delay);

        ServerSocket server = createServerSocket(ports);
        for (Integer port : ports) {
            if (port == server.getLocalPort()
                    || conexiones.nodos.keySet().stream().anyMatch(k -> k.getId() == port))
                continue;
            try {
                Socket socket = new Socket(addr, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                Messenger.send(out, MessageBuilder.Identificate(TipoConexion.Nodo, server.getLocalPort()));
                Conexion conexion = new Conexion(socket, Messenger.read(in));

                if (!conexion.isValid() || !conexiones.addConnection(conexion, socket)) {
                    socket.close();
                    continue;
                }

                Thread handle = new Thread(() -> handle(conexiones, socket, conexion, in));
                handle.setName("Handle " + conexion);
                handle.start();
            } catch (IOException e) {
            }
        }

        Thread acceptor = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = server.accept();

                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    Conexion conexion = new Conexion(socket, Messenger.read(in));
                    if (!conexion.isValid() ||
                            !conexiones.addConnection(conexion, socket)) {
                        socket.close();
                        continue;
                    }

                    socket
                            .getOutputStream()
                            .write(
                                    MessageBuilder
                                            .Identificate(TipoConexion.Nodo, server.getLocalPort())
                                            .toBytes());

                    Thread handle = new Thread(() -> handle(conexiones, socket, conexion, in));
                    handle.setName("Handle " + conexion);
                    handle.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        acceptor.setName("Acceptor");
        acceptor.start();

        try {
            acceptor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void handle(Conexiones conexiones, Socket socket, Conexion conexion, DataInputStream in) {
        while (true) {
            try {
                Message ms = Messenger.read(in);
                switch (conexion.getTipo()) {
                    case TipoConexion.Nodo:
                        conexiones.clients_requesters.forEach((con, sock) -> {
                            try {
                                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                                out.write(ms.toBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        break;
                    case TipoConexion.CelulaServer:
                        conexiones.nodos.forEach((con, sock) -> {
                            try {
                                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                                out.write(ms.toBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        conexiones.clients_requesters.forEach((con, sock) -> {
                            try {
                                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                                out.write(ms.toBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        break;
                    case TipoConexion.CelulaConsumer:
                    default:
                        break;
                }
            } catch (IOException e) {
                conexiones.rmConnection(conexion);

                try {
                    socket.close();
                } catch (IOException e1) {
                }

                return;
            }
        }
    }

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

class Conexiones {
    HashMap<Conexion, Socket> nodos;
    HashMap<Conexion, Socket> clients_servers;
    HashMap<Conexion, Socket> clients_requesters;

    public Conexiones() {
        this.nodos = new HashMap<Conexion, Socket>();
        this.clients_servers = new HashMap<Conexion, Socket>();
        this.clients_requesters = new HashMap<Conexion, Socket>();
    }

    public void rmConnection(Conexion con) {
        switch (con.getTipo()) {
            case TipoConexion.Nodo:
                System.out.println("Eliminando nodo: " + con);
                this.nodos.remove(con);
                break;
            case TipoConexion.CelulaConsumer:
                System.out.println("Eliminando celula: " + con);
                this.clients_requesters.remove(con);
                break;
            case TipoConexion.CelulaServer:
                System.out.println("Eliminando celula: " + con);
                this.clients_servers.remove(con);
                break;
            default:
                return;
        }
    }

    public boolean addConnection(Conexion con, Socket socket) {
        switch (con.getTipo()) {
            case TipoConexion.Nodo:
                if (this.nodos.keySet().stream().anyMatch(x -> x.getId() == con.getId())) {
                    return false;
                }
                System.out.println("Nuevo nodo: " + con);
                this.nodos.put(con, socket);
                break;
            case TipoConexion.CelulaConsumer:
                System.out.println("Nueva celula: " + con);
                this.clients_requesters.put(con, socket);
                break;
            case TipoConexion.CelulaServer:
                System.out.println("Nueva celula: " + con);
                this.clients_servers.put(con, socket);
                break;
            default:
                System.out.println("Descartando conexión no identificada\n:    " + con);
                return false;
        }

        return true;
    }
}

class NoAvailablePort extends Exception {
    @Override
    public String toString() {
        return "NoAvailablePort [ No se pudo establecer un ServerSocket con ningún puerto ]";
    }
}
