package org.example.logger;

import org.example.moves.CalculatedMove;

public class GameLogger extends AbstractGameLogger 
{
    private AbstractGameLogger logger;

    public GameLogger() 
    {
        enable();
    }

    public void disable() 
    {
        logger = new DisabledGameLogger();
    }

    public void enable() 
    {
        logger = new EnabledGameLogger();
    }

    @Override
    public void logMove(int x, int y, int c, int p)
    {
        logger.logMove(x, y, c, p);
    }

    @Override
    public void error(String message) 
    {
        logger.error(message);
    }

    @Override
    public void gameInfo(String message) 
    {
        logger.gameInfo(message);
    }

    @Override
    public void clientSettings(boolean abp, boolean sorting) 
    {
        logger.clientSettings(abp, sorting);
    }

    @Override
    public void gameEnd(long totalSearchTime, long totalMoveCount, char[][] finalMap, int interruptedSearches) 
    {
        logger.gameEnd(totalSearchTime, totalMoveCount, finalMap, interruptedSearches);
    }

    @Override
    public void logMap(char[][] map) 
    {
        logger.logMap(map);
    }

    @Override
    public void runtimeInfo(String message) 
    {
        logger.runtimeInfo(message);
    }


    @Override
    public void logMoveRequest(int timelimit, int[] move, long duration, int depthReach) 
    {
        logger.logMoveRequest(timelimit, move, duration, depthReach);
    }

    @Override
    public void logHelpText() 
    {
        logger.logHelpText();
    }

    @Override
    public void startMove() 
    {
        logger.startMove();
    }

    @Override
    public void logCalculatedMove(CalculatedMove calcMove) 
    {
        logger.logCalculatedMove(calcMove);
    }

    @Override
    public void endMove(long duration, int timelimit) 
    {
        logger.endMove(duration, timelimit);
    }

    @Override
    public void logIntoMove(String message) 
    {
        logger.logIntoMove(message);
    }
}
