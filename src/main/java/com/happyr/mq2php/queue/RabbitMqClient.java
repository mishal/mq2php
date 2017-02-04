package com.happyr.mq2php.queue;

import com.happyr.mq2php.executor.IExecutor;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * A Rabbit MQ client
 *
 * @author Tobias Nyholm
 */
public class RabbitMqClient implements IQueueClient {

    protected Connection connection;
    protected ConnectionFactory connectionFactory;
    protected Channel channel;
    protected RabbitMqConsumer consumer;
    protected String queueName;
    protected String errorQueueName;
    protected IExecutor executor;

    public RabbitMqClient(String host, int port, String vhost, String username, String password,
        String queueName, IExecutor executor) {
        this.executor = executor;
        this.queueName = queueName;
        errorQueueName = queueName + "_errors";

        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(host);
        this.connectionFactory.setPort(port);
        this.connectionFactory.setVirtualHost(vhost);
        this.connectionFactory.setUsername(username);
        this.connectionFactory.setPassword(password);

        this.connect();
    }

    private void connect() {
        try {
            connection = this.connectionFactory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            try {
                int prefetchCount = 1;
                channel.basicQos(prefetchCount);
            } catch (IOException ignored) {

            }
            consumer = new RabbitMqConsumer(channel, executor, queueName);
        } catch (TimeoutException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            try {
                connection.close();
            } catch (IOException ignored) {
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    public void receive() {
        try {
            this.channel.basicConsume(this.queueName, false, this.consumer);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
