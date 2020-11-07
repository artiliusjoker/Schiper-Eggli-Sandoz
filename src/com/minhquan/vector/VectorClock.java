package com.minhquan.vector;

import java.util.Arrays;

public class VectorClock {
    private int[] timeStamp;

    public VectorClock(int numOfProcesses){
        timeStamp = new int[numOfProcesses];
        Arrays.fill(timeStamp, 0);
    }

    public VectorClock(int[] array)
    {
        this.timeStamp = array;
    }

    public int[] getTimeStamp() {
        return timeStamp;
    }

    public synchronized void IncrementAt(int index) {
        ++timeStamp[index];
    }

    public synchronized void UpdateClock(VectorClock vectorToCheck){
        for (int i = 0; i < timeStamp.length; ++i) {
            if (vectorToCheck.getTimeStamp()[i] > timeStamp[i]) {
                timeStamp[i] = vectorToCheck.getTimeStamp()[i];
            }
        }
    }

    public void OverrideClock(int[] timeStamp){
        this.timeStamp = timeStamp;
    }

    public boolean StrictlyLessThan(VectorClock vectorToCompare)
    {
        for (int i = 0; i < timeStamp.length; i++)
        {
            if (!(timeStamp[i] < vectorToCompare.getTimeStamp()[i]))
            {
                return false;
            }
        }
        return true;
    }

    public boolean LessThanEqualTo(VectorClock vectorToCompare)
    {
        for (int i = 0; i < timeStamp.length; i++)
        {
            if (!(timeStamp[i] <= vectorToCompare.getTimeStamp()[i]))
            {
                return false;
            }
        }
        return true;
    }

    public int[] Max(int[] timeStamp2)
    {
        int[] max_array = timeStamp;
        for (int i = 0; i < timeStamp.length; i++)
        {
            if (timeStamp[i] < timeStamp2[i])
                max_array[i] = timeStamp2[i];
        }
        return max_array;
    }

    public String toString()
    {
        return Arrays.toString(this.timeStamp);
    }
}