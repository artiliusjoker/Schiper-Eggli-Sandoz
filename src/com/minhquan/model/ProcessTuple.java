package com.minhquan.model;

import com.minhquan.vector.VectorClock;

import java.io.Serializable;

public class ProcessTuple implements Serializable {
    private int pid;
    private VectorClock timestamp;

    public ProcessTuple(int pid, VectorClock timestamp){
        this.pid = pid;
        this.timestamp = timestamp;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public VectorClock getTimeStamp() {
        return timestamp;
    }

    public void setTimeStamp(VectorClock timestamp) {
        this.timestamp = timestamp;
    }
}