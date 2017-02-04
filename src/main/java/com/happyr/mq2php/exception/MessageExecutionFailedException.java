package com.happyr.mq2php.exception;

/**
 * An exception that is thrown by the executors if something unexpected happened.
 */
public class MessageExecutionFailedException extends RuntimeException {

    private Boolean requeue = false;

    public MessageExecutionFailedException(String message, Boolean requeue) {
        super(message);

        this.requeue = requeue;
    }

    public Boolean requeue() {
        return requeue;
    }
}
