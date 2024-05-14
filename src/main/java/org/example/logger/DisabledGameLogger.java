package org.example.logger;

import org.example.moves.CalculatedMove;

public class DisabledGameLogger extends AbstractGameLogger 
{
    @Override
    public void logMove(int x, int y, int c, int p)
    {
        // Empty implementation
    }

    @Override
    public void error(String message) 
    {
        // Empty implementation
    }

    @Override
    public void gameInfo(String message)
    {
        // Empty implementation
    }

    @Override
    public void clientSettings(boolean abp, boolean sorting) 
    {
        // Empty implementation
    }

    @Override
    public void gameEnd(long totalSearchTime, long totalMoveCount, char[][] finalMap, int interruptedSearches) 
    {
        // Empty implementation
    }

    @Override
    public void logMap(char[][] map) 
    {
        // Empty implementation
    }

    @Override
    public void runtimeInfo(String message) 
    {
        // Empty implementation
    }

    @Override
    public void logMoveRequest(int timelimit, int[] move, long duration, int depthReach) 
    {
        // Empty implementation
    }

    @Override 
    public void logHelpText()
    {
        // Empty implementation
    }

    @Override
    public void startMove() 
    {
        // Empty implementation
    }

    @Override
    public void logCalculatedMove(CalculatedMove calcMove) 
    {
        // Empty implementation
    }

    @Override
    public void endMove(long duration, int timelimit) 
    {
        // Empty implementation
    }

    @Override
    public void logIntoMove(String message) 
    {
        // Empty implementation
    }
}