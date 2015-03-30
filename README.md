
[https://console.developers.google.com/project](https://console.developers.google.com/project)

Create a project if not exist. You have such as Project Number: 875274890887, so, "875274890887" is Sender_id
Choose APIs->  turn on Cloud Messaging for Android (Mobile APIs)
Choose Credentials -> Create new Key of Pulic API access (Browser Key)

NEXT
MAKE Server side.
Creating MySQL Database
1. Open phpmyadmin panel by going to http://localhost/phpmyadmin and create a database called gcm. (if your localhost is running on port number add port number to url)
2. After creating the database, select the database and execute following query in SQL tab to create gcm_users table.


```
#!sql

CREATE TABLE IF NOT EXISTS `gcm_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gcm_regid` text,
  `name` varchar(50) NOT NULL,
  `email` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;
Check the following video to know about creating database and php project.
```



Creating & Running the PHP Project
When we are making request to GCM server using PHP i used curl to make post request. Before creating php project enable curl module in your php extensions.
Left Click on the WAMP icon the system try -> PHP -> PHP Extensions -> Enable php_curl

1. Goto your WAMP folder and inside www folder create a folder called gcm_server_php. (In my case i installed wamp in C:\WAMP)
2. Create a filed called config.php This fill holds the database configuration and google api key.


```
#!php

config.php
<?php
/**
 * Database config variables
 */
define("DB_HOST", "localhost");
define("DB_USER", "root");
define("DB_PASSWORD", "root");
define("DB_DATABASE", "gcm");
 
/*
 * Google API Key (Public API access -> choose create browser key)
 */
define("GOOGLE_API_KEY", "BIzaSyCRLa4LQZWNQBcRCYcIVYA45i9i8zfClqc"); // Place your Google API Key
?>
```

3. Create another file called db_connect.php This file handles database connections, mainly opens and closes connection.


```
#!php

db_connect.php
<?php
  
class DB_Connect {
  
    // constructor
    function __construct() {
  
    }
  
    // destructor
    function __destruct() {
        // $this->close();
    }
  
    // Connecting to database
    public function connect() {
        require_once 'config.php';
        // connecting to mysql
        $con = mysql_connect(DB_HOST, DB_USER, DB_PASSWORD);
        // selecting database
        mysql_select_db(DB_DATABASE);
  
        // return database handler
        return $con;
    }
  
    // Closing database connection
    public function close() {
        mysql_close();
    }
  
} 
?>
```

4. Create a new file named db_functions.php This file contains function to perform database CRUD operations. But i wrote function for creating user only.


```
#!php

db_functions.php
<?php
 
class DB_Functions {
 
    private $db;
 
    //put your code here
    // constructor
    function __construct() {
        include_once './db_connect.php';
        // connecting to database
        $this->db = new DB_Connect();
        $this->db->connect();
    }
 
    // destructor
    function __destruct() {
         
    }
 
    /**
     * Storing new user
     * returns user details
     */
    public function storeUser($name, $email, $gcm_regid) {
        // insert user into database
        $result = mysql_query("INSERT INTO gcm_users(name, email, gcm_regid, created_at) VALUES('$name', '$email', '$gcm_regid', NOW())");
        // check for successful store
        if ($result) {
            // get user details
            $id = mysql_insert_id(); // last inserted id
            $result = mysql_query("SELECT * FROM gcm_users WHERE id = $id") or die(mysql_error());
            // return user details
            if (mysql_num_rows($result) > 0) {
                return mysql_fetch_array($result);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
 
    /**
     * Getting all users
     */
    public function getAllUsers() {
        $result = mysql_query("select * FROM gcm_users");
        return $result;
    }
 
}
 
?>
```

5. Create another file named GCM.php This file used to send push notification requests to GCM server.


```
#!php

GCM.php
<?php
 
class GCM {
 
    //put your code here
    // constructor
    function __construct() {
         
    }
 
    /**
     * Sending Push Notification
     */
    public function send_notification($registatoin_ids, $message) {
        // include config
        include_once './config.php';
 
        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';
 
        $fields = array(
            'registration_ids' => $registatoin_ids,
            'data' => $message,
        );
 
        $headers = array(
            'Authorization: key=' . GOOGLE_API_KEY,
            'Content-Type: application/json'
        );
        // Open connection
        $ch = curl_init();
 
        // Set the url, number of POST vars, POST data
        curl_setopt($ch, CURLOPT_URL, $url);
 
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
 
        // Disabling SSL Certificate support temporarly
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
 
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
 
        // Execute post
        $result = curl_exec($ch);
        if ($result === FALSE) {
            die('Curl failed: ' . curl_error($ch));
        }
 
        // Close connection
        curl_close($ch);
        echo $result;
    }
 
}
 
?>
```

6. Create a new file called register.php This file receives requests from android device and stores the user in the database.


```
#!php

register.php
<?php
 
// response json
$json = array();
 
/**
 * Registering a user device
 * Store reg id in users table
 */
if (isset($_POST["name"]) && isset($_POST["email"]) && isset($_POST["regId"])) {
    $name = $_POST["name"];
    $email = $_POST["email"];
    $gcm_regid = $_POST["regId"]; // GCM Registration ID
    // Store user details in db
    include_once './db_functions.php';
    include_once './GCM.php';
 
    $db = new DB_Functions();
    $gcm = new GCM();
 
    $res = $db->storeUser($name, $email, $gcm_regid);
 
    $registatoin_ids = array($gcm_regid);
    $message = array("product" => "shirt");
 
    $result = $gcm->send_notification($registatoin_ids, $message);
 
    echo $result;
} else {
    // user details missing
}
?>
```

7. Create another file called send_message.php This file used to send pushnotification to android device by making a request to GCM server.


```
#!php

send_message.php
<?php
if (isset($_GET["regId"]) && isset($_GET["message"])) {
    $regId = $_GET["regId"];
    $message = $_GET["message"];
     
    include_once './GCM.php';
     
    $gcm = new GCM();
 
    $registatoin_ids = array($regId);
    $message = array("price" => $message);
 
    $result = $gcm->send_notification($registatoin_ids, $message);
 
    echo $result;
}
?>
```

8. Finally create a file called index.php and paste the following code. The following code will create a simple admin panel to list all the user devices and provides a panel to send push notification to individual devices.


```
#!php

index.php
<!DOCTYPE html>
<html>
    <head>
        <title></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                
            });
            function sendPushNotification(id){
                var data = $('form#'+id).serialize();
                $('form#'+id).unbind('submit');                
                $.ajax({
                    url: "send_message.php",
                    type: 'GET',
                    data: data,
                    beforeSend: function() {
                         
                    },
                    success: function(data, textStatus, xhr) {
                          $('.txt_message').val("");
                    },
                    error: function(xhr, textStatus, errorThrown) {
                         
                    }
                });
                return false;
            }
        </script>
        <style type="text/css">
            .container{
                width: 950px;
                margin: 0 auto;
                padding: 0;
            }
            h1{
                font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                font-size: 24px;
                color: #777;
            }
            div.clear{
                clear: both;
            }
            ul.devices{
                margin: 0;
                padding: 0;
            }
            ul.devices li{
                float: left;
                list-style: none;
                border: 1px solid #dedede;
                padding: 10px;
                margin: 0 15px 25px 0;
                border-radius: 3px;
                -webkit-box-shadow: 0 1px 5px rgba(0, 0, 0, 0.35);
                -moz-box-shadow: 0 1px 5px rgba(0, 0, 0, 0.35);
                box-shadow: 0 1px 5px rgba(0, 0, 0, 0.35);
                font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                color: #555;
            }
            ul.devices li label, ul.devices li span{
                font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                font-size: 12px;
                font-style: normal;
                font-variant: normal;
                font-weight: bold;
                color: #393939;
                display: block;
                float: left;
            }
            ul.devices li label{
                height: 25px;
                width: 50px;                
            }
            ul.devices li textarea{
                float: left;
                resize: none;
            }
            ul.devices li .send_btn{
                background: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#0096FF), to(#005DFF));
                background: -webkit-linear-gradient(0% 0%, 0% 100%, from(#0096FF), to(#005DFF));
                background: -moz-linear-gradient(center top, #0096FF, #005DFF);
                background: linear-gradient(#0096FF, #005DFF);
                text-shadow: 0 1px 0 rgba(0, 0, 0, 0.3);
                border-radius: 3px;
                color: #fff;
            }
        </style>
    </head>
    <body>
        <?php
        include_once 'db_functions.php';
        $db = new DB_Functions();
        $users = $db->getAllUsers();
        if ($users != false)
            $no_of_users = mysql_num_rows($users);
        else
            $no_of_users = 0;
        ?>
        <div class="container">
            <h1>No of Devices Registered: <?php echo $no_of_users; ?></h1>
            <hr/>
            <ul class="devices">
                <?php
                if ($no_of_users > 0) {
                    ?>
                    <?php
                    while ($row = mysql_fetch_array($users)) {
                        ?>
                        <li>
                            <form id="<?php echo $row["id"] ?>" name="" method="post" onsubmit="return sendPushNotification('<?php echo $row["id"] ?>')">
                                <label>Name: </label> <span><?php echo $row["name"] ?></span>
                                <div class="clear"></div>
                                <label>Email:</label> <span><?php echo $row["email"] ?></span>
                                <div class="clear"></div>
                                <div class="send_container">                                
                                    <textarea rows="3" name="message" cols="25" class="txt_message" placeholder="Type message here"></textarea>
                                    <input type="hidden" name="regId" value="<?php echo $row["gcm_regid"] ?>"/>
                                    <input type="submit" class="send_btn" value="Send" onclick=""/>
                                </div>
                            </form>
                        </li>
                    <?php }
                } else { ?> 
                    <li>
                        No Users Registered Yet!
                    </li>
                <?php } ?>
            </ul>
        </div>
    </body>
</html>
```

Following is a screenshot of the admin panel generated by above code (with users registered)

NEXT 
MAKE GCM client side for Android.
