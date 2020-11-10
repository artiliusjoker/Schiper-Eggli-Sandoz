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
    private final VectorProcessTuple vectorVPofProcess; // vector<pid, timestamps>
    private final VectorClock localClock; // vector<int>
    // Thread safe
    private final int pid;
    private final ArrayList<Address> addresses;
    private final Random rand;
    private final int defaultPort;
    private int messageSentCount; // only 1 thread uses it for counting messages sent by this process

    public int getPid() {
        return pid;
    }

    public synchronized VectorProcessTuple getVectorVPofProcess() {
        return vectorVPofProcess;
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
        this.vectorVPofProcess = new VectorProcessTuple(Constants.PROCESS_SIZE);
        // Buffer for waiting messages
        this.messageBuffer = new LinkedList<>();

        // Num of messages sent
        this.messageSentCount = 0;

        // Random instance
        this.rand = new Random();
    }

    public synchronized int getMessageSentCount() {
        return messageSentCount;
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
                System.out.println("Receiving done ! Stopping listening thread !!");
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
            ProcessTuple currentTuple = new ProcessTuple(pid, localClock.CloneTimeStamp());
            if (VectorProcessTuple.DoesFulfilDeliveryCondition(message, currentTuple)) {
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
        vectorVPofProcess.Merge(message.getVtpBuffer());

        // Merge Vector clock
        int[] temp = localClock.CloneTimeStamp();
        int[] max = VectorClock.Max(temp, message.getTimeStamp());
        localClock.OverrideClock(max);

        // Increment clock for current process (an even occurs)
        localClock.IncrementAt(pid - 1);

        if(getMessageSentCount() == (Constants.TEST_MESSAGE_SIZE - 1) )
        {
            if(messageBuffer.isEmpty())
            {
                Server.getInstance().StopServer();
            }
        }
    }

    // Receive messages
    private synchronized void Receive(Message incomingMessage)
    {
        ProcessTuple currentTuple = new ProcessTuple(pid, localClock.CloneTimeStamp());
        if (VectorProcessTuple.DoesFulfilDeliveryCondition(incomingMessage, currentTuple)) {
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
        private final int messageOrder;
        private final int pidToSend;

        public SenderTask(int messageOrder, int pidToSend) {
            this.messageOrder = messageOrder;
            this.pidToSend = pidToSend;
        }

        @Override
        public void run() {
            Address address = addresses.get(pidToSend - 1);
            // Create socket to send
            Client clientToSend = new Client(address);
            localClock.IncrementAt(pid - 1);

            // Compose new message to send
            int[] temp = localClock.CloneTimeStamp(); // Clock message
            String messageContent = Integer.toString(messageOrder); // Content
            ArrayList<ProcessTuple>messageVP = vectorVPofProcess.CloneVector();

            Message newMessage = new Message(messageContent, temp, messageVP, pid);
            ProcessLogger.GetInstance().LogMessage(newMessage, ProcessLogger.SEND_MESSAGE_EVENT);
            // Send message
            clientToSend.SendMessage(newMessage);
            clientToSend.Close();

            // Insert vtp (or override)
            ProcessTuple newEntry = new ProcessTuple(pidToSend, temp);
            vectorVPofProcess.InsertList(newEntry);
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
            ObjectInputStream ois;
            Message message;

            try {
                // Open raw input stream
                ois = new ObjectInputStream(socket.getInputStream());
                // Read message from socket stream
                Object bufferObj = ois.readObject();
                // Deserialize
                message = (Message) bufferObj;

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

            // Randomize sending intervals
            int randomDelay = rand.nextInt(Constants.MAX_DELAY_INTERVAL) + 1;

            while (messageSentCount < Constants.TEST_MESSAGE_SIZE)
            {
                // Randomize a process to send message (except itself)
                int randomProcessID;
                do{
                    randomProcessID = rand.nextInt(Constants.PROCESS_SIZE) + 1; // Process 1, process 2,..., process 15
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
        }
    }
}