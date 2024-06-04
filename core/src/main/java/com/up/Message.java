package com.up;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

/**
 * A message structure is as follows (in bytes):
 *  0 1 2     4 5             C D               
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | t |  len  |   event_id    |              dest             | `len` bytes of payload ...                
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |- `t`: Type of Message ( Identificate | Request | Response )
 */
public class Message {
    short tipo;
    short dest;
    byte event_id[];
    byte msg[];

    public static final class MessageType {
        static final short Identificate = 1;
        static final short Request = 2;
        static final short Response = 3;

        public static final String toString(short value) {
            return switch (value) {
                case MessageType.Identificate -> "Identificate";
                case MessageType.Request -> "Request";
                case MessageType.Response -> "Response";
                default -> "ERR";
            };
        }
    }

    public static final class MessageTarget {
        static final short Node = 1;
        static final short Server = 2;
        static final short Client = 3;

        public static final String toString(short value) {
            return switch (value) {
                case MessageTarget.Node -> "Node";
                case MessageTarget.Server -> "Server";
                case MessageTarget.Client -> "Client";
                default -> "ERR";
            };
        }
    }

    public static final class RequestType {
        public static final byte Add = 1;
        public static final byte Sub = 2;
        public static final byte Div = 3;
        public static final byte Mul = 4;
        public static final byte Err = 5;

        public static final String toString(byte type) {
            return switch (type) {
                case RequestType.Add -> "Add";
                case RequestType.Sub -> "Sub";
                case RequestType.Div -> "Div";
                case RequestType.Mul -> "Mul";
                default -> "Err";
            };
        }

        public static final byte fromString(String op) {
            return switch (op) {
                case "Add" -> RequestType.Add;
                case "Sub" -> RequestType.Sub;
                case "Div" -> RequestType.Div;
                case "Mul" -> RequestType.Mul;
                default -> RequestType.Err;
            };
        }
    }
    
    

    public static UUID get_default_uuid() {
        return UuidCreator.fromBytes(default_uuid);
    }
    
    static byte default_uuid[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    public int msg_len() {
        return this.msg.length;
    }

    public int evt_len() {
        return this.event_id.length;
    }

    public Message(short tipo, byte id[], byte msg[], short dest) {
        this.tipo = tipo;
        this.msg = msg;
        this.event_id = id;
        this.dest = dest;
    }

    public Message(short tipo, byte msg[], short dest) {
        this.tipo = tipo;
        this.msg = msg;
        this.event_id = new byte[8];
        this.dest = dest;
    }

    public Message setID(byte id[]) {
        this.event_id = id;
        return this;
    }

    @Override
    public String toString() {
        String res = "Message { tipo: ";
        res += MessageType.toString(this.tipo);

        res += ", dest: " + MessageTarget.toString(this.dest);
        res += ", id: " + Connection.displayID(event_id);
        
        switch (this.tipo) {
            case MessageType.Identificate:
                res += ", connection: ";
                res += Connection.ConnectionType.toString(this.msg[0]);
                break;
            case MessageType.Request:
                res += ", op: " + RequestType.toString(this.msg[0]);
                res += ", lhs: " + ByteBuffer.wrap(msg, 0x1, 0x8).getDouble();
                res += ", rhs: " + ByteBuffer.wrap(msg, 0x9, 0x8).getDouble();
                break;
            case MessageType.Response:
                res += ", res: " + ByteBuffer.wrap(msg).getDouble();
                break;
            default:
                res += ", msg: [ ";
                for (byte b : this.msg) {
                    res += String.format("%x ", Byte.toUnsignedInt(b));
                }
                res += "]";
        }

        res += " }";
        return res;
    }
}
