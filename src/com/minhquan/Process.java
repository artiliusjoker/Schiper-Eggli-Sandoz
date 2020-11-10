package com.minhquan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.net.Socket;

import com.minhquan.logger.ProcessLogger;
import com.minhquan.network.*;
import com.minhquan.thread.ThreadPool;
import com.minhquan.vector.*;
import com.minhquan.model.*;

public class Process {
    // Warning: race condition -> Deadlock !!!!
    private final List<Message> messageBuffer; // Buffer for incoming messages not fulfil conditions
    private final VectorProcessTuple vector; // vector<pid, timestamps>
    private final VectorClock localClock; // vector<int>
    private int totalMessageDelivered; // number of messages delivered
    // Thread safe
    private final int pid;
    private final ArrayList<Address> addresses;
    private final Random rand;
    private final int defaultPort;
    private int messageSentCount; // only 1 thread uses it for counting messages sent by this process

    public int getTotalMessageDelivered() {
        return totalMessageDelivered;
    }

    public int getPid() {
        return pid;
    }

    private synchronized void TotalMessagesIncrement(){
        ++totalMessageDelivered;
    }

    public Process(int pid, int defaultPort){
        // Process resources

        this.pid = pid;
        this.defaultPort = defaultPort;
        // Address pool of other hosts
        this.addresses = new ArrayList<>(Constants.PROCESS_SIZE);

        // Algorithm resources

        this.localClock = new VectorClock(Constants.PROCESS_SIZE);
        // Vector <processID, clock>
        this.vector = new VectorProcessTuple(Constants.PROCESS_SIZE);
        // Buffer for waiting messages
        this.messageBuffer = new LinkedList<>();

        // Num of messages sent
        this.messageSentCount = 0;
        // Num of messages received from other hosts (delivered)
        this.totalMessageDelivered = 0;

        // Random instance
        this.rand = new Random();
    }

    public int[] getLocalClock() {
        return localClock.CloneTimeStamp();
    }

