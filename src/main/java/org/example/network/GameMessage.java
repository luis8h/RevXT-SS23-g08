package org.example.network;

public record GameMessage(int type, byte[] data)
{
    public GameMessage
    {
        if (type < 1 || type > 9)
            throw new IllegalArgumentException("Invalid message type");
    }
}
