package com.up.cliente;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws UnknownHostException, IOException
    {
        System.out.println("Conectando a 127.0.0.1:31303");
        Socket socket = new Socket("127.0.0.1", 31303);
        System.out.println("Conectado!");

        Scanner socket_in = new Scanner(socket.getInputStream());
        System.out.println("Mensaje recibido: " + socket_in.nextLine());

        socket_in.close();
        socket.close();
    }
}
