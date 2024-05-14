package org.example.Evaluation;

import org.example.GameInfo;
import org.example.Settings;
import org.example.timer.MoveTimer;
import org.example.timer.TimerExpiredException;

public class EvaluationStatic
{
    public int[][] evaluationMap;

    public static final int INDEX_MAX = 1000;

    public static final int TAKEABLE_WEIGHT = 8;
    public static final int INFLUENCE_WEIGHT = 1;
    public static final int SENSIBLE_FIELD_WEIGHT = 2;

    private final GameInfo gameInfo;
    
    private MoveTimer timer;

    public EvaluationStatic(GameInfo gameInfo)
    {
        this.gameInfo = gameInfo;
    }

    public void runEvaluation(MoveTimer timer) throws TimerExpiredException
    {
        this.timer = timer;
        evaluateMap();
    }

    public boolean isEvaluated()
    {
        return evaluationMap != null;
    }

    public void evaluateMap() throws TimerExpiredException
    {
        evaluationMap = new int[gameInfo.mapWidth][gameInfo.mapHeight];
        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            if (timer.isExpired()) {
                evaluationMap = null;
                throw new TimerExpiredException();
            }

            if (!gameInfo.isInMap(x, y))
                continue;
            evaluationMap[x][y] = evaluateField(x, y);
        }
    }

    public int evaluateField(int x, int y)
    {
        if (!gameInfo.isInMap(x, y))
            return 0;
        int takeableIndex = getTakeableIndex(x, y);
        int influenceIndex = getInfluenceIndex(x, y);
        // int revealsSensibleFieldIndex = getRevealsSensibleFieldIndex(x, y);

        return normalizeIntValue(takeableIndex * TAKEABLE_WEIGHT + influenceIndex * INFLUENCE_WEIGHT, 0, INDEX_MAX * 2);
    }

    public boolean[] getPossibleDirections(int x, int y)
    {
        boolean[] possibleDirections = new boolean[8];
        for (int k = 0; k < 8; k++)
            possibleDirections[k] = isDirectionPossible(k, x, y);
        return possibleDirections;
    }

    public boolean isDirectionPossible(int dir, int x, int y)
    {
        int newX = x + Settings.DIRECTIONS[dir][1], newY = y + Settings.DIRECTIONS[dir][0];
        if (!gameInfo.isInMap(newX, newY))
            return false;
        char nextField = gameInfo.initialMap[newX][newY];
        if (nextField != '-')
            return true;
        return gameInfo.transitions[x][y] != null && gameInfo.transitions[x][y][dir] != null;
    }

    public int getTakeableIndex(int x, int y)
    {
        boolean[] possibleDirections = getPossibleDirections(x, y);
        int mirroredDirections = 0;
        int takeableIndex;

        for (int k = 0; k < 4; k++)
            if (possibleDirections[k] && possibleDirections[(k + 4) % 8]) mirroredDirections++;

        switch (mirroredDirections) {
            case 0 -> takeableIndex = 40;
            case 1 -> takeableIndex = 16;
            case 2 -> takeableIndex = 8;
            case 3 -> takeableIndex = 4;
            case 4 -> takeableIndex = 0;
            default -> takeableIndex = 0;
        }

        return normalizeIntValue(takeableIndex, 0, 40);
    }

    public int getInfluenceIndex(int x, int y)
    {
        // Todo: also consider length of possible Directions to get a more accurate value

        boolean[] possibleDirections = getPossibleDirections(x, y);
        int possibleDirectionCount = 0;
        for (int k = 0; k < 8; k++)
            if (possibleDirections[k]) possibleDirectionCount++;

        return normalizeIntValue(possibleDirectionCount, 0, 8);
    }

    // public int getRevealsSensibleFieldIndex(int x, int y) 
    // {
    //     int newX, newY; 
    //     int sumIndex = 0;
    //     for (int k = 0; k < 8; k++) 
    //     {
    //         newX = x + GameSession.DIRECTIONS[k][1];
    //         newY = y + GameSession.DIRECTIONS[k][0];
    //         if (!gameInfo.isInMap(newX, newY)) continue;
    //         sumIndex += getTakeableIndex(newX, newY);
    //     }
    //     return sumIndex;
    // }

    public void printEvaluationMap()
    {
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            for (int x = 0; x < gameInfo.mapWidth; x++) {
                if (gameInfo.initialMap[x][y] == '-') {
                    System.out.print("---- ");
                    continue;
                }
                System.out.printf("%04d ", evaluationMap[x][y]);
            }
            System.out.println();
        }
    }

    private int normalizeIntValue(int value, float min, float max)
    {
        return (int) (INDEX_MAX * ((float) value - min) / (max - min) + min);
    }
}