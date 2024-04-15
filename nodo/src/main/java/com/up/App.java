package com.up;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

        Connections connections = new Connections();

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
            if (port == server.getLocalPort())
                continue;
            try {
                Socket socket = new Socket(addr, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                Messenger.send(out, BytesBuilder.Identificate(Connection.ConnectionType.Node));
                Connection conexion = new Connection(socket, Messenger.read(in));
                if (!conexion.isValid() || !connections.addConnection(conexion, socket)) {
                    socket.close();
                    continue;
                }

                Thread handle = new Thread(() -> handle(connections, socket, conexion, in));
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
                    if (!conexion.isValid() || !connections.addConnection(conexion, socket)) {
                        socket.close();
                        continue;
                    }

                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    Messenger.send(out, BytesBuilder.Identificate(Connection.ConnectionType.Node));

                    Thread handle = new Thread(() -> handle(connections, socket, conexion, in));
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

    private static void handle(Connections conexiones, Socket socket, Connection connection, DataInputStream in) {
        while (true) {
            try {
                Message ms = Messenger.read(in);
                switch (connection.getTipo()) {
                    case Connection.ConnectionType.Node:
                        conexiones.send_to_clients_solvers(ms);
                        conexiones.send_to_clients_requesters(ms);
                        break;
                    case Connection.ConnectionType.ClientRequester:
                        ms.setOrigin(connection.id);
                        conexiones.send_to_nodes(ms);
                        conexiones.send_to_clients_solvers(ms);
                        break;
                    case Connection.ConnectionType.ClientSolver:
                        ms.setDestiny(ms.getOrigin());
                        ms.setOrigin(connection.id);
                        conexiones.send_to_nodes(ms);
                        conexiones.send_to_clients_requesters(ms);
                    default:
                        break;
                }
            } catch (IOException e) {
                conexiones.rmConnection(connection);

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

class NoAvailablePort extends Exception {
    @Override
    public String toString() {
        return "NoAvailablePort [ No se pudo establecer un ServerSocket con ningún puerto ]";
    }
}
