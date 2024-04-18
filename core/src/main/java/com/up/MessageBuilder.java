package com.up;

import java.nio.ByteBuffer;


/**
 * Payload byte constructor for sending ussing the Message protocol
 */
public class MessageBuilder {
    /**
     * An identification message payload structure has the form (in bytes):
     *  0 <- (1st byte)
     * +-+
     * |t| 
     * +-+
     *  |- `t`: Connection type (Server | ClientSolver | ClientRequester)
     */
    public static final Message Identificate(byte tipo) {
        ByteBuffer b = ByteBuffer.allocate(1);
        b.put(tipo);
        return new Message(Message.MessageType.Identificate, b.array());
    }

    /**
     * The payload of a request message has the form (in bytes):
     *  0 1             8 9             10 <- (17th byte)
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |t|      lhs      |         rhs   |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *  |- `t`: Operation type (Add | Sub | Mul | Div)
     */
    public static final Message Request(byte type, double lhs, double rhs) {
        ByteBuffer b = ByteBuffer.allocate(1 + (8 * 2));
        b.put(type);
        b.putDouble(lhs);
        b.putDouble(rhs);
        return new Message(Message.MessageType.Request, b.array());
    }


    /**
     * A response payload message has the form (in bytes):
     *  0               8  <- (8th byte)
     * +-+-+-+-+-+-+-+-+-+
     * |     result      |
     * +-+-+-+-+-+-+-+-+-+
     */
    public static final Message Restultado(Message req, double result) {
        ByteBuffer b = ByteBuffer.allocate(8);
        b.putDouble(result);
        Message msg = new Message(Message.MessageType.Response, b.array());
        msg.from = req.from;
        return msg;
    }

    public static final double GetLhs(Message req) {
        return ByteBuffer.wrap(req.msg, 1, 8).getDouble();
    }

    public static final double GetRhs(Message req) {
        return ByteBuffer.wrap(req.msg, 9, 8).getDouble();
    }
}
