/* Copyright 2013 Stephen Stacha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.innavace.ds.config;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * User: sstacha
 * Date: Mar 5, 2013
 * Holds the basic settings to create a connection via jndi; may move the create / destroy logic here as well
 */
public class Connection {
    public static Logger log = Logger.getLogger(Connection.class);

    public String type;
    public String name;
    public String jndiContext;
    public String jndiDatasource;
    public String jdbcDriver;
    public String jdbcUrl;
    public String jdbcUserName;
    public String jdbcPassword;
    public String description;
    private DataSource dataSource;

    public Connection() { }
    public Connection(String name, String type, String jndiContext, String jndiDatasource, String jdbcDriver,
                      String jdbcUrl, String jdbcUserName, String jdbcPassword, String description)
    {
        this.name = name;
        this.type = type;
        this.jndiContext = jndiContext;
        this.jndiDatasource = jndiDatasource;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.jdbcUserName = jdbcUserName;
        this.jdbcPassword = jdbcPassword;
        this.description = description;
        this.dataSource = null;
    }

    public boolean isValid() {
        // test that we have minimum properties
        // must have type
        if (this.type == null || this.type.length() == 0)
            return false;
        if (type.equalsIgnoreCase("jndi")) {
            return !(jndiDatasource == null || jndiDatasource.length() == 0);
        }
        if (type.equalsIgnoreCase("jdbc")) {
            if (jdbcDriver == null || jdbcDriver.length() == 0)
                return false;
            if (jdbcUrl == null || jdbcUrl.length() == 0)
                return false;
            return !(jdbcUserName == null || jdbcUserName.length() == 0);
        }
        else
            return false;
    }

    public boolean test() throws NamingException, SQLException {
        java.sql.Connection connection = null;

        if (this.type.equalsIgnoreCase("jndi")) {
            // return the connection if possible from the JNDI calls
            // Obtain our environment naming context
            // Ex: context="java:comp/env"  datasource="jdbc/EmployeeDB"
            Context initCtx = new InitialContext();
            Context envCtx;
            if (jndiContext != null && jndiContext.length() > 0)
                envCtx = (Context) initCtx.lookup(jndiContext);
            else
                envCtx = initCtx;

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup(jndiDatasource);

            // Allocate and use a connection from the pool then put it back
            connection = ds.getConnection();
            if (connection != null)
                connection.close();
            return true;
        }
        else if (this.type.equalsIgnoreCase("jdbc")) {

            // create a connection via driver
            log.debug("ATTEMPTING TO CREATE JDBC CONNECTION: [" + jdbcDriver + " @ " + jdbcUrl + "] " + jdbcUserName + " / " + jdbcPassword);

            // check that we can instaciate the driver class
            try {Class.forName(jdbcDriver);}
            catch (ClassNotFoundException cnfe) {
                log.info("CONNECTION DRIVER LOAD EXCEPTION: DRIVER [" + jdbcDriver + "] CLASSES NOT FOUND!");
                throw new SQLException(cnfe);
            }
            log.debug("loaded driver...");
            try {connection = DriverManager.getConnection(jdbcUrl, jdbcUserName, jdbcPassword);}
            catch (SQLException ex) {
                log.info("CONNECTION CREATION EXCEPTION: " + ex);
                throw(ex);
            }
            finally {
                if (connection != null)
                    connection.close();
            }
            log.debug("created connection...");
            return true;
        }
        else {
            log.warn("CONNECTION TYPE [" + type + "] WAS REQUESTED BUT THE CODE BLOCK WAS NOT CREATED TO HANDLE THIS TYPE!");
            return false;
        }
    }

    public java.sql.Connection getConnection() throws NamingException, SQLException
    {
        log.debug("datasource: " + dataSource);
        if (dataSource != null) {
            return dataSource.getConnection();
        }

//        // create a connection via driver
//        // todo : change this to tomcat connection pooling with possible multiple pools for custering / cloud
//        log.debug("ATTEMPTING TO CREATE JDBC CONNECTION: [" + jdbcDriver + " @ " + jdbcUrl + "] " + jdbcUserName + " / " + jdbcPassword);
//
//        // check that we can create a driver class instance
//        try {Class.forName(jdbcDriver);}
//        catch (ClassNotFoundException cnfe) {
//            log.fatal("CONNECTION DRIVER LOAD EXCEPTION: DRIVER [" + jdbcDriver + "] CLASSES NOT FOUND!");
//            throw new SQLException(cnfe);
//        }
//        log.debug("loaded driver...");
//        try {return DriverManager.getConnection(jdbcUrl, jdbcUserName, jdbcPassword);}
//        catch (SQLException ex) {
//            log.fatal("CONNECTION CREATION EXCEPTION: " + ex);
//            throw(ex);
//        }
        log.debug("building datasource pool from properties: [" + jdbcDriver + "]: " + jdbcUrl + " @ " + jdbcUserName + " / " + jdbcPassword);
        // attempt to set up the datasource for the first time and return a connection
        PoolProperties p = new PoolProperties();
        p.setUrl(jdbcUrl);
        p.setDriverClassName(jdbcDriver);
        p.setUsername(jdbcUserName);
        p.setPassword(jdbcPassword);
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(100);
        p.setInitialSize(2);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        dataSource = new DataSource();
        dataSource.setPoolProperties(p);
        log.debug("set properties...");
        return dataSource.getConnection();
        // return a connection based on type
//        if (this.type.equalsIgnoreCase("jndi")) {
//            // return the connection if possible from the JNDI calls
//            // Obtain our environment naming context
//            // Ex: context="java:comp/env"  datasource="jdbc/EmployeeDB"
//            Context initCtx = new InitialContext();
//            Context envCtx;
//            if (Convert.toString(jndiContext).length() > 0)
//                envCtx = (Context) initCtx.lookup(jndiContext);
//            else
//                envCtx = initCtx;
//
//            // Look up our data source
//            DataSource ds = (DataSource) envCtx.lookup(jndiDatasource);
//
//            // Allocate and use a connection from the pool
//            return ds.getConnection();
//        }
//        else if (this.type.equalsIgnoreCase("jdbc")) {
//            java.sql.Connection con;
//
//            // create a connection via driver
//            // todo : change this to tomcat connection pooling with possible multiple pools for custering / cloud
//            System.out.println("ATTEMPTING TO CREATE JDBC CONNECTION: [" + jdbcDriver + " @ " + jdbcUrl + "] " + jdbcUserName + " / " + jdbcPassword);
//
//            // check that we can instaciate the driver class
//            try {Class.forName(jdbcDriver);}
//            catch (ClassNotFoundException cnfe) {
//                System.out.println("CONNECTION DRIVER LOAD EXCEPTION: DRIVER [" + jdbcDriver + "] CLASSES NOT FOUND!");
//                throw new SQLException(cnfe);
//            }
//            System.out.println("loaded driver...");
//            try {con = DriverManager.getConnection(jdbcUrl, jdbcUserName, jdbcPassword);}
//            catch (SQLException ex) {
//                System.out.println("CONNECTION CREATION EXCEPTION: " + ex);
//                throw(ex);
//            }
//            System.out.println("created connection...");
//            return con;
//        }
//        else {
//            System.out.println("CONNECTION TYPE [" + type + "] WAS REQUESTED BUT THE CODE BLOCK WAS NOT CREATED TO HANDLE THIS TYPE!");
//            return null;
//        }
    }

