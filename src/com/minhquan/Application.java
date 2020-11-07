package com.minhquan;

import java.net.Socket;
import java.util.Random;
import com.minhquan.thread.ThreadPool;
import com.minhquan.network.*;

public class Application {
    public static void main(String[] args) {
        // Get other process addresses from config file
        Process process = new Process(1);
        process.Start();
        // Init thread pool singleton (worker threads)
        ThreadPool.getInstance();
        // Side thread: for randomize sending delay (sending commander thread)
        Thread sendingHandler = new Thread(new SendingMessageHandler(process, ThreadPool.getInstance()));
        sendingHandler.start();
        // Main thread: start server, listen for messages (receiving commander thread)
        Server.getInstance().StartServer(Constants.DEFAULT_PORT);
        Server.getInstance().StopServer();
        // Start listening
        while (true) {
            try {
                Socket socketForMessage = Server.getInstance().getServerSocket().accept();
                // Give job to workers
                ThreadPool.getInstance().ExecuteTask(process.CreateReceiverTask(socketForMessage));
                if(process.getTotalMessageDelivered() == 2)
                    break;
            } catch (Exception e) {
                System.err.println("Error in connection attempt.");
            }
        }
        // Join
        try {
            sendingHandler.join();
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        ThreadPool.getInstance().ShutDown();
    }

    private static class SendingMessageHandler implements Runnable{
        private int messageCount = 2;
        private final Random rand;
        private final Process currentProcess;
        private final ThreadPool threadPool;

        public SendingMessageHandler(Process process, ThreadPool pool){
            // create instance of Random class
            rand = new Random();
            currentProcess = process;
            threadPool = pool;
        }

        @Override
        public void run() {
            while (messageCount > 0)
            {
                // Todo : create a message and then handle it to the worker threads
                int randomProcess = rand.nextInt(2) + 1;
                // Give job to workers
                threadPool.ExecuteTask(currentProcess.CreateSenderTask(messageCount, randomProcess));

                // Randomize sending intervals
                int randomDelay = rand.nextInt(Constants.MAX_DELAY_INTERVAL);
                try { Thread.sleep(1000 * randomDelay); }
                catch (InterruptedException e) { e.printStackTrace(); }
                --messageCount;
            }
        }
    }

}