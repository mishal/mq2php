package com.happyr.mq2php.executor;

import com.googlecode.fcgi4j.FCGIConnection;
import com.happyr.mq2php.exception.MessageExecutionFailedException;
import com.happyr.mq2php.message.Message;
import com.happyr.mq2php.util.Marshaller;
import com.happyr.mq2php.util.PathResolver;
import com.happyr.mq2php.util.Serializer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import org.pmw.tinylog.Logger;

/**
 * Execute the message payload with HTTP
 *
 * @author Tobias Nyholm
 */
public class FastCgiExecutor implements IExecutor {

    public static final int MAX_PAYLOAD = 65535;

    private ByteBuffer buffer;
    protected String host;
    protected int port;
    protected String dispatchPath;

    public FastCgiExecutor(String host, int port, String dispatchPath) {
        this.host = host;
        this.port = port;
        this.dispatchPath = dispatchPath;
        buffer = ByteBuffer.allocateDirect(10240);
    }

    public void execute(Message message) {
        try {
            doExecute(message);
        } catch (ConnectException e) {
            Logger.error("Could not connect to to fastcgi server: {}", e.getMessage());
            throw new MessageExecutionFailedException("Could not connect to to fastcgi server: " + e.getMessage(), false);
        } catch (IOException e) {
            Logger.error("IO exception: {}", e.getMessage());
            throw new MessageExecutionFailedException("IO exception: " + e.getMessage(), false);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Logger.error("Unknown java error: {}", sw.toString());
            throw new MessageExecutionFailedException("Unknown java error: " + sw.toString(),                false);
        }
    }

    private void doExecute(Message message) throws IOException {

        // create FastCGI connection
        FCGIConnection connection = FCGIConnection.open();

        connection.connect(new InetSocketAddress(this.host, this.port));

        connection.beginRequest(PathResolver.resolve(PathResolver.resolve(this.dispatchPath)));
        connection.setRequestMethod("POST");

        Logger.debug("Sending post data to {}", this.dispatchPath);

        byte[] postData = ("DEFERRED_DATA=" + Serializer
            .serializeBase64(Marshaller.toBytes(message))).getBytes();

        // set contentLength
        int dataLength = postData.length;
        connection.setContentLength(dataLength);

        // Send data to the fcgi server.
        int offset = 0;
        while (offset + MAX_PAYLOAD < dataLength) {
            connection.write(ByteBuffer.wrap(postData, offset, MAX_PAYLOAD));
            offset += MAX_PAYLOAD;
        }
        connection.write(ByteBuffer.wrap(postData, offset, dataLength - offset));

        Map<String, String> responseHeaders = connection.getResponseHeaders();
        for (String key : responseHeaders.keySet()) {
            Logger.debug("Http header {} -> {}", key, responseHeaders.get(key));
        }

        // read response data
        connection.read(buffer);

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        // close the connection
        connection.close();
        buffer.clear();

        if (connection.hasOutputOnStdErr()) {
            // String response = new String(data);
            throw new MessageExecutionFailedException("The connection to fastcgi has an error on stdout", false);
        }
    }
}