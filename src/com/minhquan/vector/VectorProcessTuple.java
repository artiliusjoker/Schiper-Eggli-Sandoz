package com.minhquan.vector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import com.minhquan.Constants;
import com.minhquan.model.Message;
import com.minhquan.model.ProcessTuple;

public class VectorProcessTuple implements Serializable {
    private ArrayList<ProcessTuple> vector;

    public VectorProcessTuple(int processSize){
        vector = new ArrayList<>(processSize - 1);
    }

    public synchronized ArrayList<ProcessTuple> getVector() {
        return vector;
    }

    public static boolean DoesFulfilDeliveryCondition(Message incomingMessage, ProcessTuple currProcess)
    {
        // There exists an (pid,clock) in V_TP and clock <= local clock
        // => Deliver the message
        // else buffer it
        ArrayList<ProcessTuple>vectorBufferMessage = incomingMessage.getVtpBuffer();
        for (ProcessTuple bufferElementMessage : vectorBufferMessage)
        {
            if (bufferElementMessage.getPid() == currProcess.getPid())
            {
                return VectorClock.StrictLessThan(bufferElementMessage.getTimestamp(), currProcess.getTimestamp());
            }
        }
        // Not exists (pid,V) in Vm
        // => The message contains no knowledge of what should have been received
        // => Deliver the message
        return true;
    }

    public synchronized void InsertList(ProcessTuple newElement)
    {
        boolean flag = false;
        // If pid is in list update its timestamp
        for (ProcessTuple bufferElement : vector)
        {
            if(bufferElement.getPid() == newElement.getPid())
            {
                bufferElement.setTimeStamp(newElement.getTimestamp());
                flag = true;
                break;
            }
        }
        // If pid not found in list add it to the list
        if (!flag)
        {
            this.vector.add(newElement);
        }
    }

    public synchronized void Merge(ArrayList<ProcessTuple> incomingBuffer) {
        if(incomingBuffer.isEmpty()) {
            return;
        }
        for (ProcessTuple incomingElement : incomingBuffer) {
            boolean foundFlag = false;
            for (int i = 0; i < vector.size(); i++) {
                ProcessTuple currentElement = vector.get(i);
                if (incomingElement.getPid() == currentElement.getPid()) {
                    foundFlag = true;
                    int[] maxTimeStampArray = VectorClock.Max(incomingElement.getTimestamp(), currentElement.getTimestamp());
                    ProcessTuple newElement = new ProcessTuple(currentElement.getPid(), maxTimeStampArray);
                    //resultBuffer.getVector().add(newElement);
                    vector.set(i, newElement);
                }
            }
            if (!foundFlag) {
                vector.add(incomingElement);
            }
        }
    }

    public synchronized ArrayList<ProcessTuple> CloneVector(){
        return new ArrayList<>(vector);
    }

    public synchronized void PrintVector(){
        for (ProcessTuple processTuple : vector) {
            if(processTuple != null)
                System.out.println(processTuple.toString());
        }
        System.out.println("\n");
    }
}