package org.example.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerConnection
{
    private final DataInputStream  input;
    private final DataOutputStream output;

    public ServerConnection(Socket s) throws IOException
    {
        this.input  = new DataInputStream (new BufferedInputStream (s.getInputStream ()));
        this.output = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

    public void send(GameMessage message)
    {
        try {
            output.writeByte(message.type());
            output.writeInt (message.data().length);
            output.write    (message.data());
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GameMessage receive()
    {
        int type, length;
        byte[] data;
        try {
            type   = input.readByte();
            length = input.readInt();
            data   = new byte[length];
            input.readFully(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new GameMessage(type, data);
    }
}
