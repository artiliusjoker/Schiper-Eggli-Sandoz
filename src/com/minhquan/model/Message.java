package com.minhquan.model;

import java.util.List;
import java.io.Serializable;
import java.util.Arrays;

import com.minhquan.vector.*;

public class Message implements Serializable{
    private String message;
    private VectorProcessTuple vtpBuffer;
    private int[] timeStamp;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public VectorProcessTuple getVtpBuffer() {
        return vtpBuffer;
    }

    public void setVtpBuffer(VectorProcessTuple vtpBuffer) {
        this.vtpBuffer = vtpBuffer;
    }

    public int[] getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int[] timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", timeStamp=" + Arrays.toString(timeStamp) +
                '}';
    }
}