package com.up;

/**
 * `MessageType.Identificate = 1`
 * `MessageType.Request = 2`
 * `MessageType.Response = 3`
 */
public class MessageType {
    static final short Identificate = 1;
    static final short Request = 2;
    static final short Response = 3;

    public static final String toString(short value) {
        switch (value) {
            case MessageType.Identificate:
                return "Identificate";
            case MessageType.Request:
                return "Request";
            case MessageType.Response:
                return "Response";
            default:
                return "ERR";
        }
    }
}
