package com.happyr.mq2php.queue;

/**
 * A interface for message queue clients
 *
 * @author Tobias Nyholm
 */
public interface IQueueClient {

    /**
     * Start receiving messages.
     */
    void receive();

    /**
     * Shutdown, close connection
     */
    void shutdown();
}
