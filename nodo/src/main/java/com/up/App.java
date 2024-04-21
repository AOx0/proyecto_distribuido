package com.up;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.f4b6a3.uuid.UuidCreator;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) throws IOException, NoAvailablePort, InterruptedException {
        Config config = null;
        try {
            String ruta_config = getRutaConfigArgs(args);
            config = ConfigBuilder.parseFromFileConfig(ruta_config);
        } catch (NoConfigPath e) {
            logger.error(
                    "Error: No se especificó una ruta de configuración. Debe especificarse como argumento del programa o en la variable de entorno NODO_CONF");
            System.exit(1);
        } catch (ConfigError e) {
            logger.error("Hubo un error al intentar leer la configuración");
            System.exit(1);
        } catch (FileNotFoundException e) {
            logger.error("El archivo de configuración especificado no existe");
            System.exit(1);
        }

        Connections connections = new Connections();

        String addr = config.get_section_value("nodes", "addr");
        List<Integer> ports = config.get_section_values("nodes", "ports")
                .stream()
                .map(v -> Integer.valueOf(v, 10))
                .collect(Collectors.toList());

        /* Random delay to enable Node sync on startup */
        long delay = new Random().nextLong(1, 25) * 200 + 200;
        System.out.println("Sleeping " + delay);
        Thread.sleep(delay);

        ServerSocket server = createServerSocket(ports);
        UUID node_id = UuidCreator.getRandomBased();
        System.out.println("UUID: " + node_id);

        for (Integer port : ports) {
            if (port == server.getLocalPort())
                continue;
            try {
                Socket socket = new Socket(addr, port);
                DataInputStream in = new DataInputStream(socket.getInputStream());

                Message identificate = MessageBuilder
                        .Identificate(Connection.ConnectionType.Node)
                        .setOrigin(node_id);

                Messenger.send(socket, identificate);

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

                    Message identificate = MessageBuilder
                            .Identificate(Connection.ConnectionType.Node)
                            .setOrigin(node_id);

                    Messenger.send(socket, identificate);

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
                System.out.println(ms);
                switch (connection.getTipo()) {
                    case Connection.ConnectionType.Node:
                        conexiones.send_to_clients_solvers(ms);
                        conexiones.send_to_clients_requesters(ms);
                        break;
                    case Connection.ConnectionType.ClientRequester:
                        ms.setOrigin(connection.getID());
                        conexiones.send_to_nodes(ms);
                        conexiones.send_to_clients_solvers(ms);
                        break;
                    case Connection.ConnectionType.ClientSolver:
                        ms
                                .setDestiny(ms.getOrigin())
                                .setOrigin(connection.getID());
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

    public static String getRutaConfigArgs(String[] args) throws NoConfigPath {
        String ruta_config = System.getenv("NODO_CONF");

        if (ruta_config == null || ruta_config.isEmpty()) {
            try {
                ruta_config = args[1];
            } catch (IndexOutOfBoundsException e) {
                throw new NoConfigPath();
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
}

class NoConfigPath extends Exception {
}
