package org.example.moves;

import org.example.Evaluation.EvaluationDynamic;
import org.example.GameState;
import org.example.timer.MoveTimer;
import org.example.timer.TimerExpiredException;

import java.util.ArrayList;

public class MoveList extends ArrayList<Move>
{
    private final boolean sorting;

    public MoveList(boolean sorting)
    {
        super();
        this.sorting = sorting;
    }

    public void sort(EvaluationDynamic evDynamic, GameState state, int player, boolean desc, MoveTimer timer) throws TimerExpiredException
    {
        if (!sorting)
            return;

        GameState nextState;
        var sortedMoves = new MoveList(true);
        int evaluationValue;

        for (Move move : this) {
            if (timer.isExpired())
                throw new TimerExpiredException();

            nextState = new GameState(state, state.gameInfo);
            nextState.makeMove(move.x, move.y, move.c, player);

            evaluationValue = evDynamic.evaluateForPlayer((char) (player + '0'), nextState, timer);

            sortedMoves.insertSorted(move, evaluationValue, desc);
        }

        this.clear();
        this.addAll(sortedMoves);
    }

    public void insertSorted(Move move, int evaluationValue, boolean desc)
    {
        move.setEvaluation(evaluationValue);

        for (int i = 0; i < size(); i++) {
            if (!desc && get(i).evaluation < evaluationValue)
                continue;

            if (desc && get(i).evaluation > evaluationValue)
                continue;

            add(i, move);
            return;
        }

        add(move);
    }

    public void clearNoSpecial()
    {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).isSpecial)
                continue;
            this.remove(i);
        }
    }

    public void insertSortedKiller(Move move, int[][][] killerArray, int depth)
    {
        int killerCount = killerArray[depth][move.x][move.y];

        for (int i = 0; i < size(); i++) {
            if (killerArray[depth][this.get(i).x][this.get(i).y] > killerCount)
                continue;

            add(i, move);
            return;
        }

        add(move);
    }

    public void sortKiller(int[][][] killerArray, int depth, boolean killerSorting, MoveTimer timer) throws TimerExpiredException
    {
        if (!killerSorting)
            return;

        var sortedMoves = new MoveList(sorting);

        for (Move move : this) {
            if (timer.isExpired())
                throw new TimerExpiredException();

            sortedMoves.insertSortedKiller(move, killerArray, depth);
        }

        this.clear();
        this.addAll(sortedMoves);
    }
}
