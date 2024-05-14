package org.example.moves;

public class CalculatedMove 
{
    public final double PRUNING_RATE = 0.6;

    public Move move;
    public int evaluationValue;
    public long duration;
    public int depthReach;
    public int innerCount;
    public int leafCount;
    public int validMovesCount;
    public int totalStateCount;
    public float timePerState;
    public int statesInNextMove;
    public float estimatedTimeNextMove;

    public CalculatedMove(Move move, int evaluationValue, long duration, int depthReach, int innerCount, int leafCount, int validMovesCount)
    {
        this.move = move;
        this.evaluationValue = evaluationValue;
        this.duration = duration;
        this.depthReach = depthReach;
        this.innerCount = innerCount;
        this.leafCount = leafCount;
        this.validMovesCount = validMovesCount;
        this.totalStateCount = leafCount + innerCount;
        this.timePerState = (float) duration / (float) totalStateCount;
        this.statesInNextMove = (int) (((validMovesCount / innerCount) * leafCount + innerCount + leafCount) * PRUNING_RATE);
        this.estimatedTimeNextMove = (float) statesInNextMove * timePerState;
    }
}
