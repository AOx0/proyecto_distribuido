package com.up;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args)
            throws NumberFormatException, UnknownHostException, IOException, InterruptedException {
        if (args.length < 3) {
            System.err.println(
                    "Error: No se especificó dirección y puerto.\n\n    Uso: celula_solicitante <ADDR> <PORT>");
        }

        String addr = args[1];
        String port = args[2];

        Socket socket = new Socket(addr, Integer.valueOf(port, 10));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        Message ident = MessageBuilder.Identificate(Connection.ConnectionType.ClientSolver);
        Messenger.send(out, ident);

        Connection con = new Connection(socket, Messenger.read(in));
        System.out.println("Conectado exitosamente: " + con);

        while (true) {
            Message req = Messenger.read(in);
            double lhs = MessageBuilder.GetLhs(req);
            double rhs = MessageBuilder.GetRhs(req);
            double result = 0;
            switch (req.msg[0]) {
              case Message.RequestType.Add: 
                result = lhs + rhs;
                break;
              case Message.RequestType.Sub: 
                result = lhs - rhs;
                break;
              case Message.RequestType.Mul: 
                result = lhs * rhs;
                break;
              case Message.RequestType.Div: 
                result = lhs / rhs;
                break;
            }
            
            Messenger.send(out, MessageBuilder.Restultado(req, result));
            System.out.println("Respondiendo a: " + req);
        }
    }
}
