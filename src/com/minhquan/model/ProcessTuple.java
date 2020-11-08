package com.minhquan.model;

import com.minhquan.Constants;
import com.minhquan.vector.VectorClock;

import java.io.Serializable;
import java.util.Arrays;

public class ProcessTuple implements Serializable {
    private int pid;
    private VectorClock timestamp;
    private final int[] timeStamp;

    public ProcessTuple(int pid, int[]timeStamp){
        this.pid = pid;
        this.timeStamp = timeStamp;
    }

    public int getPid() {
        return pid;
    }

    public VectorClock getTimeStamp() {
        return timestamp;
    }

    public int[] getTimestamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "ProcessTuple{" +
                "pid=" + pid +
                ", timestamp=" + Arrays.toString(timeStamp) +
                '}';
    }
}