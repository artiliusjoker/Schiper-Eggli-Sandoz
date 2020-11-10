package com.minhquan.model;

import java.io.Serializable;
import java.util.Arrays;

public class ProcessTuple implements Serializable {
    private final int pid;
    private int[] timeStamp;

    public ProcessTuple(int pid, int[]timeStamp){
        this.pid = pid;
        this.timeStamp = timeStamp;
    }

    public void setTimeStamp(int[] timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getPid() {
        return pid;
    }

    public int[] getTimestamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "\tTuple: {" +
                "pid=" + pid +
                ", timestamp=" + Arrays.toString(timeStamp) +
                '}';
    }
}