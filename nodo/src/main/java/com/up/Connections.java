package com.up;

import java.net.Socket;
import java.util.HashMap;

class Connections {
    HashMap<Connection, Socket> nodes;
    HashMap<Connection, Socket> clients_servers;
    HashMap<Connection, Socket> clients_requesters;

    public Connections() {
        this.nodes = new HashMap<Connection, Socket>();
        this.clients_servers = new HashMap<Connection, Socket>();
        this.clients_requesters = new HashMap<Connection, Socket>();
    }

    public void rmConnection(Connection con) {
        switch (con.getTipo()) {
            case ConnectionType.Node:
                System.out.println("Removing node: " + con);
                this.nodes.remove(con);
                break;
            case ConnectionType.ClientConsumer:
                System.out.println("Removing celula: " + con);
                this.clients_requesters.remove(con);
                break;
            case ConnectionType.ClientServer:
                System.out.println("Removing celula: " + con);
                this.clients_servers.remove(con);
                break;
            default:
                return;
        }
    }

    public boolean addConnection(Connection con, Socket socket) {
        switch (con.getTipo()) {
            case ConnectionType.Node:
                if (this.nodes.keySet().stream().anyMatch(x -> x.getId() == con.getId())) {
                    return false;
                }
                System.out.println("New node: " + con);
                this.nodes.put(con, socket);
                break;
            case ConnectionType.ClientConsumer:
                System.out.println("Nueva celula: " + con);
                this.clients_requesters.put(con, socket);
                break;
            case ConnectionType.ClientServer:
                System.out.println("Nueva celula: " + con);
                this.clients_servers.put(con, socket);
                break;
            default:
                System.out.println("Descartando conexión no identificada\n:    " + con);
                return false;
        }

        return true;
    }
}
