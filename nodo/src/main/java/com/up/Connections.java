package com.up;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
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
            try {
                Messenger.send(con.socket, ms);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void send_to_clients_requesters(Message ms) {
        if (ms.dest == Message.MessageTarget.Client) {
            this.clients_requesters.stream().forEach(con -> {
                ByteBuffer wrapped = ByteBuffer.wrap(ms.event_id);
                int id = wrapped.getInt(0);
                int pkg = wrapped.getInt(4);

                if (con.id == id && con.setPkg(pkg) == pkg) {
                    try {
                        Messenger.send(con.socket, ms);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void send_to_clients_solvers(Message ms) {
        if (ms.tipo == Message.MessageTarget.Server) {
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
        String tipo = Connection.ConnectionType.toString(con.getTipo());
        System.out.println("Removing " + tipo + ": " + con);

        switch (con.getTipo()) {
            case Connection.ConnectionType.Node:
                this.nodes.remove(con);
                break;
            case Connection.ConnectionType.ClientSolver:
                this.clients_solvers.remove(con);
                break;
            case Connection.ConnectionType.ClientRequester:
                this.clients_requesters.remove(con);
                break;
            default:
                return;
        }
    }

    public boolean addConnection(Connection con, Socket socket) {
        switch (con.getTipo()) {
            case Connection.ConnectionType.Node:
                this.nodes.add(con);
                break;
            case Connection.ConnectionType.ClientSolver:
                this.clients_solvers.add(con);
                break;
            case Connection.ConnectionType.ClientRequester:
                this.clients_requesters.add(con);
                break;
            default:
                System.out.println("Descartando conexión no identificada\n:    " + con);
                return false;
        }

        String tipo = Connection.ConnectionType.toString(con.getTipo());
        System.out.println("New " + tipo + ": " + con);
        return true;
    }
}
