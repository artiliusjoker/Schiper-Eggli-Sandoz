package com.minhquan.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.minhquan.Constants;

public class ThreadPool {
    private ExecutorService pool;
    private static ThreadPool instance = null;

    private ThreadPool(){
        pool = Executors.newFixedThreadPool(Constants.MAX_POOL_SIZE);
    }
    public static ThreadPool getInstance()
    {
        if (instance == null)
            instance = new ThreadPool();
        return instance;
    }
    public void ShutDown(){
        pool.shutdown();
    }
    public synchronized void ExecuteTask(Runnable newTask){
        pool.execute(newTask);
    }
}