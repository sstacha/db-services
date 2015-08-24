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

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


/**
 * User: sstacha
 * Date: Mar 5, 2013
 * Encapsulates all functions for getting and writing configurations
 */
public class ConfigurationHandler
{
    public static Logger log = Logger.getLogger(ConfigurationHandler.class);
    private static Map<String, Configuration> configurationsMap = new LinkedHashMap<String, Configuration>();
    private static Map<String, String> registryMap = new LinkedHashMap<String, String>();

    public static synchronized void init() throws NamingException, SQLException {
        initConfigurations();
        log.info("data configurations initialized");
        if (!hasConfigurations())
            log.warn("no configurations crated: This shouldn't happen.");
        initSystemRegistry();
        if (registryMap.size() == 0)
            log.warn("no registry entries loaded.");
        log.info("system registry initialized");
    }

    public static boolean hasConfigurations() { return configurationsMap.size() > 0; }

    public static synchronized Collection<Configuration> getConfigurations() { return configurationsMap.values(); }

    public static Configuration getConfiguration(String path) { return configurationsMap.get(path); }

    public static synchronized Set<Map.Entry<String, String>> getRegistryEntries() { return registryMap.entrySet(); }

    private static synchronized void initConfigurations() throws NamingException, SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        // attempt to read in configurations from the datasource;
        if (configurationsMap.size() > 0)
            configurationsMap.clear();
        try
        {
            con = ConnectionHandler.getConnection("default");
            if (con == null)
                throw new SQLException("Unable to obtain default connection; aborting configuration initialization.");
            stmt = con.createStatement();
            Configuration configuration;
            try {rs = stmt.executeQuery("SELECT * FROM CONFIGURATIONS");}
            catch (SQLException sqlex)
            {
                // if we have an exception reading the configurations table lets try to create it and the default data
                // and read it again
                createDatabaseSchema(stmt);
                try {con.commit();}
                catch (Exception ex) {log.debug("attempted commit but failed: " + ex);}
                rs = stmt.executeQuery("SELECT * FROM CONFIGURATIONS");
            }
            while(rs.next())
            {
                configuration = new Configuration();
                //configuration.id = rs.getLong("CONFIGURATION_ID");
                configuration.connectionName = rs.getString("CONNECTION_NAME");
                if (configuration.connectionName == null || configuration.connectionName.length() == 0)
                    configuration.connectionName = "default";
                configuration.path = rs.getString("PATH");
                if (configuration.path != null && configuration.path.length() > 0 && (!configuration.path.startsWith("/")))
                    configuration.path = "/" + configuration.path;
                configuration.queryStatement = rs.getString("QUERY_STATEMENT");
                configuration.updateStatement = rs.getString("UPDATE_STATEMENT");
                configuration.insertStatement = rs.getString("INSERT_STATEMENT");
                configuration.deleteStatement = rs.getString("DELETE_STATEMENT");
//                configuration.cached = (rs.getString("CACHED") != null && rs.getString("CACHED").equalsIgnoreCase("true"));
                configuration.keywords = rs.getString("KEYWORDS");
                // todo - determine if we want to try to connect to a table to determine if it exists
                // NOTE: if not exists try to create it (will error if we don't have read permissions; test
                configurationsMap.put(configuration.path, configuration);
            }
        }
        finally
        {
            if (rs != null) {
                try {rs.close();}
                catch (Exception ex) {log.warn("Exception attempting to close non null result set in configuration handler.init().  May have a memory leak!: ", ex);}
            }
            if (stmt != null) {
                try {stmt.close();}
                catch (Exception ex) {log.warn("Exception attempting to close non null statement in configuration handler.init().  May have a memory leak!: ", ex);}
            }
            if (con != null) {
                try {con.close();}
                catch (Exception ex) {log.warn("Exception attempting to close non null connection in configuration handler.init().  May have a memory leak!: ",  ex);}
            }
        }
    }

    public static synchronized void createDatabaseSchema(Statement stmt)
    {
        String sql = "CREATE TABLE CONFIGURATIONS (" + // CONFIGURATION_ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, " +
            "CONNECTION_NAME VARCHAR(50), PATH VARCHAR(1000) PRIMARY KEY, " +
            "QUERY_STATEMENT VARCHAR(2000) NOT NULL, INSERT_STATEMENT VARCHAR(1000), UPDATE_STATEMENT VARCHAR(1000), DELETE_STATEMENT VARCHAR(1000), KEYWORDS VARCHAR(2000))";
        try {stmt.execute(sql);log.info("SYSTEM TABLE [CONFIGURATIONS] CREATED");}
        catch (SQLException sqlex) {log.fatal("Exception attempting to create configurations table: ", sqlex);}
        log.info("created configuration table.");
        // create the default data for this table
        // keep it simple for now and run some DDL to create the tables and such.  Later we may look at running a file
        //  that can be streamed so we can do automatic updates and such
        sql = "INSERT INTO CONFIGURATIONS (CONNECTION_NAME, PATH, " +
            "QUERY_STATEMENT, INSERT_STATEMENT, UPDATE_STATEMENT, DELETE_STATEMENT, KEYWORDS) VALUES (" +
            "'default', '/configurations', 'SELECT * FROM CONFIGURATIONS', " +
            "'INSERT INTO CONFIGURATIONS (CONNECTION_NAME, PATH, QUERY_STATEMENT, INSERT_STATEMENT, UPDATE_STATEMENT, DELETE_STATEMENT, KEYWORDS) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)', " +
            "'UPDATE CONFIGURATIONS SET CONNECTION_NAME=?, PATH=?, QUERY_STATEMENT=?, " +
                "INSERT_STATEMENT=?, UPDATE_STATEMENT=?, DELETE_STATEMENT=?, KEYWORDS=? WHERE PATH=?', " +
            "'DELETE FROM CONFIGURATIONS WHERE PATH=?', '_system, product:console')";
        log.debug(sql);
        try {stmt.execute(sql);log.info("SYSTEM [CONFIGURATIONS] DEFAULT DATA CREATED");}
        catch (SQLException sqlex) {log.fatal("Exception attempting to create default configurations data: ", sqlex);}
        sql = "INSERT INTO CONFIGURATIONS (CONNECTION_NAME, PATH, " +
            "QUERY_STATEMENT, INSERT_STATEMENT, UPDATE_STATEMENT, DELETE_STATEMENT, KEYWORDS) VALUES (" +
            "'default', '/connections', 'SELECT * FROM CONNECTIONS', " +
            "'INSERT INTO CONNECTIONS (NAME, TYPE, JDBC_DRIVER, JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD, JNDI_NAME, JNDI_CONTEXT, DESCRIPTION) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)', " +
            "'UPDATE CONNECTIONS SET NAME=?, TYPE=?, " +
                "JDBC_DRIVER=?, JDBC_URL=?, JDBC_USERNAME=?, JDBC_PASSWORD=?, " +
                "JNDI_NAME=?, JNDI_CONTEXT=?, DESCRIPTION=? WHERE NAME=?', " +
            "'DELETE FROM CONNECTIONS WHERE NAME=?', '_system, product:console')";
        log.info(sql);
        try {stmt.execute(sql);log.info("SYSTEM [CONNECTIONS] DEFAULT DATA CREATED");}
        catch (SQLException sqlex) {log.fatal("Exception attempting to create default configurations data for connections: ", sqlex);}

        sql = "CREATE TABLE SYSREG (SYSREG_CODE VARCHAR(50) PRIMARY KEY, SYSREG_VALUE VARCHAR(255) NOT NULL)";
        try {stmt.execute(sql);log.info("SYSTEM TABLE [SYSREG] CREATED");}
        catch (SQLException sqlex) {log.fatal("Exception attempting to create sysreg table: ", sqlex);}
        log.info("created sysreg table.");

        // create the default data for this table
        sql = "INSERT INTO SYSREG VALUES ('DB_VERSION', '1.0.0')";
        try {stmt.execute(sql);log.info("SYSTEM [SYSREG] DEFAULT DATA CREATED");}
        catch (SQLException sqlex) {log.fatal("Exception attempting to create default sysreg data: ", sqlex);}
        log.info("created sysreg data.");
    }

    public static synchronized void clearCache() {
        Collection<Configuration> configurations = configurationsMap.values();
        for (Configuration configuration : configurations)
            configuration.cachedResult = null;
    }

    private static void initSystemRegistry() throws NamingException, SQLException
    {
        // keep it simple; read the system registry values from the sysreg table and put them in the map
        // assume we have already run the getConfigurations() which creates the default tables if not there and populates
        // todo : move the default creation to method and call from here too
        Connection con = ConnectionHandler.getConnection("default");
        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.createStatement();
            try {rs = stmt.executeQuery("SELECT * FROM SYSREG");}
            catch (SQLException sqlex)
            {
                // if we have an exception reading the configurations table lets try to create it and the default data
                // and read it again
                createDatabaseSchema(stmt);
                rs = stmt.executeQuery("SELECT * FROM SYSREG");
            }
            String key;
            String value;
            while(rs.next())
            {
                key = rs.getString("SYSREG_CODE");
                value = rs.getString("SYSREG_VALUE");
                if (key == null || key.trim().equalsIgnoreCase(""))
                {
                    log.warn("missing registry key: skipping entry");
                    continue;
                }
                registryMap.put(key, value);
            }
        }
        finally
        {
            if (rs != null) {
                try {rs.close();}
                catch (Exception ex) {log.warn("Exception attempting to close non null result set in configuration handler.initRegistry().  May have a memory leak!: " + ex);}
            }
            if (stmt != null) {
                try {stmt.close();}
                catch (Exception ex) {log.warn("Exception attempting to close non null statement in configuration handler.initRegistry().  May have a memory leak!: " + ex);}
            }
            if (con != null) {
                try {con.close();}
                catch (Exception ex) {log.warn("Exception attempting to close non null connection in configuration handler.initRegistry().  May have a memory leak!: " + ex);}
            }
        }
    }

    public static synchronized Configuration get(String path) {
        if (path == null)
            return null;
        return configurationsMap.get(path);
    }

    public static synchronized  boolean hasConfiguration(String path) {
        return path != null && configurationsMap.get(path) != null;
    }

    public static synchronized boolean updateConfiguration(Configuration configuration) throws NamingException, SQLException, IOException {
        if (configuration == null || configuration.path == null || configuration.path.length() == 0)
            throw new SQLException("Unable to update connection because the connection was null or the name was empty.");
        log.debug("updating connection: " + configuration.path);
        Configuration connectionConfiguration = ConfigurationHandler.getConfiguration("/configurations");
        if (connectionConfiguration == null)
            throw new SQLException("/configurations configuration not found!");
        OrderedParameterWrapper parameterWrapper = new OrderedParameterWrapper(null, configuration.toQueryString(), null);
        Configuration existingConfiguration = ConfigurationHandler.get(configuration.path);
        connectionConfiguration.execute(parameterWrapper.getParameterMap(), "text/json", existingConfiguration != null ? "update" : "insert");
        return true;
    }
    public static synchronized String toJSON() {
        StringBuilder buffer = new StringBuilder(400);
        Collection<Configuration> configurations = configurationsMap.values();
        buffer.append("[");
        for (Configuration configuration : configurations) {
            if (buffer.length() > 1)
                buffer.append(", ");
            buffer.append(configuration.toJSON());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public static synchronized String toJSON(String key) {
        Configuration configuration = configurationsMap.get(key);
        if (configuration == null)
            return "{}";
        return configuration.toJSON();
    }

    public static synchronized String toXML() {return toXML(null);}
    public static synchronized String toXML(String filter) {
        StringBuilder buffer = new StringBuilder(400);
        Collection<Configuration> configurations = configurationsMap.values();
        buffer.append("<configurations>");
        String fragment;
        for (Configuration configuration : configurations) {
            if (filter == null || filter.length() == 0 || filter.equalsIgnoreCase("all")) {
                buffer.append(configuration.toXML());
            }
            else if (filter.startsWith("!")) {
                fragment = filter.substring(1);
                if (!(configuration.hasKeyword(fragment)))
                    buffer.append(configuration.toXML());
            }
            else
                if (configuration.hasKeyword(filter))
                    buffer.append(configuration.toXML());
        }
        buffer.append("</configurations>");
        return buffer.toString();
    }
    public static synchronized String toXML(String filter, String tagFilter) {
        StringBuilder buffer = new StringBuilder(400);
        Collection<Configuration> configurations = configurationsMap.values();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buffer.append("<configurations>");
        for (Configuration configuration : configurations) {
            if (filterPath(configuration, filter) && filterTags(configuration, tagFilter)) {
                buffer.append(configuration.toXML());
            }
        }
        buffer.append("</configurations>");
        return buffer.toString();
    }
    // path filter is pretty simple.  for an element return true if the path matches; false otherwise
    private static synchronized boolean filterPath(Configuration configuration, String pathFilter) {
        return pathFilter == null || pathFilter.length() == 0 || configuration.path.startsWith(pathFilter);
    }
    // tag filter is a bit more complicated; start from left to right and determine if the element is shown
    //      based on keywords/tags on the element and the filter sent in.
    private static synchronized boolean filterTags(Configuration configuration, String tagFilter) {
		// by precedence if we have a ! it goes first; ie: web product:web !crm !pie
		//		translates to web and product:web and not crm and not pie
		// strip each word block by spaces
		// NOTE: if we have an empty filter we simply want to always include everything
        if (tagFilter == null)
            tagFilter = "";
        else
            tagFilter = tagFilter.trim();
        if (tagFilter.length() == 0)
            return true;

        String[] parts = tagFilter.split("\\s+");
        String[] tags = new String[0];
        if (configuration.keywords != null && configuration.keywords.length() > 0)
            tags = configuration.keywords.replaceAll("\\s+","").split(",");
        boolean lvalue = false;
        boolean rvalue = false;
        boolean rvalueSet = false;
        int i = 0;
        String op = "";
        for (String part : parts) {
            if (i==0) {
                lvalue = evaluateCondition(part, tags);
                i=1;
            }
            else if (i==2) {
                //optional op or value2
                if (part.equals("&&") || part.equals("||"))
                    op = part;
                else {
                    rvalue = evaluateCondition(part, tags);
                    rvalueSet = true;
                    op="&&";
                    i=3;
                }
            }
            if (i==3) {
                if (!rvalueSet)
                    rvalue = evaluateCondition(part, tags);
                if (op.equals("&&"))
                    lvalue = lvalue && rvalue;
                else
                    lvalue = lvalue || rvalue;
            }
            if (++i > 3) {
                i = 2;
                rvalueSet = false;
            }
        }
        return lvalue;
    }
    private static synchronized boolean evaluateCondition(String part, String[] tags) {
        String realValue = part;
      		if (part.indexOf('!') == 0)
      			realValue = realValue.substring(1);
      		boolean found;

            found = inArray(realValue, tags);
      		if (part.indexOf('!') == 0)
      			return !found;
      		else
      			return found;

    }
    private static synchronized boolean inArray(String value, String[] arr) {
        boolean contains = false;
        for (String item : arr) {
            if (value.equalsIgnoreCase(item)) {
                contains=true;
                break;
            }
        }
        return contains;
    }
    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try
        {
            ConfigurationHandler.init();
            Collection<Configuration> configurations =  ConfigurationHandler.getConfigurations();
            for (Configuration configuration : configurations)
                System.out.println(configuration);

            String sql = "SELECT CONFIGURATIONS.*, DS_CONFIGURATION_CATEGORY_LIST (CONFIGURATION_ID) AS CATEGORIES FROM CONFIGURATIONS";
            connection = ConnectionHandler.getConnection("default");
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            while (rs != null && rs.next()) {
                System.out.println("configuration [" + rs.getString("PATH") + "]: " + rs.getString("CATEGORIES"));
            }
            if (rs != null)
                rs.close();

        }
        catch (Exception ex) {System.out.println("Exception in main: " + ex);}
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch(SQLException sqlex) {System.out.println("Exception closing statement: " + sqlex);}
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch(SQLException sqlex) {System.out.println("Exception closing connection: " + sqlex);}
            }
            ConnectionHandler.destroy();
        }
    }

}
