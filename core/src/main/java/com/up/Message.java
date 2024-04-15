package com.up;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

/**
 * A message structure is as follows (in bytes):
 *  0 1 2     4 5             C D             14 <- (20th byte)   
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | t |  len  |     from      |      dest     |   `len` bytes of payload ...                
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |- `t`: Type of Message ( Identificate | Request | Response )
 */
public class Message {
    short tipo;
    byte msg[];
    UUID from;
    UUID dest;

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

    public static final class RequestType {
        public static final byte Add = 1;
        public static final byte Sub = 2;
        public static final byte Div = 3;
        public static final byte Times = 4;

        public static final String toString(byte type) {
            return switch (type) {
                case RequestType.Add -> "Add";
                case RequestType.Sub -> "Sub";
                case RequestType.Div -> "Div";
                case RequestType.Times -> "Times";
                default -> "Err";
            };
        }
    }
    
    

    public static UUID get_default_uuid() {
        return UuidCreator.fromBytes(default_uuid);
    }
    
    static byte default_uuid[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    public int len() {
        return msg.length;
    }

    public boolean has_from() {
        return this.from.compareTo(Message.get_default_uuid()) != 0;
    }

    public boolean has_dest() {
        return this.dest.compareTo(Message.get_default_uuid()) != 0;
    }

    public Message(short tipo, byte msg[], UUID dest, UUID from) {
        this.tipo = tipo;
        this.msg = msg;

        // IMB: Aqui ambos eran from
        this.dest = dest;
        this.from = from;
    }

    public Message(short tipo, byte msg[]) {
        this.tipo = tipo;
        this.msg = msg;
        this.from = Message.get_default_uuid();
        this.dest = Message.get_default_uuid();
    }

    public void setOrigin(UUID from) {
        this.from = from;
    }
    
    public UUID getOrigin() {
        return this.from;
    }

    public void setDestiny(UUID dest) {
        this.dest = dest;
    }

    @Override
    public String toString() {
        String res = "Message { tipo: ";
        res += MessageType.toString(this.tipo);

        res += ", from: " + from;
        res += ", dest: " + dest;
        
        switch (this.tipo) {
            case MessageType.Identificate:
                res += ", connection: ";
                res += Connection.ConnectionType.toString(this.msg[0]);
                break;
            case MessageType.Request:
                res += ", op: " + RequestType.toString(this.msg[0]);
                res += ", lhs: " + ByteBuffer.wrap(Arrays.copyOfRange(msg, 1, 9)).getDouble();
                res += ", rhs: " + ByteBuffer.wrap(Arrays.copyOfRange(msg, 0xa, 0x12)).getDouble();
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
