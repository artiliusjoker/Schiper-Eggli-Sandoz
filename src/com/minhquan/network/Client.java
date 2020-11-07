package com.minhquan.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.minhquan.model.Message;

public class Client {
    Socket socketForSending = null;

    public Client(String destIP, int destPort) {
        try {
            socketForSending = new Socket(destIP, destPort);
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            e.printStackTrace();
        }
    }

    public void SendMessage(Message message){
        if(socketForSending != null){
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socketForSending.getOutputStream());
                oos.writeObject(message);
            }
            catch (IOException err){
                err.printStackTrace();
            }
        }
    }
}