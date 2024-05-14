package org.example.timer;

import java.util.Timer;
import java.util.TimerTask;

public class EnabledMoveTimer extends MoveTimer
{
    private final Timer timer;
    private final long startTime;
    private final long durationInMillis;
    private boolean isExpired;

    public EnabledMoveTimer(long durationInMillis)
    {
        this.durationInMillis = durationInMillis;
        timer = new Timer();
        startTime = System.currentTimeMillis();

        if (durationInMillis < 0)
            durationInMillis = 0;

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                isExpired = true;
                timer.cancel();
            }
        }, durationInMillis);
    }

    public boolean isExpired()
    {
        return isExpired;
    }

    public long getTimeLeft()
    {
        if (isExpired)
            return 0;
        else {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long timeLeft = durationInMillis - elapsedTime;
            return Math.max(timeLeft, 0);
        }
    }

    public void cancelTimer()
    {
        if (!isExpired) {
            timer.cancel();
            isExpired = true;
        }
    }
}
