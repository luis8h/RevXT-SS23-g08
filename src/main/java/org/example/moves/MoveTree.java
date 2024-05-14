package org.example.moves;

import org.example.GameInfo;
import org.example.GameState;
import org.example.Settings;
import org.example.Evaluation.EvaluationDynamic;
import org.example.timer.MoveTimer;
import org.example.timer.TimerExpiredException;

public class MoveTree
{
    private final int MOVESORT_DEPTH = 4;

    private final GameInfo gameInfo;
    private final EvaluationDynamic evDynamic;
    private final Settings settings;

    public int noMoves = 0;
    public int stateCount, leafCount, validMovesCount;

    private int currentDepthLimit;

    private final int rootPlayer;

    private int[][][] killerArray;

    public MoveTree(GameInfo gameInfo, EvaluationDynamic evDynamic, Settings settings, int rootPlayer)
    {
        this.gameInfo   = gameInfo;
        this.evDynamic  = evDynamic;
        this.settings   = settings;
        this.rootPlayer = rootPlayer;
    }

    public CalculatedMove calculateBestMove(GameState currentState, int depthlimit, MoveTimer timer, boolean brsp) throws TimerExpiredException
    {
        long startTime = System.currentTimeMillis();

        stateCount = 0; leafCount = 0; validMovesCount = 0;
        this.currentDepthLimit = depthlimit;

        killerArray = new int[depthlimit][gameInfo.mapWidth][gameInfo.mapHeight];

        int alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        int newValue;
        Move bestMove = currentState.getFirstMove(rootPlayer);
        MoveList moves;
        GameState newState;

        moves = currentState.getAllValidMoves(rootPlayer, evDynamic, false, timer, settings.sorting);

        if (moves.size() == 0)
            moves = currentState.getAllValidMoves(rootPlayer, evDynamic, true, timer, settings.sorting);

        moves.sort(evDynamic, currentState, rootPlayer, true, timer);

        for(Move move: moves) {
            if (timer.isExpired())
                throw new TimerExpiredException();

            newState = new GameState(currentState, gameInfo);
            newState.makeMove(move.x, move.y, move.c, rootPlayer);

            newValue = nextDepth(newState, nextPlayer(rootPlayer), depthlimit, timer, 1, alpha, beta, brsp);

            if (newValue >= max) {
                max = newValue;
                bestMove = move;
            }

            alpha = Integer.max(alpha, max);
        }

        return new CalculatedMove(bestMove, max, System.currentTimeMillis() - startTime, depthlimit, stateCount, leafCount, validMovesCount);
    }

    private int nextDepth(GameState currentState, int player, int depthlimit, MoveTimer timer, int m, int alpha, int beta, boolean brsp) throws TimerExpiredException
    {
        if (timer.isExpired())
            throw new TimerExpiredException();

        // check if depthlimit is reached
        if (depthlimit <= 0) {
            leafCount ++;
            return evDynamic.evaluateForPlayer((char) (rootPlayer + '0'), currentState, timer);
        }

        MoveList moves = currentState.getAllValidMoves(player, evDynamic, false, timer, settings.sorting);

        if (moves.size() == 0)
            moves = currentState.getAllValidMoves(player, evDynamic, true, timer, settings.sorting);

        validMovesCount += moves.size();
        stateCount ++;

        if (moves.size() == 0)
            return skipPlayer(currentState, player, depthlimit, timer, m, alpha, beta, brsp);

        noMoves = 0;

        // TODO: if size == 1 do return without evaluation in sort method
        if (currentDepthLimit - depthlimit < MOVESORT_DEPTH)
            moves.sort(evDynamic, currentState, player, isMaximizer(player), timer);
        else
            moves.sortKiller(killerArray, currentDepthLimit-depthlimit, settings.killer, timer);

        if (brsp)
            return brsP(currentState, player, depthlimit, timer, m, alpha, beta, moves);
        else
            return getNextValue(moves, currentState, player, depthlimit, alpha, beta, timer, false, 0);
    }

    private int brsP(GameState currentState, int player, int depthlimit, MoveTimer timer, int m, int alpha, int beta, MoveList moves) throws TimerExpiredException
    {
        if (isMaximizer(player))
            m = 0;
        else if (m == 2) {
            moves.get(0).isSpecial = true;
            moves.clearNoSpecial();
        }
        else if (nextPlayer(player) != rootPlayer)
            moves.get(0).isSpecial = true;

        return getNextValue(moves, currentState, player, depthlimit, alpha, beta, timer, true, m);
    }

    private int getNextValue(MoveList moves, GameState currentState, int player, int depthlimit, int alpha, int beta, MoveTimer timer, boolean brsp, int m) throws TimerExpiredException
    {
        if (timer.isExpired())
            throw new TimerExpiredException();

        int bestValue = (isMaximizer(player)) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int newValue;
        GameState nextState;

        for (Move move : moves)
        {
            if (timer.isExpired())
                throw new TimerExpiredException();

            nextState = new GameState(currentState, gameInfo);
            nextState.makeMove(move.x, move.y, move.c, player);

            newValue = nextDepth(nextState, nextPlayer(player), depthlimit-1, timer, move.getM(m), alpha, beta, brsp);

            bestValue = (isMaximizer(player)) ? Integer.max(bestValue, newValue) : Integer.min(bestValue, newValue);

            if (isMaximizer(player))
                alpha = Integer.max(alpha, bestValue);
            else
                beta = Integer.min(beta, bestValue);

            if (newValue >= beta && isMaximizer(player) && settings.abp) {
                killerArray[currentDepthLimit - depthlimit][move.x][move.y] ++;
                break;
            }
            if (newValue <= alpha && !isMaximizer(player) && settings.abp) {
                killerArray[currentDepthLimit - depthlimit][move.x][move.y] ++;
                break;
            }
        }

        return bestValue;
    }

    private int skipPlayer(GameState currentState, int player, int depthlimit, MoveTimer timer, int m, int alpha, int beta, boolean brsp) throws TimerExpiredException
    {
            noMoves++;
            if (noMoves >= gameInfo.playerCount) {
                if (evDynamic.gameWon(currentState))
                    return Integer.MAX_VALUE;
                else
                    return Integer.MIN_VALUE;
            }
            return nextDepth(currentState, nextPlayer(player), depthlimit, timer, m, alpha, beta, brsp);
    }

    private int nextPlayer(int player)
    {
        if (player >= gameInfo.playerCount)
            return 1;
        else
            return (player + 1);
    }

    private boolean isMaximizer(int player)
    {
        return player == rootPlayer;
    }
}
