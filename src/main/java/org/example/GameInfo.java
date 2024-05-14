package org.example;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GameInfo
{
    public int playerNumber;
    public int playerCount;
    public int overwriteCount;
    public int bombCount;
    public int bombRadius;
    public int mapWidth;
    public int mapHeight;
    public int[][][][] transitions;
    public char[][] initialMap;

    public GameInfo(int playerCount, int overwriteCount, int bombCount, int bombRadius, int mapWidth, int mapHeight, int[][][][] transitions, char[][] initialMap)
    {
        this.playerNumber = 1;
        this.playerCount = playerCount;
        this.overwriteCount = overwriteCount;
        this.bombCount = bombCount;
        this.bombRadius = bombRadius;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.transitions = transitions;
        this.initialMap = initialMap;
    }

    public GameInfo(byte[] data)
    {
        int i;
        var s = new Scanner(new ByteArrayInputStream(data));

        playerCount    = s.nextInt();
        overwriteCount = s.nextInt();
        bombCount      = s.nextInt();
        bombRadius     = s.nextInt();
        mapHeight      = s.nextInt();
        mapWidth       = s.nextInt();

        initialMap = new char[mapWidth][mapHeight];
        transitions = new int[mapWidth][mapHeight][][];

        s.nextLine();
        for (int y = 0; y < mapHeight; y++) {
            byte[] line = s.nextLine().replaceAll("\\s", "").getBytes(StandardCharsets.US_ASCII);
            for (int x = 0; x < mapWidth; x++)
                initialMap[x][y] = (char) line[x];
        }

        while (s.hasNextInt()) {
            var tSrc = new int[3];
            var tDst = new int[3];

            for (i = 0; i < 3; i++)
                tSrc[i] = s.nextInt();
            s.next();
            for (i = 0; i < 3; i++)
                tDst[i] = s.nextInt();


            if (transitions[tSrc[0]][tSrc[1]] == null)
                transitions[tSrc[0]][tSrc[1]] = new int[8][];
            if (transitions[tDst[0]][tDst[1]] == null)
                transitions[tDst[0]][tDst[1]] = new int[8][];
            if (transitions[tSrc[0]][tSrc[1]][tSrc[2]] == null)
                transitions[tSrc[0]][tSrc[1]][tSrc[2]] = new int[3];
            if (transitions[tDst[0]][tDst[1]][tDst[2]] == null)
                transitions[tDst[0]][tDst[1]][tDst[2]] = new int[3];

            for (i = 0; i < 3; i++)
                transitions[tSrc[0]][tSrc[1]][tSrc[2]][i] = tDst[i];
            for (i = 0; i < 3; i++)
                transitions[tDst[0]][tDst[1]][tDst[2]][i] = tSrc[i];
        }
    }

    @Override
    public String toString()
    {
        return  "Spieler: "              + playerCount +
                "\nUeberschreibsteine: " + overwriteCount +
                "\nBomben/Staerke: "     + bombCount + "/" + bombRadius +
                "\nSpielfeldhoehe: "     + mapHeight +
                "\nSpielfeldbreite: "    + mapWidth;
    }

    public boolean isPlayer(char c)
    {
        return c >= '1' && c <= (char) (playerCount + '0');
    }

    public boolean isInMap(int x, int y)
    {
        return x >= 0 && y >= 0 && x < mapWidth && y < mapHeight && initialMap[x][y] != '-';
    }

}
