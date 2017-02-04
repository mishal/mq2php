package com.happyr.mq2php.executor;

import com.happyr.mq2php.Application;
import com.happyr.mq2php.exception.MessageExecutionFailedException;
import com.happyr.mq2php.message.Message;
import com.happyr.mq2php.util.Marshaller;
import com.happyr.mq2php.util.Serializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import org.pmw.tinylog.Logger;

/**
 * Execute the message payload with the php cli
 *
 * @author Tobias Nyholm
 */
public class ShellExecutor implements IExecutor {

    protected String phpBin;
    protected String dispatchPath;

    public ShellExecutor(String phpBin, String dispatchPath) {
        this.phpBin = phpBin;
        this.dispatchPath = dispatchPath;
    }

    public void execute(Message message) {

        StringBuffer output = new StringBuffer();

        String command = phpBin + " " + dispatchPath + " " + Serializer
            .serializeBase64(Marshaller.toBytes(message));

        Logger.debug("Executing shell command: {}", command);

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            String line = "";

            // Read std out
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // read str err
            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

        } catch (Exception e) {
            throw new MessageExecutionFailedException(e.getMessage(), false);
        }

        // if error
        if (0 != p.exitValue()) {
            // FIXME: decide if requeue based on the code status something like: https://github.com/ricbra/rabbitmq-cli-consumer#strict-exit-code-processing
            throw new MessageExecutionFailedException("The process returned non zero status code.", false);
        }
    }
}
