# Activity Tools

##Setup

###Database Setup
Your need to create a mysql database using the script "hci.sql" in folder "database".
Then you can config the database connection information in txt file "db.txt"

###Database Configuration in Applications
* Data Collection Tool: "db.txt" 
* Web Application: 

If you put the "ActivityManagment.war" in tomcat webapps folder, tomcat will generate a folder "ActivityManagment" when first delpoyed. Then you can modify the database connection in path "WEB-INF\classes\config\db.txt" and restart tomcat. 

If you import the project "ActivityManagment" into eclipse, you can configue the database in "src\config\db.txt".

* Acticity Tracker (front-end) : "db.txt" 


## Data Collection Tool

Source code is in folder "ActivityCollecter".

The execute file in folder "tools" is "ActivityCollecterConsole.exe" and "ActivityUploader.exe". 

it will collect your interactions with applications which you can config it in txt file "config.txt".
You just need to run it by click "ActivityCollecterConsole.exe" and it need to run continually in order to collect your interactions

Sometimes if the program crashes or "ActivityUploader" program crashes, please close then re-start it.

## Web Application

you need deploy the web application "ActivityManagment" in a tomcat server. it is a background program to process the collected low level your interaction data and the front-end application use a web page embedded a java panel or open the default web browser to show the your history activities.

## Activity Tracker (front end applicaiton)

Source code is in folder "ActivityTracker".

You can run it by the bat file "run.bat" or the execute file "activitytracker.exe".
The database connection configuration file is also "db.txt". 

Two other parameters is for ActivityTracker: 

* WEBAPPLICATION: the web application url
      
* JAVAFX: show history activities in javafx panel if true, or open it using browser. this is because the encoding problem in javafx panel and i'm trying to fix it.
