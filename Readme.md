# Rabbit Message Queue 2 Php

This Java application pulls data from a message queue and give the message to PHP by using PHP-FPM.

## Installation

Download the jar file and put it somewhere like /opt/mq2php/mq2php.jar. You might
want to use a init script. See [this file][initFile] for a template. You do also need to install [Rabbit MQ](http://www.rabbitmq.com/).

Remember to protect your queue. If someone unauthorized could write to your queue he will get the same permission to execute
programs as PHP does. Make sure you restrict access on the queue to localhost or make sure you know what you are doing.

## Configuration

There is some configuration you might want to consider when starting the worker.

### Connection to RabbitMq

By default the application will connect to localhost. Pass additional parameters to specify the RabbitMq connection info or use environment variables:

```bash
java -DqueueHost=localhost -DqueuePort=5672 -DqueueVhost=/my-app -jar mq2php.jar
```

### Parameters:

  * `queueHost`:  RabbitMq host (default to `localhost`), environment variable: `RABBIT_MQ_HOST`
  * `queuePort`:  RabbitMq port (default to `5672`), environment variable: `RABBIT_MQ_PORT`
  * `queueUsername`: RabbitMq username (default to `quest`), environment variable: `RABBIT_MQ_USERNAME`
  * `queuePassword`: RabbitMq password (default to `quest`), environment variable: `RABBIT_MQ_PASSWORD`
  * `queueVhost`: Virtual host (default to `/`), environment variable: `RABBIT_MQ_VHOST`

You can use *dot env style* config like (see source for example):

```bash
source ./.env
java -jar mq2php.jar
```

### dispatchPath

This parameter (*mandatory*) represents the path to the php script which will process the message (if http executor is used, this is the complete url).
The message is passed to the executor as base64 encoded json array. If Php-fpm or http executor is used, it will be passed as POST data, if shell it will be passed as first argument.

Json array:
  * `body`: The message body
  * `headers`: The message headers
  * `properties`: The message properties
  * `queue`: The queue name which the message came from (useful if you listen to more then one queue (see above))
  * `redelivered`: The flag if the message is redelivered.

See the [dispatcher.php][dispatcherFile] script for example.

### executor

How do you want to execute the php job? Do you want to do it with PHP-FPM (`fcgi`), PHP cli (`shell`) command or Http (`http`)?.

```bash 
java -Dexecutor=fcgi -DdispatchPath=/var/www/myproject/dispatcher.php -jar mq2php.jar
```

Possible values are:

 * `fcgi`
 * `shell`
 * `http`

Use PHP-FPM for best performance.

#### Warning!

Connecting to unix sockets (when using php-fpm) is not supported!

### queueNames

You can subscribe to different queues with different names. You should separate names by a comma.
Defaults to `default`.

```bash
java -DqueueNames=foo,bar,baz -jar mq2php.jar
```

These queues will be evenly distributed over the worker threads.

### threads

The number of threads you want to use for each queue. As default there is one thread listening to each queue. These
threads are waiting for a response from the PHP script. If you are planning to have several long running script
simultaneously you may want to increase this. Usually you don't need to bother.

```bash 
java -jar mq2php.jar -Dthreads=3
```

When the message is sent to PHP it will contain one extra header. It will be the `queue` header that contains the
name of the queue.

### PHP-FPM (fcgi)

These parameters must exist when you are using the FastCGI executor. They can be passed from command line or from environment variables (see above).

* `fcgiHost` - If you are using PHP-FPM you have to specify a host to connect to. This is normally `localhost`.
* `fcgiPort` - The port to connect to. Defaults to `9000`.

### Shell Executor

* `phpBin` - This should be a path to the php executable, defaults to `php`.

### Logging

The application uses [tinylog](http://www.tinylog.org/configuration) for logging.
Use can setup the logging using configuration file or using command line arguments like:

```bash 
java -Dtinylog.configuration=/opt/mq2php/tinylog.properties -jar mq2php.jar
```
  
    # see: http://www.tinylog.org/configuration
    tinylog.writer = file
    tinylog.writer.filename = mq2php.log
    tinylog.level = warning
    tinylog.writer.append = true
    tinylog.writer.label = timestamp
    tinylog.writer.policies = monthly

## Contribute

If you want to make a change and compile the application your self you can do so with:

```bash
mvn clean compile assembly:single
java -Dexecutor=shell -DqueueNames=asynchronous_commands,asynchronous_events -jar target/mq2php-0.6.0-SNAPSHOT-jar-with-dependencies.jar
```

## Debian service installation

```bash
sudo cp mq2php.init-file /etc/init.d/mq2php
```

Edit to fit your environment, setup permissions... Install the service:

```
sudo insserv -v mq2php
```

See: https://wiki.debian.org/LSBInitScripts/DependencyBasedBoot

[initFile]: https://github.com/mishal/mq2php/blob/master/mq2php.init-file
[dispatcherFile]: https://github.com/mishal/mq2php/blob/master/dispatcher.php
