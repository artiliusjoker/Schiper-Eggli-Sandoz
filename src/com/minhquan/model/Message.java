package com.minhquan.model;

import java.io.Serializable;
import java.util.Arrays;

import com.minhquan.vector.*;

public class Message implements Serializable{
    private final String message;
    private VectorProcessTuple vtpBuffer;
    private int[] timeStamp;

    public Message(int message, VectorProcessTuple vtpBuffer, int[] timeStamp) {
        this.message = "Message " + message;
        this.vtpBuffer = vtpBuffer;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
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