    public void close()
    {
        if (dataSource != null)
            dataSource.close(true);
    }

    public String toXML() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append("<connection>");
        buffer.append("<name>").append(this.name == null ? "" : this.name).append("</name>");
        buffer.append("<type>").append(this.type == null ? "" : this.type).append("</type>");
        buffer.append("<jndiContext>").append(this.jndiContext == null ? "" : this.jndiContext).append("</jndiContext>");
        buffer.append("<jndiDatasource>").append(this.jndiDatasource == null ? "" : this.jndiDatasource).append("</jndiDatasource>");
        buffer.append("<jdbcDriver>").append(this.jdbcDriver == null ? "" : this.jdbcDriver).append("</jdbcDriver>");
        buffer.append("<jdbcUrl>").append(this.jdbcUrl == null ? "" : this.jdbcUrl).append("</jdbcUrl>");
        buffer.append("<jdbcUserName>").append(this.jdbcUserName == null ? "" : this.jdbcUserName).append("</jdbcUserName>");
        buffer.append("<jdbcPassword>").append(this.jdbcPassword == null ? "" : this.jdbcPassword).append("</jdbcPassword>");
        buffer.append("<description>").append(this.description == null ? "" : this.description).append("</description>");
        buffer.append("</connection>");
        log.debug("toXML string: " + buffer.toString());
        return  buffer.toString();
    }

    public String toJSON() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append("{\"name\":\"").append(this.name).append("\", \"type\":\"").append(this.type).append("\", \"jndi_context\":\"");
        buffer.append(this.jndiContext).append("\", \"jndi_name\":\"").append(this.jndiDatasource).append("\", \"jdbc_driver\":\"");
        buffer.append(this.jdbcDriver).append("\", \"jdbc_url\":\"").append(this.jdbcUrl).append("\", \"jdbc_username\":\"");
        buffer.append(this.jdbcUserName).append("\", \"jdbc_password\":\"").append(this.jdbcPassword).append("\", \"description\":\"");
        buffer.append(this.description).append("\", \"internal\":\"");
        if (this.type.equalsIgnoreCase("jdbc") && this.jdbcDriver.equalsIgnoreCase(""))
            buffer.append("true");
        else
            buffer.append("false");
        buffer.append("\"}");
        log.debug("toJSON string: " + buffer.toString());
        return buffer.toString();
    }

    // note: adding id parameter again at the end for update statements (we are simulating a form submit)
    public String toQueryString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("name=").append(name).append("&type=").append(type)
                .append("&jdbcDriver=").append(jdbcDriver).append("&jdbcUrl=").append(jdbcUrl).append("&jdbcUserName=").append(jdbcUserName).append("&jdbcPassword=").append(jdbcPassword)
                .append("&jndiDatasource=").append(jndiDatasource).append("&jndiContext=").append(jndiContext)
                .append("&description=").append(description).append("&id=").append(name);
        return sb.toString();
    }

    @Override
    public String toString() {
        // instead of the memory reference implemented by Object we want to output a one line string with the values
        StringBuilder sb = new StringBuilder(100);
        sb.append(name).append(", ").append(type).append(", ")
                .append(jdbcDriver).append(", ").append(jdbcUrl).append(", ").append(jdbcUserName).append(", ").append(jdbcPassword).append(", ")
                .append(jndiDatasource).append(", ").append(jndiContext).append(", ")
                .append(description);
        return sb.toString();
    }

    public static void main (String[] args) {
        // attempting to test and see that a good connection gets created automatically for us
        Connection connection = new Connection("ds3", "jdbc", "", "", "org.h2.Driver", "jdbc:h2:~/dbServices/ds3", "dsadmin", "dsadmin", "system default file connection");
        try {connection.test();}
        catch (Exception ex) {System.out.println("Exception creating connection : " + ex);}
    }
}
