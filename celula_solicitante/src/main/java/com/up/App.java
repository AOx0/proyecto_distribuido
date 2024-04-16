package com.up;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        Message ident = BytesBuilder.Identificate(Connection.ConnectionType.ClientRequester);
        Messenger.send(out, ident);

        Connection con = new Connection(socket, Messenger.read(in));
        System.out.println("Conectado exitosamente: " + con);

        Messenger.send(out, BytesBuilder.Request(Message.RequestType.Add, 10.0, 11.0));
        System.out.println("Mandando solicitud");
    	System.out.println("Respuesta: " + Messenger.read(in));
    }
}
