package com.happyr.mq2php.queue;

import com.happyr.mq2php.exception.MessageExecutionFailedException;
import com.happyr.mq2php.executor.IExecutor;
import com.happyr.mq2php.message.Headers;
import com.happyr.mq2php.message.Message;
import com.happyr.mq2php.message.Properties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.pmw.tinylog.Logger;

final public class RabbitMqConsumer extends DefaultConsumer {

    private String queueName;
    private IExecutor executor;

    public RabbitMqConsumer(Channel channel, IExecutor executor, String queueName) {
        super(channel);
        this.executor = executor;
        this.queueName = queueName;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
        byte[] body) throws IOException {
        super.handleDelivery(consumerTag, envelope, properties, body);
        Message message = new Message(
            new String(body),
            convertToProperties(properties),
            convertHeaders(properties.getHeaders()),
            envelope.isRedeliver(),
            this.queueName
        );

        try {
            executor.execute(message);
            // acknowledge the message
            this.getChannel().basicAck(envelope.getDeliveryTag(), false);
        } catch(MessageExecutionFailedException e) {
            Logger.error(e.getMessage());
            // requeue message if the execution failed
            if (e.requeue()) {
                Logger.debug("Requeueing the message");
                this.getChannel().basicNack(envelope.getDeliveryTag(), false, true);
            } else  {
                throw e;
            }
        }
    }

    private Properties convertToProperties(AMQP.BasicProperties amqpProperties) {
        Properties map = new Properties();
        // match with php enqueue message object
        map.put("message_id", amqpProperties.getMessageId());
        map.put("app_id", amqpProperties.getAppId());
        map.put("correlation_id", amqpProperties.getCorrelationId());
        map.put("type", amqpProperties.getType());
        map.put("content_encoding", amqpProperties.getContentEncoding());
        map.put("content_type", amqpProperties.getContentType());
        map.put("expiration", amqpProperties.getExpiration());
        map.put("priority", amqpProperties.getPriority());
        map.put("reply_to", amqpProperties.getReplyTo());
        // unit timestamp
        map.put("timestamp",
            amqpProperties.getTimestamp() != null ? amqpProperties.getTimestamp().getTime() / 1000
                : null);
        map.put("user_id", amqpProperties.getUserId());

        return map;
    }

    private Headers convertHeaders(Map<String, Object> headers) {
        Headers map = new Headers();
        if (headers != null) {
            Iterator<Entry<String, Object>> it = headers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                String key = entry.getKey();
                String value = entry.getValue().toString();
                map.put(key, value);
            }
        }
        return map;
    }

}
