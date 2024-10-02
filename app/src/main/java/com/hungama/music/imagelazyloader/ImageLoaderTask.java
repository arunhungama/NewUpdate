package com.hungama.sdk.imagelazyloader;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class ImageLoaderTask
{
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final BlockingQueue<Runnable> FILE_IO_TASK_QUEUE = new LinkedBlockingQueue<Runnable>();
    private static final BlockingQueue<Runnable> DOWNLOAD_TASK_QUEUE = new LinkedBlockingQueue<Runnable>();

    static ExecutorService FILE_IO_EXECUTOR = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT, FILE_IO_TASK_QUEUE, new BackgroundThreadFactory());


    static ExecutorService DOWNLOAD_EXECUTOR = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT, DOWNLOAD_TASK_QUEUE, new BackgroundThreadFactory());


    private static class BackgroundThreadFactory implements ThreadFactory
    {
        private static final int sTag = 1;

        @Override
        public Thread newThread(Runnable runnable)
        {
            Thread thread = new Thread(runnable);
            thread.setName("ImageLoaderThread" + sTag);
            //thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            // A exception handler is created to log the exception from threads
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
            {
                @Override
                public void uncaughtException(Thread thread, Throwable ex)
                {
                    Log.e("IES", thread.getName() + " encountered an error: " + ex.getMessage());
                }
            });
            return thread;
        }
    }
}
