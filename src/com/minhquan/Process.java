package com.minhquan;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import com.minhquan.network.Client;
import com.minhquan.vector.*;
import com.minhquan.model.*;


public class Process {
    // Warning: race condition -> Deadlock !!!!
    private final List<Message> messageBuffer;
    private final VectorProcessTuple vector;
    private final VectorClock localClock;
    private int totalMessageDelivered;
    // Thread safe
    private final int pid;

    public int getTotalMessageDelivered() {
        return totalMessageDelivered;
    }

    private synchronized void TotalMessagesIncrement(){
        ++totalMessageDelivered;
    }

    public Process(int pid){
        this.pid = pid;
        localClock = new VectorClock(Constants.PROCESS_SIZE);
        vector = new VectorProcessTuple(Constants.PROCESS_SIZE);
        messageBuffer = new LinkedList<>();
        totalMessageDelivered = 0;
    }

    public void Start(){

    }

    // Check buffer for buffered messages, deliver it if fulfil conditions
    private synchronized void CheckMessageBuffer() {
        for(int i = 0; i < messageBuffer.size(); i++) {
            Message message = messageBuffer.get(i);
            if (vector.DoesFulfilDeliveryCondition(new ProcessTuple(pid, localClock))) {
                DeliverMessage(message);
                messageBuffer.remove(i);
                CheckMessageBuffer();
                break;
            }
        }
    }
    // Deliver message from buffer
    private synchronized void DeliverMessage(Message message) {
        // Update knowledge of what should have occurred
        vector.Merge(message.getVtpBuffer());
        //println("Delivering - " + m.toString());

        // Merge Vector Clocks
        int[] max = localClock.Max(message.getTimeStamp());
        this.localClock.OverrideClock(max);

        // Increment clock for current process
        //this.timeStamp[pid]++;
        this.localClock.IncrementAt(pid);
    }

    public SenderTask CreateSenderTask(int message, int pidToSend){
        return new SenderTask(message, pidToSend);
    }

    public ReceiverTask CreateReceiverTask(Socket receivedSocket){
        return new ReceiverTask(receivedSocket);
    }

    // Sending task
    private class SenderTask implements Runnable {
        private final int messageContent;
        private int pidToSend;

        public SenderTask(int message, int pidToSend) {
            messageContent = message;
            this.pidToSend = pidToSend;
        }

        @Override
        public void run() {
            String desIP = "localhost";
            int desPort = 34568;
            // Create socket to send
            Client clientToSend = new Client(desIP, desPort);
            // Compose new message to send
            Message newMessage = new Message(messageContent, vector, localClock.getTimeStamp());
            clientToSend.SendMessage(newMessage);
        }
    }

    // Receiving task
    private class ReceiverTask implements Runnable {
        private final Socket socket;

        public ReceiverTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            // Todo: get message from socket -> check condition:
            // False : buffer the message
            // True : receive, update variables
            TotalMessagesIncrement();
        }
    }
}