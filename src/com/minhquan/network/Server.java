package com.minhquan.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class Server {
    private ServerSocket serverSocket = null;
    private static Server serverInstance = null;

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    private Server(){
        // Empty
    }

    public void StartServer(int port){
        if(serverSocket == null){
            // Try port
            int tryPort = port;
            while (true){
                try {
                    serverSocket = new ServerSocket(tryPort);
                    break;
                }
                catch (SocketException err){
                    tryPort++;
                }
                catch (IOException err){
                    System.err.println("Please reboot server !");
                    return;
                }
            }
        }
    }

    public void StopServer() {
        if (serverSocket != null)
        {
            try {
                serverSocket.close();
            }catch (IOException err){
                err.printStackTrace();
            }
        }
    }

    public static Server getInstance(){
        if (serverInstance == null)
        {
            serverInstance = new Server();
        }
        return serverInstance;
    }
}