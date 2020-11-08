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

    public List<ProcessTuple> getVector() {
        return vector;
    }

    public synchronized boolean DoesFulfilDeliveryCondition(ProcessTuple currProcess)
    {
        // There exists an (pid,V) in Sm and V <= Vi
        for (ProcessTuple bufferElementMessage : this.vector)
        {
            if (bufferElementMessage.getPid() == currProcess.getPid())
            {
                return bufferElementMessage.getTimeStamp().LessThanEqualTo(currProcess.getTimeStamp());
            }
        }
        // The message contains no knowledge of what should have been received
        return true;
    }

    public synchronized void InsertList(ProcessTuple newElement)
    {
        boolean flag = false;
//        // If pid is in list update its timestamp
//        for (ProcessTuple bufferElement : this.vector)
//        {
//            if(bufferElement.getPid() == newElement.getPid())
//            {
//                bufferElement.setTimeStamp(newElement.getTimeStamp());
//                flag = true;
//                break;
//            }
//        }
        // If pid not found in list add it to the list
        if (!flag)
        {
            this.vector.add(newElement);
        }

    }

    public synchronized void Merge(VectorProcessTuple incomingBuffer) {
        if(incomingBuffer.getVector().isEmpty()) {
            return;
        }
        VectorProcessTuple resultBuffer = new VectorProcessTuple();

        for(int i = 0; i < incomingBuffer.getVector().size(); ++i) {
            boolean found = false;
            ProcessTuple incomingS = incomingBuffer.getVector().get(i);
            // Check exist
            for (ProcessTuple ownS : vector) {
                if (incomingS.getPid() == ownS.getPid()) {
                    found = true;
                    int[] maxTimeStampArray = incomingS.getTimeStamp().Max(ownS.getTimeStamp().getTimeStamp());
                    VectorClock maxTimeStamp = new VectorClock(maxTimeStampArray);
                    resultBuffer.getVector().add(new ProcessTuple(ownS.getPid(), maxTimeStamp.CloneTimeStamp()));
                }
            }
            if(!found) {
                resultBuffer.getVector().add(incomingBuffer.getVector().get(i));
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