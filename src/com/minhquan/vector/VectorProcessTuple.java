package com.minhquan.vector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.minhquan.Constants;
import com.minhquan.model.ProcessTuple;

public class VectorProcessTuple implements Serializable {
    private List<ProcessTuple> vector;

    public VectorProcessTuple(){
        vector = new ArrayList<>();
    }

    public VectorProcessTuple(int processSize){
        vector = new ArrayList<>(processSize - 1);
    }

    public synchronized List<ProcessTuple> getVector() {
        return vector;
    }

    public synchronized boolean DoesFulfilDeliveryCondition(ProcessTuple currProcess)
    {
        // There exists an (pid,clock) in V_TP and clock <= local clock
        // => Deliver the message
        // else buffer it
        for (ProcessTuple bufferElementMessage : this.vector)
        {
            if (bufferElementMessage.getPid() == currProcess.getPid())
            {
                return VectorClock.LessThanEqualTo(bufferElementMessage.getTimestamp(), currProcess.getTimestamp());
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
        for (ProcessTuple bufferElement : this.vector)
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

        VectorProcessTuple resultBuffer = new VectorProcessTuple(Constants.PROCESS_SIZE);
        for (ProcessTuple incomingElement : incomingBuffer) {
            boolean foundFlag = false;
            // Check exist
            for (ProcessTuple currentElement : vector) {
                if (incomingElement.getPid() == currentElement.getPid()) {
                    foundFlag = true;
                    int[] maxTimeStampArray = VectorClock.Max(incomingElement.getTimestamp(), currentElement.getTimestamp());
                    VectorClock maxTimeStamp = new VectorClock(maxTimeStampArray);
                    resultBuffer.getVector().add(new ProcessTuple(currentElement.getPid(), maxTimeStamp.CloneTimeStamp()));
                }
            }
            if (!foundFlag) {
                resultBuffer.getVector().add(incomingElement);
            }
        }
        this.vector = resultBuffer.getVector();
    }

    public synchronized ArrayList<ProcessTuple> CloneVector(){
        ArrayList<ProcessTuple>result = new ArrayList<>(Constants.PROCESS_SIZE);
        this.vector.forEach(element ->{
            ProcessTuple clone = new ProcessTuple(element.getPid(), element.getTimestamp());
            result.add(clone);
        });
        return result;
    }

    public void PrintVector(){
        for (ProcessTuple processTuple : vector) {
            if(processTuple != null)
                System.out.println(processTuple.toString());
        }
    }
}