#!/usr/bin/env bash

# Queue name, separate more by comma
QUEUE_NAMES="default"
# queue executor
EXECUTOR="fcgi" # could be "shell", "fcgi", or "http"
# Php dispatcher
DISPATCH_PATH="/var/www/myproject/dispatcher.php"
# Path to the tiny log config
LOG_CONFIG=./tinylog.properties
# Path to the application jar
JAR_PATH=./mq2php.jar
# Path to the dot evn file
ENV_FILE=./.env
PIDFILE=./mq2php.pid
# This is the command that will start the application
JAVA_COMMAND="-Dexecutor=$EXECUTOR -Dtinylog.configuration=$LOG_CONFIG -DdispatchPath=$DISPATCH_PATH -DqueueNames=$QUEUE_NAMES -jar $JAR_PATH"

# Test that the application jar is present
if [ ! -r "$JAR_PATH" ]; then
  echo "Application JAR not found at $JAR_PATH"
  exit 1
fi

case $1 in
  start)
      # source the environment file
      [ -f ${ENV_FILE} ] && source ${ENV_FILE};
      # execute, put to background
      exec java ${JAVA_COMMAND} &
      PID=$!
      echo ${PID} > ${PIDFILE};
      # sleep for a while
      sleep 2
      # check that the background process is still running, if not, die
      if ! kill -0 ${PID} > /dev/null 2>&1 ; then
          echo "The process died. Check the error log for more info." >&2  # write to stderr
          rm -f ${PIDFILE}
          exit 1
      fi
     ;;
   stop)
     if [ -f "${PIDFILE}" ];
      then
        kill `cat ${PIDFILE}` > /dev/null 2>&1
        rm -f ${PIDFILE};
      fi;
     ;;
   *)
     echo "usage: mq2php {start|stop}" ;;
esac

exit 0
