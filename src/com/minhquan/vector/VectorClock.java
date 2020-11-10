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

    // t != Tpi for all elements
    public static boolean StrictDiff(int[]tMessage, int[] tPi)
    {
        for (int i = 0; i < tMessage.length; i++)
        {
            if ((tMessage[i] == tPi[i]))
            {
                return false;
            }
        }
        return true;
    }

    // t <= Tpi for all elements
    public static boolean LessThanOrEqualTo(int[]tMessage, int[] tPi)
    {
        for (int i = 0; i < tMessage.length; i++)
        {
            if ((tMessage[i] > tPi[i]))
            {
                return false;
            }
        }
        return true;
    }

    // t < Tpi for all elements
    public static boolean StrictLessThan(int[]tMessage, int[] tPi)
    {
//        System.out.println("\nSo sanh");
//        System.out.println(Arrays.toString(tMessage) + "//"  + Arrays.toString(tPi));

        //boolean strictDiff = StrictDiff(tMessage, tPi);

        return LessThanOrEqualTo(tMessage, tPi);
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