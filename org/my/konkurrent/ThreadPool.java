package org.my.konkurrent;

import org.my.konkurrent.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class implements a thread pool using a shared
 * bounded blocking queue. The pool contains fixed number of
 * worker threads. Producers add runnable tasks to the
 * queue. The worker threads pick up task from queue
 * and execute them.
 *
 * A task can take long to execute  depending on what
 * business logic they encapsulate. This can cause a
 * fixed number of worker threads to sometimes fail to
 * keep up with items added to the queue. A bounded queue
 * helps by limiting the number of items in the thread
 * pool, so that the queue does not bloat. Instead,
 * the producer will block and wait until space gets
 * cleared up in the queue.
 *
 * Currently, java.util.concurrent.Executors do not have a
 * factory method for 'a pool with a shared bounded queue'.
 * An ideal implementation will be to build it using
 * java.util.concurrent  - ExecutorService and BlockingQueue.
 *
 */
public class ThreadPool {
    private Queue<Runnable> qStore = new LinkedList<Runnable>();
    private final int qsize;

    private Lock qLock;

    // Condition used to simulate a bounded queue
    private Condition qLimitCondition;

    // Condition used to signal item addition and removal
    private Condition itemAddedCondition;

    // Flag used to gracefully stop the worker threads
    private volatile boolean done = false;

    private ArrayList<WorkerThread> wList = new ArrayList<WorkerThread>();

    /**
     * Creates a pool of qsize with threadCount worker threads
     *
     * @param qsize Size of queue
     * @param threadCount Number of worker threads
     * @param fairness Specify 'true' if you desire a fair
     *                  ordering for the worker threads
     */
    public ThreadPool(int qsize, int threadCount, boolean fairness){
        this.qsize = qsize;

        qLock = new ReentrantLock(fairness);
        qLimitCondition = qLock.newCondition();
        itemAddedCondition = qLock.newCondition();

        for(int i=0; i<threadCount; i++) {
            WorkerThread t = new WorkerThread("MyWorker-" + i);
            wList.add(t);
            t.start();
        }
    }

    /**
     * Gracefully bring down the thread pool.
     */
    public void stopThreadPool() {

        if(done)
            return;

        done = true;

        for(WorkerThread wt : wList) {
            wt.interrupt();
        }
    }

    /**
     * Add a runnable to the pool.
     *
     * A better implementation would be for this method to
     * return a failure immediately when the queue is full
     * so that clients will not block. I wrote it this way
     * just to illustrate the interrupt() methods.
     *
     *
     * @param task
     * @return true if the task was successfully submitted.
     * @throws InterruptedException
     */
    public boolean addTask(Runnable task) throws InterruptedException {

        Utils.LOG("Adding task");

        boolean ret = false;

        try {
            qLock.lockInterruptibly();

            // If Q is full, block
            while(qStore.size() > qsize) {
                qLimitCondition.await();
            }

            boolean wasEmpty = qStore.isEmpty();

            ret = qStore.offer(task);

            // Q was empty, but now there
            // is one element in it, unblock
            // one worker.
            if(wasEmpty) {
                qLimitCondition.signal();
            }

            itemAddedCondition.signal();

        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();

            Utils.LOG("addTask() interrupted.");

            throw new InterruptedException();
        } finally {
            qLock.unlock();
        }

        return ret;
    }

    /**
     * Remove a task from the queue
     *
     * @return a runnable task
     * @throws InterruptedException
     */
    private Runnable removeTask() throws InterruptedException {

        Utils.LOG("Removing task");

        Runnable ret = null;

        try {
            qLock.lockInterruptibly();

            while(qStore.isEmpty()) {
                qLimitCondition.await();
            }

            boolean wasFull = qStore.size() == qsize;

            ret = qStore.remove();

            if(wasFull)
                qLimitCondition.signal();

        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();

            Utils.LOG("removeTask() interrupted.");

            throw new InterruptedException();
        } finally {
            qLock.unlock();
        }

        return ret;
    }

    /**
     * Method in package scope for JUnit tests
     * to access the internal queue for testing.
     *
     * Method is not available to clients.
     *
     * @return the internal queue
     */
    Queue<Runnable> getInternalQueue() {
        return qStore;
    }

    /***
     * The worker thread.
     *
     */
     class WorkerThread extends  Thread {
         private String name;
         public WorkerThread(String name) {
            this.setName(name);
            this.name = name;
         }


         public void run() {
             Utils.LOG(name + " beginning.");

             while(!done) {
                 Runnable task = null;
                 try {
                     task = removeTask();
                 } catch (InterruptedException e) {

                     // If removeTask() was interrupted
                     // then continue and check if 'done'.
                     continue;
                 }

                 // Check if the current thread is interrupted
                 // before running the task.
                 if(Thread.currentThread().isInterrupted()) {
                    continue;
                 }

                 if(!done) {
                     // Finally run the task
                     task.run();
                 }
             }

             Utils.LOG(name + " terminated.");
         }

     }

}
