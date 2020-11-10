package com.minhquan.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.minhquan.Constants;

public class ThreadPool {
    private final ExecutorService pool;
    private static ThreadPool instance = null;

    private ThreadPool(){
        pool = Executors.newFixedThreadPool(Constants.MAX_POOL_SIZE);
        //pool = Executors.newSingleThreadExecutor();
    }
    public static ThreadPool getInstance()
    {
        if (instance == null)
            instance = new ThreadPool();
        return instance;
    }

    public void ShutDown(){
        try {
            pool.shutdown();
            pool.awaitTermination(Constants.MAX_DELAY_INTERVAL, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("Tasks interrupted");
        }
        finally {
            if (!pool.isTerminated()) {
                System.err.println("Cancel non-finished tasks");
            }
            pool.shutdownNow();
            System.out.println("SES algorithm finished !!!");
        }
    }
    
    public synchronized void ExecuteTask(Runnable newTask){
        pool.execute(newTask);
    }
}