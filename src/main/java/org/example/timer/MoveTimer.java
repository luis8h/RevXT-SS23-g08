package org.example.timer;

public abstract class MoveTimer 
{
    protected boolean isExpired;

    public abstract boolean isExpired(); 

    public abstract long getTimeLeft();

    public abstract void cancelTimer();
}
