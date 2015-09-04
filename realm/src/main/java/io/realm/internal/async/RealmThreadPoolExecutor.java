package io.realm.internal.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Nabil on 04/09/15.
 */
public class RealmThreadPoolExecutor extends ThreadPoolExecutor {
    // reduce context switch by using a number of thread proportionate to the number of cores
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 1;

    private boolean isPaused;
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();

    private static volatile RealmThreadPoolExecutor instance;

    public static RealmThreadPoolExecutor getInstance() {
           if (instance == null) {
               synchronized (RealmThreadPoolExecutor.class) {
                   if (instance == null) {
                       instance = new RealmThreadPoolExecutor();
                   }
               }
           }
        return instance;
    }

//    private AtomicInteger numberOfProcessed
    private RealmThreadPoolExecutor() {
        super(CORE_POOL_SIZE, CORE_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS, //terminated idle thread
                new ArrayBlockingQueue<Runnable>(100));
    }

//    private RealmThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
//        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
////        setThreadFactory(new ThreadFactory() {
////            @Override
////            public Thread newThread(Runnable r) {
////                Thread thread = new Thread(r);
//////                android.os.Process.setThreadPriority()
////
////                thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
////                return null;
////            }
////        });
//    }

//        public RealmThreadPoolExecutor(...) { super(...);


    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(new BgPriorityRunnable(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(new BgPriorityCallable<T>(task));
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) unpaused.await();
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
    }

//    @Override
//    protected void afterExecute(Runnable r, Throwable t) {
//        super.afterExecute(r, t);
//        // clean up the Queue from the background thread?
//        //purge();
//    }

    public void pause() {
//        ((PausableArrayBlockingQueue<Runnable>) getQueue()).pause();
        pauseLock.lock();
        try {
            isPaused = true;
        } finally {
            pauseLock.unlock();
        }
    }

    public void resume() {
//        ((PausableArrayBlockingQueue<Runnable>) getQueue()).resume();
        pauseLock.lock();
        try {
            isPaused = false;
            unpaused.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }


}