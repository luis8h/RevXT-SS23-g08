package org.example.Evaluation;

import org.example.GameInfo;
import org.example.GameState;
import org.example.timer.MoveTimer;
import org.example.timer.TimerExpiredException;

import java.util.ArrayList;

public class EvaluationDynamic
{
    public final int MAPSUM_WEIGHT = 1;
    public final int MOBILITY_WEIGHT = 2;
    public final int STONECOUNT_WEIGHT = 8;
    public final int OVERWRITE_INDEX_WEIGHT = 1;

    public final int VALIDMOVES_WEIGHT = 1;
    public final int VALIDDIRECTIONS_WEIGHT = 1;

    private final int INDEX_MAX = 100000;

    public final GameInfo gameInfo;
    public final EvaluationStatic evStatic;

    public EvaluationDynamic(GameInfo gameInfo, EvaluationStatic evStatic)
    {
        this.gameInfo = gameInfo;
        this.evStatic = evStatic;
    }

    public int evaluateForPlayer(char player, GameState state, MoveTimer timer) throws TimerExpiredException
    {
        // int overwriteIndex = state.overwriteCountPlayers[Character.getNumericValue(player) - 1];

        var indexes = new ArrayList<int[]>();
        indexes.add(new int[]{getStoneCount(player, state), STONECOUNT_WEIGHT, 0, INDEX_MAX});
        indexes.add(new int[]{getMapSum(player, state), MAPSUM_WEIGHT, 0, INDEX_MAX});
        indexes.add(new int[]{getMobilityIndex(player, state, timer), MOBILITY_WEIGHT, 0, INDEX_MAX});

        return sumIndex(indexes);
    }

    public int getMapSum(char player, GameState state)
    {
        int mapSum = 0;

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++)
            if (state.map[x][y] == player) mapSum += evStatic.evaluationMap[x][y];

        return normalizeIntValue(mapSum, 0, EvaluationStatic.INDEX_MAX * gameInfo.mapHeight * gameInfo.mapWidth);
    }

    public int getMobilityIndex(char player, GameState state, MoveTimer timer) throws TimerExpiredException
    {
        boolean hasOverride = (state.overwriteCountPlayers[Character.getNumericValue(player) - 1] > 0);
        int[] sums = state.getValidMovesAndDirectionsCount(player, hasOverride, timer);

        int validMovesCount = normalizeIntValue(sums[0], 0, state.gameInfo.mapHeight * state.gameInfo.mapWidth);
        int validDirectionsCount = normalizeIntValue(sums[1], 0, state.gameInfo.mapHeight * state.gameInfo.mapWidth * 8);

        return normalizeIntValue(validMovesCount * VALIDMOVES_WEIGHT + validDirectionsCount * VALIDDIRECTIONS_WEIGHT, 0, INDEX_MAX * 2);
    }

    public boolean gameWon(GameState finalState)
    {
        int currentMax = 0;
        int stoneCountEnemy;
        int ownStoneCount = getStoneCount((char) (gameInfo.playerNumber + '0'), finalState);
        for (int i = 1; i <= gameInfo.playerCount; i++) {
            if (gameInfo.playerNumber == i)
                continue;
            stoneCountEnemy = getStoneCount((char) (i + '0'), finalState);
            currentMax = Integer.max(currentMax, stoneCountEnemy);
        }
        return ownStoneCount >= currentMax;
    }


    // mobility

    public int getValidMovesCount(char player, boolean overwrite, GameState state, MoveTimer timer) throws TimerExpiredException
    {
        int sum = 0;
        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            if (timer.isExpired())
                throw new TimerExpiredException();

            if (state.isValidMove(Character.getNumericValue(player), x, y, overwrite))
                sum++;
        }
        return sum;
    }

    public int getValidDirectionsCount(char player, boolean overwrite, GameState state, MoveTimer timer) throws TimerExpiredException
    {
        int sum = 0;

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            if (timer.isExpired())
                throw new TimerExpiredException();

            for (int k = 0; k < 8; k++)
                if (state.isValidDirection(k, x, y, Character.getNumericValue(player)))
                    sum++;
        }

        return sum;
    }

    // absolute stone count
    public int getStoneCount(char player, GameState state)
    {
        int sum = 0;

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++)
            if (state.map[x][y] == player)
                sum++;

        return normalizeIntValue(sum, 0, gameInfo.mapHeight * gameInfo.mapWidth);
    }

    private int normalizeIntValue(int value, double min, double max)
    {
        return (int) (INDEX_MAX * ((double) value - min) / (max - min) + min);
    }

    private int sumIndex(ArrayList<int[]> indexes)
    {
        return sumIndex(indexes, 0, INDEX_MAX);
    }

    private int sumIndex(ArrayList<int[]> indexes, int min, int max)
    {
        int sum = 0, sumMin = 0, sumMax = 0;

        for (int[] index : indexes) {
            sum += index[0] * index[1];
            sumMin += index[2];
            sumMax += index[3] * index[1];
        }

        return normalizeIntValue(sum, sumMin, sumMax);
    }
}
