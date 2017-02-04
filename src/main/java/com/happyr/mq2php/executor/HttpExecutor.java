package com.happyr.mq2php.executor;

import com.happyr.mq2php.Application;
import com.happyr.mq2php.exception.MessageExecutionFailedException;
import com.happyr.mq2php.message.Message;
import com.happyr.mq2php.util.Marshaller;
import com.happyr.mq2php.util.Serializer;
import java.util.logging.Level;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.pmw.tinylog.Logger;

/**
 * Execute a message by HTTP POST request.
 *
 * @author Tobias Nyholm
 */
public class HttpExecutor implements IExecutor {

    protected String httpUrl;

    public HttpExecutor(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public void execute(Message message) {
        Logger.debug("Sending post data to {}, ", this.httpUrl);
        try {
            sendHttpPost(this.httpUrl, Serializer.serializeBase64(Marshaller.toBytes(message)));
        } catch (IOException e) {
            Logger.error(e.getMessage());
            throw new MessageExecutionFailedException(e.getMessage(), false);
        }
    }

    private void sendHttpPost(String httpUrl, String body) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(httpUrl);

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("DEFERRED_DATA", body));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {
                int httpStatus = response.getStatusLine().getStatusCode();
                if (httpStatus != 200) {
                    // FIXME: decide if requeue based on the code status something like: https://github.com/ricbra/rabbitmq-cli-consumer#strict-exit-code-processing
                    throw new MessageExecutionFailedException("The http response returned non 200 status code: " + httpStatus, false);
                }
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }
}
