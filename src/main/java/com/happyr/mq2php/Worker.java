package com.happyr.mq2php;

import com.happyr.mq2php.queue.IQueueClient;

/**
 * @author Tobias Nyholm
 */
public class Worker implements Runnable {

    private IQueueClient mq;
    private String queueName;

    public Worker(String queueName, IQueueClient mq) {
        this.queueName = queueName;
        this.mq = mq;
    }

    public void run() {
        mq.receive();
    }

    public String getQueueName() {
        return queueName;
    }
}
