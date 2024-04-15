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
        return switch (value) {
            case MessageType.Identificate -> "Identificate";
            case MessageType.Request -> "Request";
            case MessageType.Response -> "Response";
            default -> "ERR";
        };
    }
}
