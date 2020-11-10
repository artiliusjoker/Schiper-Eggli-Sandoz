package com.minhquan.logger;

import com.minhquan.Process;
import com.minhquan.model.Message;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class ProcessLogger {
    public static final int BUFFER_MESSAGE_EVENT = 1;
    public static final int DELIVER_MESSAGE_EVENT = 2;
    public static final int SEND_MESSAGE_EVENT = 3;

    private static ProcessLogger instance = null;

    private static Process currentProcess;
    private File logFile;

    public static void setCurrentProcess(Process process) {
        currentProcess = process;
    }

    private ProcessLogger(){
        try {
            logFile = new File(currentProcess.getPid() + ".log");
            if(logFile.exists())
            {
                if(!logFile.delete())
                    throw new IOException("Fatal : cannot delete old log files, close this process !!!");
            }
            if (logFile.createNewFile()) {
                System.out.println("Log created: " + logFile.getName());
            } else {
                System.out.println("Fatal : cannot create log file, close this process !!!");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ProcessLogger GetInstance(){
        if (instance == null)
        {
            instance = new ProcessLogger();
        }
        return instance;
    }

    public synchronized void LogMessage(Message message, int type){
        try {
            FileWriter fw = new FileWriter(logFile, true);
            BufferedWriter bw = new BufferedWriter(fw);

            switch (type){
                case DELIVER_MESSAGE_EVENT:{
                    bw.write("\nEVENT : DELIVER IMMEDIATELY A MESSAGE (NOT BUFFERED)\n");
                    int[]currentTime = currentProcess.getLocalClock();
                    bw.write("CURRENT CLOCK: " + Arrays.toString(currentTime));
                    bw.newLine();
                    bw.write(message.toString());
                    bw.newLine();
                    bw.write("SENDER : PROCESS : #" + message.getPidSender());
                    bw.newLine();
                    break;
                }
                case BUFFER_MESSAGE_EVENT:{
                    bw.write("\nEVENT : BUFFER A MESSAGE\n");
                    int[]currentTime = currentProcess.getLocalClock();
                    bw.write("CURRENT CLOCK: " + Arrays.toString(currentTime));
                    bw.newLine();
                    bw.write(message.toString());
                    bw.newLine();
                    break;
                }
                case SEND_MESSAGE_EVENT:{
                    bw.write("\nEVENT : SEND A MESSAGE\n");
                    int[]currentTime = currentProcess.getLocalClock();
                    bw.write("CURRENT CLOCK: " + Arrays.toString(currentTime));
                    bw.newLine();
                    bw.write(message.toString());
                    bw.newLine();
                }
            }
            bw.close();

        }
        catch (IOException err)
        {
            err.printStackTrace();
        }
    }

    public synchronized void LogMessageDeliveredFromBuffer
            (Message trigger, Message deliveredByTrigger)
    {
        try{
            FileWriter fw = new FileWriter(logFile, true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("EVENT : DELIVER A MESSAGE X TRIGGERED BY Y");
            int[]currentTime = currentProcess.getLocalClock();
            bw.write("CURRENT CLOCK: " + Arrays.toString(currentTime));
            bw.newLine();
            bw.write("MESSAGE X :" + deliveredByTrigger.toString());
            bw.newLine();
            bw.write("MESSAGE Y :" + trigger.toString());
            bw.newLine();
            bw.close();

        }
        catch (IOException err)
        {
            err.printStackTrace();
        }
    }
}
