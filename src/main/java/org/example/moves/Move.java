package org.example.moves;

public class Move
{
    public int x;
    public int y;
    public int c;

    public int evaluation;
    public boolean isEvaluated = false;
    public boolean isSpecial = false;

    public Move(int x, int y, int c)
    {
        this.x = x;
        this.y = y;
        this.c = c;
    }

    public int getM(int m)
    {
        if (isSpecial)
            return m;
        return m + 1;
    }

    public void setEvaluation(int evaluation)
    {
        this.evaluation = evaluation;
        this.isEvaluated = true;
    }
}
