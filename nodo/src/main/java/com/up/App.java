package com.up;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) throws IOException, NoAvailablePort, InterruptedException {
        Config config = null;
        try {
            String ruta_config = getRutaConfigArgs(args);
            config = ConfigParser.parseFromFileConfig(ruta_config);
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

        HashMap<String, Vector<String>> nodes_section = config.get_section("nodes");
        String addr = nodes_section.get("addr").firstElement();        
        List<Integer> ports = null;
       
        if (nodes_section.get("ports") != null) {
            ports = nodes_section.get("ports")
                .stream()
                .map(v -> Integer.valueOf(v, 10))
                .collect(Collectors.toList());
        } else if (nodes_section.get("ports_range") != null) {
            List<Integer> range = nodes_section.get("ports_range")
                .stream()
                .map(v -> Integer.valueOf(v, 10))
                .collect(Collectors.toList());

            if (range.size() > 2) {
                logger.error("El valor de ports_range espera una lista con dos elementos");
                System.exit(1);
            }

            ports = new ArrayList<Integer>();
            for (Integer i  = range.getFirst(); i <= range.getLast(); i++) {
            	ports.add(i);
            }
        }

        System.out.println("PORTS: " + ports);

        /* Random delay to enable Node sync on startup */
        long delay = new Random().nextLong(1, 25) * 200 + 200;
        logger.info("Sleeping " + delay);
        Thread.sleep(delay);

        ServerSocket server = createServerSocket(ports);
        System.out.println("Servidor escuchando en " + server.getInetAddress() + ":" + server.getLocalPort());

        for (Integer port : ports) {
            if (port == server.getLocalPort())
                continue;
            try {
                Socket socket = new Socket(addr, port);
                DataInputStream in = new DataInputStream(socket.getInputStream());

                Messenger.send(socket, MessageBuilder.Identificate(Connection.ConnectionType.Node));

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

                    Messenger.send(socket, MessageBuilder.Identificate(Connection.ConnectionType.Node));

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
                        ms.setID(connection.getCID());
                        conexiones.send_to_nodes(ms);
                        conexiones.send_to_clients_solvers(ms);
                        break;
                    case Connection.ConnectionType.ClientSolver:
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
