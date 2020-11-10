package com.minhquan.vector;

import java.io.Serializable;
import java.util.Arrays;

public class VectorClock implements Serializable {
    private int[] timeStamp;

    public VectorClock(int numOfProcesses){
        timeStamp = new int[numOfProcesses];
        Arrays.fill(timeStamp, 0);
    }

    public VectorClock(int[] array)
    {
        this.timeStamp = array;
    }

    public synchronized int[] CloneTimeStamp(){
        return Arrays.copyOf(this.timeStamp, this.timeStamp.length);
    }

    public synchronized void IncrementAt(int index) {
        timeStamp[index] = timeStamp[index] + 1;
    }

    public synchronized void OverrideClock(int[] timeStamp){
        this.timeStamp = timeStamp;
    }

    public static boolean LessThanEqualTo(int[]vector1, int[] vector2)
    {
        for (int i = 0; i < vector1.length; i++)
        {
            if (!(vector1[i] <= vector2[i]))
            {
                return false;
            }
        }
        return true;
    }

    public static int[] Max(int[]timeStamp1, int[] timeStamp2)
    {
        for (int i = 0; i < timeStamp1.length; i++)
        {
            if (timeStamp1[i] < timeStamp2[i])
                timeStamp1[i] = timeStamp2[i];
        }
        return timeStamp1;
    }

    public String toString()
    {
        return Arrays.toString(this.timeStamp);
    }
}