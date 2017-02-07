package com.happyr.mq2php;

import com.happyr.mq2php.executor.IExecutor;
import com.happyr.mq2php.executor.FastCgiExecutor;
import com.happyr.mq2php.executor.HttpExecutor;
import com.happyr.mq2php.executor.ShellExecutor;
import com.happyr.mq2php.queue.IQueueClient;
import com.happyr.mq2php.queue.RabbitMqClient;
import com.happyr.mq2php.util.PathResolver;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.pmw.tinylog.Logger;

/**
 * @author Tobias Nyholm
 */
public class Application {

    public static void main(String[] args) {
        int nbThreads = getNumberOfThreads();
        String[] queueNames = getQueueNames();
        String queueHost = "localhost";
        int queuePort = 5672;
        String queueVhost = "/";
        String queueUsername = "guest";
        String queuePassword = "guest";

        if (System.getProperty("queueHost") != null) {
            queueHost = System.getProperty("queueHost");
        } else if(System.getenv("RABBIT_MQ_HOST") != null) {
            queueHost = System.getenv("RABBIT_MQ_HOST");
        }

        if (System.getProperty("queuePort") != null) {
            queuePort = Integer.valueOf(System.getProperty("queuePort"));
        } else if(System.getenv("RABBIT_MQ_PORT") != null) {
            queuePort = Integer.valueOf(System.getenv("RABBIT_MQ_PORT"));
        }

        if (System.getProperty("queueVhost") != null) {
            queueVhost = System.getProperty("queueVhost");
        } else if(System.getenv("RABBIT_MQ_VHOST") != null) {
            queueVhost = System.getenv("RABBIT_MQ_VHOST");
        }

        if (System.getProperty("queueUsername") != null) {
            queueUsername = System.getProperty("queueUsername");
        } else if(System.getenv("RABBIT_MQ_USERNAME") != null) {
            queueUsername = System.getenv("RABBIT_MQ_USERNAME");
        }

        if (System.getProperty("queuePassword") != null) {
            queuePassword = System.getProperty("queuePassword");
        } else if(System.getenv("RABBIT_MQ_PASSWORD") != null) {
            queuePassword = System.getenv("RABBIT_MQ_PASSWORD");
        }

        Logger.info("Starting mq2php listening to " + queueNames.length + " queues.");

        ExecutorService executor = Executors.newFixedThreadPool(nbThreads * queueNames.length);
        // Start all queue
        for (int i = 0; i < queueNames.length; i++) {
            for (int j = 0; j < nbThreads; j++) {
                String name = queueNames[i];
                try {
                    executor.execute(getNewWorker(queueHost, queuePort, queueVhost, queueUsername, queuePassword, name));
                    Logger.info("Starting worker for queue '"+ name+"'");
                } catch(IllegalArgumentException e) {
                    Logger.error("Error creating the worker instance: {}", e.getMessage());
                    throw e;
                }
            }
        }
    }

    private static Worker getNewWorker(String host, int port, String vhost, String username, String password, String name) {
        return new Worker(name, getQueueClient(host, port, vhost, username, password, name));
    }

    private static String[] getQueueNames() {
        // This is a comma separated string
        String queueNamesArg = System.getProperty("queueNames");
        if (queueNamesArg == null) {
            queueNamesArg = "default";
        }

        return queueNamesArg.split(",");
    }

    private static int getNumberOfThreads() {
        String param = System.getProperty("threads");
        if (param == null) {
            // default
            param = "1";
        }

        int threads;
        try {
            threads = Integer.parseInt(param);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The number of threads must me an integer");

        }

        if (threads < 1) {
            throw new IllegalArgumentException(
                "You must specify a number of threads that is greather than 0");
        }

        return threads;
    }

    /**
     * Get a queue client from the system properties.
     *
     * @return IQueueClient
     */
    private static IQueueClient getQueueClient(String host, int port, String vhost, String username,
        String password, String queueName) {
        String param = System.getProperty("messageQueue");
        if (param == null) {
            // default
            param = "rabbitmq";
        }

        if (param.equalsIgnoreCase("rabbitmq")) {
            return new RabbitMqClient(host, port, vhost, username, password, queueName, getExecutor());
        }

        throw new IllegalArgumentException(
            "Could not find client implementation named " + param);
    }

    /**
     * Get a executor object from the system properties
     *
     * @return IExecutor
     */
    private static IExecutor getExecutor() {
        String param = System.getProperty("executor");
        if (param == null) {
            // default
            param = "shell";
        }
        String dispatchPath = null;
        if (System.getProperty("dispatchPath") != null) {
            dispatchPath = System.getProperty("dispatchPath");
        }

        if (dispatchPath == null) {
            throw new IllegalArgumentException("Missing dispatchPath parameter which is mandatory");
        }

        // fastcgi
        if (param.equalsIgnoreCase("fcgi")) {
            // get parameters
            String fcgiHost = "localhost";
            int fcgiPort = 9000;
            if (System.getProperty("fcgiHost") != null) {
                fcgiHost = System.getProperty("fcgiHost");
            }
            if (System.getProperty("fcgiPort") != null) {
                fcgiPort = Integer.valueOf(System.getProperty("fcgiPort"));
            }
            if (System.getProperty("dispatchPath") != null) {
                dispatchPath = System.getProperty("dispatchPath");
            }

            return new FastCgiExecutor(fcgiHost, fcgiPort, PathResolver.resolve(dispatchPath));
        }

        // shell
        if (param.equalsIgnoreCase("shell")) {
            String phpBin = "php";
            if (System.getProperty("phpBin") != null) {
                phpBin = System.getProperty("phpBin");
            }
            return new ShellExecutor(phpBin, PathResolver.resolve(dispatchPath));
        }

        // http
        if (param.equalsIgnoreCase("http")) {
            return new HttpExecutor(dispatchPath);
        }

        throw new IllegalArgumentException(
            "Could not find IExecutor implementation named " + param);
    }

}
