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

import com.innavace.ds.wrapper.OrderedParameterWrapper;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * User: sstacha
 * Date: Mar 5, 2013
 * Encapsulates all the building and retrieving of connections in the application.  Also supports configuring connections
 */
public class ConnectionHandler
{
    public static Logger log = Logger.getLogger(ConnectionHandler.class);
    public static Map<String, Connection> connectionsMap = new LinkedHashMap<String, Connection>();

    public static synchronized void init() throws SQLException{
        // we need to figure out our default system connection
        // RULES:
        //      1) system properties to fill initial "default" connection object
        //      2) look for jndi variables for environment parameters in web.xml
        //      3) use pre-defined defaults to attempt to load the connection
        //      4) load all other user defined connections from connections table using system default connection

        // NOTE: we only initialize once; if we have any existing values we bail
        if (connectionsMap.size() > 0) {
            log.warn("ATTEMPTED TO INITIALIZE CONNECTION HANDLER BUT WAS ALREADY INITIALIZED; SKIPPING INITIALIZATION!");
            return;
        }
        // attempt to build the default connection via system properties; if found and valid add to the map
        Connection connection = getSystemVariableConnection();
        if (connection != null && connection.isValid()) {
            connectionsMap.put("default", connection);
            log.info("USING SYSTEM PROPERTY CONNECTION DEFINITION: " + connection);
        }
        else {
            // attempt to build the default connection via jndi properies; if found and valid add to the map
            connection = getJndiVariableConnection();
            if (connection != null && connection.isValid()) {
                connectionsMap.put("default", connection);
                log.info("USING WEB.XML ENVIRONMENT PROPERTY CONNECTION DEFINITION: " + connection);
            }
        }

        // as a last resort if we still don't have a default connection then attempt to build from default values and add to map (DERBY or JavaDB implementation)
        if (connectionsMap.size() == 0) {
            log.debug("No configuration parameters found; attempting to create H2 in-process file database...");
            connection = new Connection("default", "jdbc", "", "", "org.h2.Driver", "jdbc:h2:~/data/dbServices/ds", "dsadmin", "dsadmin", "system default file connection");
            log.debug("attempting to determine external default connection...");
            if (connection.isValid()) {
                // first check to see if there is a reference pointer to a different database defined if inprocess database
                String sql = "SELECT * FROM CONNECTIONS WHERE NAME = 'default'";
                java.sql.Connection con = null;
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    con = connection.getConnection();
                    stmt = con.createStatement();
                    rs = stmt.executeQuery(sql);
                    if (rs != null && rs.next()) {
                        Connection externalConnection = new Connection(rs.getString("NAME"), rs.getString("TYPE"), rs.getString("JNDI_CONTEXT"), rs.getString("JNDI_NAME"), rs.getString("JDBC_DRIVER"), rs.getString("JDBC_URL"), rs.getString("JDBC_USERNAME"), rs.getString("JDBC_PASSWORD"), rs.getString("DESCRIPTION"));
                        if (externalConnection.isValid()) {
                            log.info("USING EXTERNAL DEFAULT CONNECTION: " + externalConnection);
                            connectionsMap.put("default", externalConnection);
                        }
                        else
                            log.warn("EXTERNAL DEFAULT CONNECTION FOUND BUT WAS NOT VALID... SKIPPING...");
                    }
                }
                catch (Exception ex) {
                    if (stmt != null) {
                        log.debug("attempting to create table in case does not exist");
                        try {ConnectionHandler.createSchema(stmt);}
                        catch (SQLException schemaException) {
                            log.fatal("Exception attempting to look up default connection to external database: " + ex);
                            log.fatal("Exception attempting to create table schema for connection system table: " + schemaException);
                        }
                    }
                    else
                        log.fatal("Exception attempting to look up default connection to external database: " + ex);
                }
                finally {
                    if (rs != null) {
                        try {rs.close();}
                        catch (Exception ex) {log.warn("Exception attempting to close non null result set in connection handler.init().  May have a memory leak!: " + ex);}
                    }
                    if (stmt != null) {
                        try {stmt.close();}
                        catch (Exception ex) {log.warn("Exception attempting to close non null statement in connection handler.init().  May have a memory leak!: " + ex);}
                    }
                    if (con != null) {
                        try {con.close();}
                        catch (Exception ex) {log.warn("Exception attempting to close non null connection in connection handler.init().  May have a memory leak!: " + ex);}
                    }
                }
                // if we don't have an external connection set and have a valid connection then use it
                if (connectionsMap.get("default") == null) {
                    log.debug("no external default connection; using H2 one...");
                    log.info("USING DEFAULTED CONNECTION DEFINITION: " + connection);
                    connectionsMap.put("default", connection);
                }

                // finally, use the default connection to load all the other connections
                try {
                    log.info("loading all external connections...");
                    sql = "SELECT * FROM CONNECTIONS WHERE NAME != 'default'";
                    con = connection.getConnection();
                    stmt = con.createStatement();
                    rs = stmt.executeQuery(sql);
                    while (rs != null && rs.next()) {
                        Connection externalConnection = new Connection(rs.getString("NAME"), rs.getString("TYPE"), rs.getString("JNDI_CONTEXT"), rs.getString("JNDI_NAME"), rs.getString("JDBC_DRIVER"), rs.getString("JDBC_URL"), rs.getString("JDBC_USERNAME"), rs.getString("JDBC_PASSWORD"), rs.getString("DESCRIPTION"));
                        connectionsMap.put(externalConnection.name, externalConnection);
                        if (!externalConnection.isValid())
                            log.warn("EXTERNAL CONNECTION [ " + externalConnection + "] WAS FOUND BUT WAS NOT VALID...");
                    }
                }
                catch (SQLException sqlex) {log.fatal("Exception attempting to look up remaining external connections in local database: " + sqlex);}
                catch (NamingException nex) {log.fatal("Exception attempting to look up remaining external connections in local database: " + nex);}
                finally {
                    if (rs != null) {
                        try {rs.close();}
                        catch (Exception ex) {log.warn("Exception attempting to close non null result set in connection handler.init().  May have a memory leak!: " + ex);}
                    }
                    if (stmt != null) {
                        try {stmt.close();}
                        catch (Exception ex) {log.warn("Exception attempting to close non null statement in connection handler.init().  May have a memory leak!: " + ex);}
                    }
                    if (con != null) {
                        try {con.close();}
                        catch (Exception ex) {log.warn("Exception attempting to close non null connection in connection handler.init().  May have a memory leak!: " + ex);}
                    }
                }

            }
            else
                throw new SQLException("Connection was attempted but invalid:\n" + connection);
        }

