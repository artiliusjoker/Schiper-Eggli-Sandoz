package com.minhquan.network;

public class Address {
    private String hostName;
    private int port;

    public Address(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void PrintAddress(){
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        return "Address{" +
                "hostName='" + hostName + '\'' +
                ", port=" + port +
                '}';
    }
}
