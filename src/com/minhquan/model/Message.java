package com.minhquan.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import com.minhquan.Constants;
import com.minhquan.vector.*;

public class Message implements Serializable{
    private final String message;
    private final ArrayList<ProcessTuple>vectorBuffer;
    private final int[] timeStamp;

    public Message(int message, int[] timeStamp, ArrayList<ProcessTuple> currentBuffer) {
        this.message = "Message " + message;
        this.timeStamp = timeStamp;
        vectorBuffer = currentBuffer;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<ProcessTuple> getVtpBuffer() {
        return vectorBuffer;
    }

    public int[] getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", timeStamp=" + Arrays.toString(timeStamp) +
                ", vector=" + vectorBuffer.size() +
                '}';
    }
}