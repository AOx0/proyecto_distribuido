package com.up;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;

class Connections {
    HashSet<Connection> nodes;
    HashSet<Connection> clients_requesters;
    HashSet<Connection> clients_solvers;

    public Connections() {
        this.nodes = new HashSet<Connection>();
        this.clients_requesters = new HashSet<Connection>();
        this.clients_solvers = new HashSet<Connection>();
    }

    public void send_to_nodes(Message ms) {
        this.nodes.stream().forEach(con -> {
        System.out.println("RE SEND: " + ms);
            try {
                Messenger.send(con.socket, ms);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void send_to_clients_requesters(Message ms) {
        this.clients_requesters.stream().forEach(con -> {
            System.out.println("TO REQ: " + ms);
            if (ms.has_from() && ms.has_dest() && ms.dest.compareTo(con.id) == 0) {
                System.out.println("YE REQ: " + ms);

                try {
                    Messenger.send(con.socket, ms);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void send_to_clients_solvers(Message ms) {
        System.out.println("TO SOLV: " + ms);
        if (!ms.has_dest() && ms.has_from()) {
            System.out.println("YE SOLV: " + ms);
            this.clients_solvers.stream().forEach(con -> {
                try {
                    Messenger.send(con.socket, ms);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void rmConnection(Connection con) {
        switch (con.getTipo()) {
            case Connection.ConnectionType.Node:
                System.out.println("Removing node: " + con);
                this.nodes.remove(con);
                break;
            case Connection.ConnectionType.ClientSolver:
                System.out.println("Removing celula: " + con);
                this.clients_solvers.remove(con);
                break;
            case Connection.ConnectionType.ClientRequester:
                System.out.println("Removing celula: " + con);
                this.clients_requesters.remove(con);
                break;
            default:
                return;
        }
    }

    public boolean addConnection(Connection con, Socket socket) {
        switch (con.getTipo()) {
            case Connection.ConnectionType.Node:
                System.out.println("New node: " + con);
                this.nodes.add(con);
                break;
            case Connection.ConnectionType.ClientSolver:
                System.out.println("Nueva celula: " + con);
                this.clients_solvers.add(con);
                break;
            case Connection.ConnectionType.ClientRequester:
                System.out.println("Nueva celula: " + con);
                this.clients_requesters.add(con);
                break;
            default:
                System.out.println("Descartando conexión no identificada\n:    " + con);
                return false;
        }

        return true;
    }
}
