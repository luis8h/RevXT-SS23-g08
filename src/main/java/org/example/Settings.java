package org.example;

public class Settings
{
    public static final byte GROUP_NUMBER = 8;
    public static final int[][] DIRECTIONS = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};

    public static final int TIMER_BUFFER = 199;

    public boolean abp, sorting, brsp, killer;

    public Settings(boolean abp, boolean sorting, boolean brsp, boolean killer)
    {
        this.abp     = abp;
        this.sorting = sorting;
        this.brsp    = brsp;
        this.killer  = killer;
    }
}
