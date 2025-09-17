# Java_Example_Uploader
> A simple file repository with Java servlets

This application shows how to correctly upload and download files in a web application with database support.

## Usage

This is a *sample application* developed during the lectures of the  [**Web Engineering course**](https://webengineering-univaq.github.io). The code is organized to best match the lecture topics and examples. It is not intended for production use and is not optimized in any way. 

*This example code will be shown and described approximately during the 12th lecture of the course, so wait to download it, since it may get updated in the meanwhile.*

## Installation

This is a Maven-based project. Simply download the code and open it in any Maven-enabled IDE such as Netbeans or Eclipse. 

The main branch contains the application version to be run on the **JakartaEE 10** platform inside the **Apache Tomcat 11** server. 
Note that you may need to *configure the project deploy settings* in your IDE based on the chosen platform/server: refer to the IDE help files to perform this step. For example, in Apache Netbeans, you must enter these settings in Project properties > Run.

The **JEE** branch contains the old application version designed to run on the **JavaEE 8** platform inside **Apache Tomcat 9**, and is no longer maintained or updated. 

Finally, this example uses a MySQL database. Therefore, you need a working instance of **MySQL version 8 or above**. 
**A SQL script to setup the required database is included in the project.**
Such database setup script must be run as root on the DBMS in order to create the database, populate it, and also create the application-specific user that connects to the database in the application code.
*The application assumes that the DBMS is accessible on localhost with the default port (3306) and the username/password configured by the database setup script. Otherwise, update the database connection parameters in the META_INF/context.xml.*


---

![University of L'Aquila](https://www.disim.univaq.it/skins/aqua/img/logo2021-2.png)

 
