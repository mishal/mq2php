<?php

set_time_limit(0);

/*
 * Look both in $_SERVER, $_POST and $argv after some data.
 */
$data = null;
if (isset($_SERVER['DEFERRED_DATA'])) {
    $data = $_SERVER['DEFERRED_DATA'];
} elseif (isset($_POST['DEFERRED_DATA'])) {
    $data = $_POST['DEFERRED_DATA'];
} elseif (isset($argv)) {
    // if shell
    if (isset($argv[1])) {
        $data = $argv[1];
    }
}

if ($data === null) {
    trigger_error('No message data found', E_USER_WARNING);
    exit(1);
}

// Decode the message and get the data
$data = base64_decode($data);

$message = json_decode($data, true);
$headers = $message['headers'];
$properties = $message['headers'];
$body = $message['body'];
