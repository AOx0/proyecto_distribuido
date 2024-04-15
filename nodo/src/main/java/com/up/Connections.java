package com.up;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

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
            try {
                Messenger.send(con.socket, ms);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean send_to_clients_requesters(Message ms) {
        AtomicBoolean sent = new AtomicBoolean(false);
        this.clients_requesters.stream().forEach(con -> {
            if (ms.dest == con) {
                sent.set(true);
                try {
                    Messenger.send(con.socket, ms);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return sent.get();
    }

    public void send_to_clients_solvers(Message ms) {
        if (ms.dest == null) {
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
            case ConnectionType.Node:
                System.out.println("Removing node: " + con);
                this.nodes.remove(con);
                break;
            case ConnectionType.ClientSolver:
                System.out.println("Removing celula: " + con);
                this.clients_solvers.remove(con);
                break;
            case ConnectionType.ClientRequester:
                System.out.println("Removing celula: " + con);
                this.clients_requesters.remove(con);
                break;
            default:
                return;
        }
    }

    public boolean addConnection(Connection con, Socket socket) {
        switch (con.getTipo()) {
            case ConnectionType.Node:
                if (this.nodes.stream().anyMatch(x -> x.getId() == con.getId())) {
                    return false;
                }
                System.out.println("New node: " + con);
                this.nodes.add(con);
                break;
            case ConnectionType.ClientSolver:
                System.out.println("Nueva celula: " + con);
                this.clients_solvers.add(con);
                break;
            case ConnectionType.ClientRequester:
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
