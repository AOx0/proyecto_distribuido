package com.up.nodo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Cliente {
    InetAddress addr;
    int port;

    public Cliente(Socket socket) {
        this.addr = socket.getInetAddress();
        this.port = socket.getPort();
    }

    public String toString() {
        return this.addr + ":" + this.port;
    }

}

public class App 
{
    public static void main( String[] args ) throws IOException
    {
        HashMap<Cliente, Socket> conexiones = new HashMap<Cliente, Socket>(); 

        ServerSocket server = new ServerSocket(31303);

        while (true) {
            Socket client = server.accept();
            Cliente info_cliente = new Cliente(client);
            
            System.out.println("Nueva conexión de " + info_cliente);

            System.out.println("Mandando mensaje 'Hola desde el server!'");
            OutputStream out = client.getOutputStream();

            out.write("Hola desde el server!\n".getBytes());

            conexiones.put(info_cliente, client);
        }
    }
}
