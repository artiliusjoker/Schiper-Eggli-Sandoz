package com.minhquan.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.minhquan.model.Message;

public class Client {
    Socket socketForSending = null;

    public Client(Address address) {
        try {
            socketForSending = new Socket(address.getHostName(), address.getPort());
        } catch (Exception e) {
            e.printStackTrace();
            socketForSending = null;
        }
    }

    public Socket getSocketForSending() {
        return socketForSending;
    }

    public void Close(){
        try {
            if (socketForSending != null)
            {
                socketForSending.close();
            }
        }catch (IOException e)
        {
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