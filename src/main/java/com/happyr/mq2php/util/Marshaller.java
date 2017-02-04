package com.happyr.mq2php.util;

import com.happyr.mq2php.message.Message;
import com.rabbitmq.tools.json.JSONWriter;

/**
 * Marshal JSON programmatically.
 */
public class Marshaller {

    private static JSONWriter jsonWriter = new JSONWriter();

    private Marshaller() {
    }

    public static byte[] toBytes(Message message) {
        return jsonWriter.write(message).getBytes();
    }
}
