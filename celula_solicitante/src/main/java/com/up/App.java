package com.up;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class App {
    public static void main(String[] args)
            throws NumberFormatException, UnknownHostException, IOException, InterruptedException {
        if (args.length < 3) {
            System.err.println("Error: No se especificó dirección y puerto.\n\n    Uso: celula_servidor <ADDR> <PORT>");
        }

        String addr = args[1];
        String port = args[2];

        Socket socket = new Socket(addr, Integer.valueOf(port, 10));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        Message ident = MessageBuilder.Identificate(Connection.ConnectionType.ClientRequester);
        Messenger.send(out, ident);

        Connection con = new Connection(socket, Messenger.read(in));
        System.out.println("Conectado exitosamente: " + con);

        try (Scanner scanner = new Scanner(System.in)) {
			boolean print_msg = true;
			while (true) {
			    if (print_msg) {
			        System.out.print("Tipo de operación (Add|Sub|Div|Mul): ");
			        System.out.flush();
			    } else {
			        print_msg = true;
			    }

			    String opStr = scanner.nextLine();
			    if (opStr.isEmpty()) {
			        print_msg = false;
			        continue;
			    }
			    byte op = Message.RequestType.fromString(opStr);

			    if (op == Message.RequestType.Err) {
			        System.err.println("No se especificó un tipo de operación válido");
			        continue;
			    }

			    try {
			        System.out.print("Operando 1: ");
			        System.out.flush();
			        double lhs = scanner.nextDouble();

			        System.out.print("Operando 2: ");
			        System.out.flush();
			        double rhs = scanner.nextDouble();

			        Messenger.send(out, MessageBuilder.Request(op, lhs, rhs));
			        System.out.println("Mandando solicitud");
			        System.out.println("Respuesta: " + Messenger.read(in));
			    } catch (InputMismatchException e) {
			        System.err.println("No se especificó un número válido");
			    }
			}
		}
    }
}
