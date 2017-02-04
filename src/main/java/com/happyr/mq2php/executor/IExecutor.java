package com.happyr.mq2php.executor;

import com.happyr.mq2php.message.Message;

/**
 * @author Tobias Nyholm
 */
public interface IExecutor {

    /**
     * Execute the message payload.
     *
     * @throws com.happyr.mq2php.exception.MessageExecutionFailedException
     */
    void execute(Message message);
}
