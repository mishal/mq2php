package com.happyr.mq2php.util;

import org.apache.commons.codec.binary.Base64;

/**
 * Serialize a message so it can be put on the queue.
 */
public class Serializer {

    /**
     * Return a serialized version of this message.
     */
    public static String serializeBase64(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }
}
