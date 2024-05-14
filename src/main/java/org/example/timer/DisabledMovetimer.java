package org.example.timer;

public class DisabledMovetimer extends MoveTimer
{
    public DisabledMovetimer()
    {
        this.isExpired = false;
    }

    @Override
    public boolean isExpired()
    {
        return this.isExpired;
    }

    @Override
    public long getTimeLeft()
    {
        return 1;
    }

    @Override
    public void cancelTimer()
    {
        this.isExpired = true;
    }
}
