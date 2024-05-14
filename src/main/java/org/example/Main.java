package org.example;

import org.example.logger.GameLogger;
import org.example.network.ServerConnection;

import java.io.IOException;
import java.net.Socket;

public class Main
{
    public static final GameLogger logger = new GameLogger();

    public static void main(String[] args)
    {
        String serverIP   = "127.0.0.1";
        int    serverPort =  7777;
        boolean abp = true, sorting = true, brsp = true, killer = true;

        for (int i = 0; i < args.length; i++)
            if (args[i].length() == 2 && args[i].charAt(0) == '-') {
                char key = args[i].charAt(1);
                String value = "";

                if ((args.length > i + 1) && (args[i + 1].charAt(0) != '-'))
                    value = args[++i];

                switch (key) {
                    case 'i' -> serverIP = value;
                    case 'p' -> serverPort = Integer.parseInt(value);
                    case 'a' -> abp = false;
                    case 'n' -> sorting = false;
                    case 'b' -> brsp = false;
                    case 'k' -> killer = false;
                    case 'q' -> logger.disable();
                    case 'h' -> {
                        logger.logHelpText();
                        System.exit(0);
                    }
                }
            }

        Client game;
        try (var clientSocket = new Socket(serverIP, serverPort)) {
            game = new Client(new ServerConnection(clientSocket), new Settings(abp, sorting, brsp, killer));
            game.run();
        } catch (IOException e) {
            logger.error("Server not found");
            System.exit(1);
        }
    }
}
