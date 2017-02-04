package com.happyr.mq2php.message;

import com.rabbitmq.tools.json.JSONSerializable;
import com.rabbitmq.tools.json.JSONWriter;

/**
 * A representation of a message
 */
public class Message implements JSONSerializable {

    private String body;
    private Headers headers;
    private Properties properties;
    private Boolean redelivered;
    private String queue;

    public Message(String body, Properties properties, Headers headers, Boolean isRedelivered,
        String queueName) {
        this.body = body;
        this.properties = properties;
        this.headers = headers;
        this.redelivered = isRedelivered;
        this.queue = queueName;
    }

    /**
     * Returns the message body.
     *
     * @return String
     */
    public String getBody() {
        return body;
    }

    /**
     * Returns the message properties
     * @return Properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Returns the message headers
     *
     * @return Headers
     */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * Is the message redelivered?
     *
     * @return Boolean
     */
    public Boolean getRedelivered() {
        return redelivered;
    }

    /**
     * Returns the queue name
     *
     * @return String
     */
    public String getQueue() {
        return queue;
    }

    public void jsonSerialize(JSONWriter jsonWriter) {
        jsonWriter.writeLimited(Message.class, this, null);
    }

}
