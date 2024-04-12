package com.up;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class App 
{
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

        List<Integer> ports = config.get_section_value("nodes", "ports")
                .stream()
                .map(v -> Integer.valueOf(v, 10))
                .collect(Collectors.toList());
        String addr = config.get_section_value("nodes", "addr").get(0);

        ServerSocket server = createServerSocket(ports);
        for (Integer port : ports) {
            if (port == server.getLocalPort())
                continue;
            try {
                Socket socket = new Socket(addr, port);
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
                                .toBytes()
                        );

                    Thread handle = new Thread(() -> {
                        while (true) {
                            try {
								Message ms = new Message(in);
								System.out.println(ms);
							} catch (IOException e) {
								// e.printStackTrace();
								try {
									socket.close();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
                        }
                    });
                    handle.start();
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

class NoAvailablePort extends Exception {
    @Override
    public String toString() {
        return "NoAvailablePort [ No se pudo establecer un ServerSocket con ningún puerto ]";
    }
}