    public void Start(){
        // Get other process addresses from config file
        try {
            // Open file to read addresses of other processes
            FileInputStream fis=new FileInputStream("addresses");
            Scanner fileScanner = new Scanner(fis);

            while(fileScanner.hasNextLine())
            {
                String newLine = fileScanner.nextLine();
                // Split line by ':'
                String[] arrayOfTokens = newLine.split(":", 3);
                Address newAddress = new Address(arrayOfTokens[1], Integer.parseInt(arrayOfTokens[2]));
                addresses.add(newAddress);
            }
            fileScanner.close();
        }catch (FileNotFoundException err)
        {
            err.printStackTrace();
            return;
        }
        // Init thread pool singleton (worker threads)
        ThreadPool.getInstance();
        // Init Logger singleton
        ProcessLogger.setCurrentProcess(this);
        ProcessLogger.GetInstance();
        // Side thread: for randomize sending delay (sending commander thread)
        Thread sendingHandler = new Thread(new SendingMessageHandler());
        sendingHandler.start();

        // Main thread: start server, listen for messages (receiving commander thread)
        Server.getInstance().StartServer(defaultPort);
        // Start listening
        while (true) {
            try {
                Socket socketForMessage = Server.getInstance().getServerSocket().accept();
                // Give job to workers
                ThreadPool.getInstance().ExecuteTask(CreateReceiverTask(socketForMessage));

            } catch (IOException e) {
                System.out.println("Done ! Stopping process...");
                break;
            }
        }
        // Join the Side thread
        try {
            sendingHandler.join();
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        Server.getInstance().StopServer();
        ThreadPool.getInstance().ShutDown();
    }

    // Check buffer for buffered messages, deliver it if fulfil conditions
    private synchronized void CheckMessageBuffer(Message trigger) {
        for(int i = 0; i < messageBuffer.size(); i++) {
            Message message = messageBuffer.get(i);
            if (vector.DoesFulfilDeliveryCondition(new ProcessTuple(pid, localClock.CloneTimeStamp()))) {
                DeliverMessage(message);
                messageBuffer.remove(i);
                ProcessLogger.GetInstance().LogMessageDeliveredFromBuffer(trigger, message);
                CheckMessageBuffer(message);
                break;
            }
        }
    }
    // Deliver message from buffer
    private synchronized void DeliverMessage(Message message) {
        // Update knowledge of what should have occurred
        vector.Merge(message.getVtpBuffer());

        // Merge Vector clock
        int[] temp = localClock.CloneTimeStamp();
        int[] max = VectorClock.Max(temp, message.getTimeStamp());
        this.localClock.OverrideClock(max);

        // Increment clock for current process (an even occurs)
        this.localClock.IncrementAt(pid);

        System.out.println(message.toString());
    }

    // Receive messages
    private synchronized void Receive(Message incomingMessage)
    {
        if (vector.DoesFulfilDeliveryCondition(new ProcessTuple(pid, localClock.CloneTimeStamp()))) {
            DeliverMessage(incomingMessage);
            ProcessLogger.GetInstance().LogMessage(incomingMessage, ProcessLogger.DELIVER_MESSAGE_EVENT);
            CheckMessageBuffer(incomingMessage);
        } else {
            messageBuffer.add(incomingMessage);
            ProcessLogger.GetInstance().LogMessage(incomingMessage, ProcessLogger.BUFFER_MESSAGE_EVENT);
        }
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
        private final int pidToSend;

        public SenderTask(int message, int pidToSend) {
            this.messageContent = message;
            this.pidToSend = pidToSend;
        }

        @Override
        public void run() {
            Address address = addresses.get(pidToSend - 1);
            // Create socket to send
            Client clientToSend = new Client(address);
            // Compose new message to send
            localClock.IncrementAt(pid - 1);
            int[] temp = localClock.CloneTimeStamp();
            Message newMessage = new Message(messageContent, temp, vector.CloneVector());
            ProcessLogger.GetInstance().LogMessage(newMessage, ProcessLogger.SEND_MESSAGE_EVENT);
            // Send message
            clientToSend.SendMessage(newMessage);
            clientToSend.Close();

            // Insert vtp
            ProcessTuple newEntry = new ProcessTuple(pidToSend, temp);
            vector.InsertList(newEntry);
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
            ObjectInputStream ois = null;
            Message message;

            try {
                // Open raw input stream
                ois = new ObjectInputStream(socket.getInputStream());
                // Read message from socket stream
                Object bufferObj = ois.readObject();
                // Deserialize
                message = (Message) bufferObj;

                TotalMessagesIncrement();
                // Check condition in Receive(message)
                Receive(message);
            }
            catch (IOException | ClassNotFoundException err){
                err.printStackTrace();
            }
        }
    }

    private class SendingMessageHandler implements Runnable{

        public SendingMessageHandler(){
        }

        @Override
        public void run() {
            // Wait to run other processes
            try {
                Thread.sleep(1000 * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Randomize sending intervals
            int randomDelay = rand.nextInt(Constants.MAX_DELAY_INTERVAL) + 1;

            while (messageSentCount < Constants.TEST_MESSAGE_SIZE)
            {
                // Randomize a process to send message (except itself)
                int randomProcessID;
                do{
                    randomProcessID = rand.nextInt(2) + 1; // Process 1, process 2,..., process 15
                }while (randomProcessID == getPid());
                // Give this sending job to workers
                int messageContent = messageSentCount + 1; // Message 1 , message 2,..., message 150
                ThreadPool.getInstance().ExecuteTask(CreateSenderTask(messageContent, randomProcessID));

                // Wait to send new messages (randomized delay interval)
                try { Thread.sleep(1000 * randomDelay); }
                catch (InterruptedException e) { e.printStackTrace(); }
                ++messageSentCount;

                // Continue it's duty in while loop
            }
            // Sent enough messages, stops the listening thread commander if delivers enough messages
            while (true)
            {
                if(getTotalMessageDelivered() == Constants.TEST_MESSAGE_SIZE){
                    Server.getInstance().StopServer();
                    break;
                }
            }
        }
    }
}