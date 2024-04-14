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

        String addr = config.get_section_value("nodes", "addr");
        List<Integer> ports = config.get_section_values("nodes", "ports")
                .stream()
                .map(v -> Integer.valueOf(v, 10))
                .collect(Collectors.toList());

        /* Random delay to enable Node sync on startup */
        long delay = Math.abs(new Random().nextLong()) % 10000;
        System.out.println("Sleeping " + delay);
        Thread.sleep(delay);

        ServerSocket server = createServerSocket(ports);
        for (Integer port : ports) {
            if (port == server.getLocalPort()
                    || conexiones.nodes.keySet().stream().anyMatch(k -> k.getId() == port))
                continue;
            try {
                Socket socket = new Socket(addr, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                Messenger.send(out, MessageBuilder.Identificate(ConnectionType.Node, server.getLocalPort()));
                Connection conexion = new Connection(socket, Messenger.read(in));
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
                    Connection conexion = new Connection(socket, Messenger.read(in));
                    if (!conexion.isValid() || !conexiones.addConnection(conexion, socket)) {
                        socket.close();
                        continue;
                    }

                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    Messenger.send(out, MessageBuilder.Identificate(ConnectionType.Node, server.getLocalPort()));

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

    private static void handle(Conexiones conexiones, Socket socket, Connection conexion, DataInputStream in) {
        while (true) {
            try {
                Message ms = Messenger.read(in);
                switch (conexion.getTipo()) {
                    case ConnectionType.Node:
                        conexiones.clients_requesters.forEach((con, sock) -> {
                            try {
                                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                                Messenger.send(out, ms);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        break;
                    case ConnectionType.ClientServer:
                        conexiones.nodes.forEach((con, sock) -> {
                            try {
                                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                                Messenger.send(out, ms);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        conexiones.clients_requesters.forEach((con, sock) -> {
                            try {
                                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                                Messenger.send(out, ms);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        break;
                    case ConnectionType.ClientConsumer:
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
    HashMap<Connection, Socket> nodes;
    HashMap<Connection, Socket> clients_servers;
    HashMap<Connection, Socket> clients_requesters;

    public Conexiones() {
        this.nodes = new HashMap<Connection, Socket>();
        this.clients_servers = new HashMap<Connection, Socket>();
        this.clients_requesters = new HashMap<Connection, Socket>();
    }

    public void rmConnection(Connection con) {
        switch (con.getTipo()) {
            case ConnectionType.Node:
                System.out.println("Removing node: " + con);
                this.nodes.remove(con);
                break;
            case ConnectionType.ClientConsumer:
                System.out.println("Removing celula: " + con);
                this.clients_requesters.remove(con);
                break;
            case ConnectionType.ClientServer:
                System.out.println("Removing celula: " + con);
                this.clients_servers.remove(con);
                break;
            default:
                return;
        }
    }

    public boolean addConnection(Connection con, Socket socket) {
        switch (con.getTipo()) {
            case ConnectionType.Node:
                if (this.nodes.keySet().stream().anyMatch(x -> x.getId() == con.getId())) {
                    return false;
                }
                System.out.println("New node: " + con);
                this.nodes.put(con, socket);
                break;
            case ConnectionType.ClientConsumer:
                System.out.println("Nueva celula: " + con);
                this.clients_requesters.put(con, socket);
                break;
            case ConnectionType.ClientServer:
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
