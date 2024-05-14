package org.example.logger;

import org.example.moves.CalculatedMove;

public abstract class AbstractGameLogger 
{
    public abstract void logMove(int x, int y, int c, int p);

    public abstract void error(String message);

    public abstract void gameInfo(String message);

    public abstract void clientSettings(boolean abp, boolean sorting);

    public abstract void gameEnd(long totalSearchTime, long totalMoveCount, char[][] finalMap, int interruptedSearches);

    public abstract void logMap(char[][] map);

    public abstract void runtimeInfo(String message);

    public abstract void logMoveRequest(int timelimit, int[] move, long duration, int depthReach);

    public abstract void logHelpText();

    public abstract void startMove();

    public abstract void logCalculatedMove(CalculatedMove calcMove);

    public abstract void endMove(long duration, int timelimit);

    public abstract void logIntoMove(String message);
}