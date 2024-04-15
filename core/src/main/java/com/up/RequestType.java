package com.up;

public class RequestType {
    public static final byte Add = 1;
    public static final byte Sub = 2;
    public static final byte Div = 3;
    public static final byte Times = 4;

    public String toString(byte type) {
        return switch (type) {
            case RequestType.Add -> "Add";
            case RequestType.Sub -> "Sub";
            case RequestType.Div -> "Div";
            case RequestType.Times -> "Times";
            default -> "Err";
        };
    }
}
