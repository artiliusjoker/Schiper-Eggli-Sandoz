package com.minhquan.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Message implements Serializable{
    private final String messageContent;
    private final ArrayList<ProcessTuple>vectorBuffer;
    private final int[] timeStamp;
    private final int pidSender;

    public Message(String messageContent, int[] timeStamp, ArrayList<ProcessTuple> currentBuffer, int pidSender) {
        this.messageContent = "Message " + messageContent;
        this.timeStamp = timeStamp;
        this.vectorBuffer = currentBuffer;
        this.pidSender = pidSender;
    }

    public int getPidSender() {
        return pidSender;
    }

    public ArrayList<ProcessTuple> getVtpBuffer() {
        return vectorBuffer;
    }

    public int[] getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "MESSAGE CONTENT : {" +
                "message='" + messageContent + '\'' +
                ", timestamp=" + Arrays.toString(timeStamp) +
                ", vector_tp=" + vectorBuffer.size() +
                '}';
    }
}