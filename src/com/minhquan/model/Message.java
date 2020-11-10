package com.minhquan.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

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
        return "MESSAGE_CONTENT : {" +
                "message='" + message + '\'' +
                ", timestamp=" + Arrays.toString(timeStamp) +
                ", vector_tp=" + vectorBuffer.size() +
                '}';
    }
}