        Set<Map.Entry<String, Connection>> entries = ConnectionHandler.connectionsMap.entrySet();
        for (Map.Entry<String, Connection> entry : entries)
            log.debug("    " + entry.getKey() + "\t: " + entry.getValue());

        log.info("connections initialized");

    }

    public static synchronized boolean test(String name, String type, String jndiContext, String jndiDatasource, String jdbcDriver,
                          String jdbcUrl, String jdbcUserName, String jdbcPassword) throws NamingException, SQLException {
        log.debug("testing connection: " + name);
        Connection connection = new Connection(name, type, jndiContext, jndiDatasource, jdbcDriver, jdbcUrl, jdbcUserName, jdbcPassword, "");
        return connection.test();
    }

    public static synchronized Connection get(String connectionCode) {
        if (connectionCode == null)
            return null;
        return connectionsMap.get(connectionCode);
    }

    public static synchronized  boolean hasConnection(String connectionCode) {
        return connectionCode != null && connectionsMap.get(connectionCode) != null;
    }

    public static synchronized boolean updateConnection(Connection connection) throws NamingException, SQLException, IOException {
        if (connection == null || connection.name == null || connection.name.length() == 0)
            throw new SQLException("Unable to update connection because the connection was null or the name was empty.");
        log.debug("updating connection: " + connection.name);
        Configuration connectionConfiguration = ConfigurationHandler.getConfiguration("/connections");
        if (connectionConfiguration == null)
            throw new SQLException("/connections configuration not found!");
        OrderedParameterWrapper parameterWrapper = new OrderedParameterWrapper("application/json", null, connection.toJSON());
        connectionConfiguration.execute(parameterWrapper.getParameterMap(), "text/json", ConnectionHandler.hasConnection(connection.name) ? "update" : "insert");
        return true;
    }

    public static synchronized java.sql.Connection getConnection(String connectionCode) throws NamingException, SQLException {
        if (connectionCode == null || connectionCode.length() == 0)
            throw new SQLException("Unable to update connection because no code was passed.");
        log.debug("getting connection: " + connectionCode);
        Connection connection =  connectionsMap.get(connectionCode);
        if (connection == null)
            throw new SQLException("Unable to create connection with values provided for connection: " + connectionCode);
        log.debug("got connection: "  + connection);
        if (!connection.isValid())
            throw new SQLException("Connection [" + connectionCode + "] is not valid; update or refresh the connection to re-validate and enable it.");
        return connection.getConnection();
    }

    public static synchronized String toJSON() {
        StringBuilder buffer = new StringBuilder(400);
        Collection<Connection> connections = connectionsMap.values();
        buffer.append("[");
        for (Connection connection : connections) {
            if (buffer.length() > 1)
                buffer.append(", ");
            buffer.append(connection.toJSON());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public static synchronized String toJSON(String key) {
        Connection connection = connectionsMap.get(key);
        if (connection == null)
            return "{}";
        return connection.toJSON();
    }
    public static synchronized String toXML() {
        StringBuilder buffer = new StringBuilder(400);
        Collection<Connection> connections = connectionsMap.values();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buffer.append("<connections>");
        for (Connection connection : connections)
            buffer.append(connection.toXML());
        buffer.append("</connections>");
        return buffer.toString();
    }
    public static synchronized String toXML(String nameFilter) {
        StringBuilder buffer = new StringBuilder(400);
        Collection<Connection> connections = connectionsMap.values();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buffer.append("<connections>");
        for (Connection connection : connections)
            if (filterName(connection, nameFilter))
                buffer.append(connection.toXML());
        buffer.append("</connections>");
        return buffer.toString();
    }
    // name filter is pretty simple.  for an element return true if the path matches; false otherwise
    private static synchronized boolean filterName(Connection connection, String nameFilter) {
        return nameFilter == null || nameFilter.length() == 0 || connection.name.startsWith(nameFilter);
    }
    public static synchronized void createSchema(Statement stmt) throws SQLException {

        String sql = "CREATE TABLE connections (NAME VARCHAR(50) PRIMARY KEY, TYPE VARCHAR(4) NOT NULL DEFAULT 'jdbc', JNDI_NAME VARCHAR(50), JNDI_CONTEXT VARCHAR(50), JDBC_DRIVER VARCHAR(150), JDBC_URL VARCHAR(255), JDBC_USERNAME VARCHAR(50), JDBC_PASSWORD VARCHAR(50), DESCRIPTION VARCHAR(255) NOT NULL)";
        stmt.execute(sql);
        log.info("SYSTEM TABLE [CONNECTIONS] CREATED");
    }

    private static synchronized String getJndiVariable(String jndiLocation) {
        String value = null;
        if (jndiLocation == null || jndiLocation.length()  == 0)
            return value;
        try
        {
            log.debug("attempting to lookup <" + jndiLocation + ">...");
            Context ctx = new InitialContext();
            Object obj = ctx.lookup(jndiLocation);
            if (obj instanceof String)
                value = (String)obj;
            log.debug("Fetched JNDI <" + jndiLocation + ">: " + value);
        }
        catch (NamingException ne)
        {
            log.debug("JNDI lookup failed for variable <" + jndiLocation + ">: " + ne.getMessage());
        }
        return value;
    }
    private static synchronized Connection getSystemVariableConnection() {
        Connection connection = new Connection();
        connection.type = System.getProperty("dsc_type");
        // if we don't have parameter values then return
        if (connection.type == null || connection.type.length()  == 0)
            return null;

        connection.name = System.getProperty("dsc_name");
        connection.jndiContext = System.getProperty("dsc_jndi_context");
        connection.jndiDatasource = System.getProperty("dsc_jndi_datasource");
        connection.jdbcDriver = System.getProperty("dsc_jdbc_driver");
        connection.jdbcUrl = System.getProperty("dsc_jdbc_url");
        connection.jdbcUserName = System.getProperty("dsc_jdbc_user_name");
        connection.jdbcPassword = System.getProperty("dsc_jdbc_password");
        connection.description = System.getProperty("dsc_description");

        return connection;
    }
    private static synchronized Connection getJndiVariableConnection() {
        Connection connection = new Connection();
        connection.type = getJndiVariable("dsc_type");
        // if we don't have parameter values then return
        if (connection.type == null || connection.type.length()  == 0)
            return null;

        connection.name = getJndiVariable("dsc_name");
        connection.jndiContext = getJndiVariable("dsc_jndi_context");
        connection.jndiDatasource = getJndiVariable("dsc_jndi_datasource");
        connection.jdbcDriver = getJndiVariable("dsc_jdbc_driver");
        connection.jdbcUrl = getJndiVariable("dsc_jdbc_url");
        connection.jdbcUserName = getJndiVariable("dsc_jdbc_user_name");
        connection.jdbcPassword = getJndiVariable("dsc_jdbc_password");
        connection.description = getJndiVariable("dsc_description");

        return connection;
    }

    public static void destroy()
    {
        // allow proper cleanup for all loaded connections in the map (may be pooled)
        Collection<Connection> connections = connectionsMap.values();
        for (Connection connection : connections) {
            connection.close();
            connection = null;
        }
        connectionsMap.clear();
        log.debug("connection map cleared...");
    }

    public static void main (String[] args) {
        BasicConfigurator.configure();
        try {
            ConnectionHandler.init();
//            Collection<Connection> connections = ConnectionHandler.connectionsMap.values();
//            for (Connection con : connections)
//                System.out.println(con);
            Connection con = ConnectionHandler.get("sas_test");
            System.out.println(con.jndiContext);
            System.out.println(con.jndiDatasource);
            con.test();
            ConnectionHandler.destroy();
        }
        catch (Exception ex) {System.out.println("Exception in main: " + ex);}
    }
}
