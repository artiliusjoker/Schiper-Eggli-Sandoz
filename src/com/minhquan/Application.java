package com.minhquan;

import java.util.Scanner;

public class Application {
    public static void main(String[] args){
//        final int processID;
//        final int defaultPort;
//        Scanner inputScanner= new Scanner(System.in);    //System.in is a standard input stream
//        System.out.print("Enter process ID :");
//        processID = inputScanner.nextInt();
//        System.out.print("Enter port :");
//        defaultPort = inputScanner.nextInt();

        // Start this process
        Process process = new Process(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        process.Start();
    }
}