package com.up;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws NumberFormatException, UnknownHostException, IOException, InterruptedException
    {
        if (args.length < 3) {
            System.err.println("Error: No se especificó dirección y puerto.\n\n    Uso: celula_servidor <ADDR> <PORT>");
        }
        
        String addr = args[1];
        String port = args[2];

        Socket socket = new Socket(addr, Integer.valueOf(port, 10));
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        Message ident = MessageBuilder.Identificacion(TipoConexion.CelulaServer, Integer.valueOf(port, 10));
        out.write(ident.toBytes());

        Conexion con = new Conexion(socket, new Message(in));

        System.out.println("Conectado exitosamente: " + con);

        while (true) {
            out.write(ident.toBytes());
        	Thread.sleep(500);
        }
    }
}
