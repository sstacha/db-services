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

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * User: sstacha
 * Date: Mar 5, 2013
 * Trying to keep this as simple and low memory as possible; holds the needed values for processing data storage/retrevial
 */
public class ConfigurationBackup
{
    public static Logger log = Logger.getLogger(ConfigurationBackup.class);
//    public static enum Action {query, insert, update, delete}
//    public static int USE_READ = (int) Math.pow(2, 1);
//    public static int USE_WRITE = (int) Math.pow(2, 2);
//    public static int USE_EXECUTE = (int) Math.pow(2, 3);
//    public static int USE_DELETE = (int) Math.pow(2, 4);

    public String connectionName;
	public String path;
	public String queryStatement;
    public String updateStatement;
    public String insertStatement;
	public String deleteStatement;
    public String keywords;
    public boolean cached;
    public String cachedResult = null;

    public boolean isSystem() {return (path != null && (path.equalsIgnoreCase("/configurations") || path.equalsIgnoreCase("/connections")));}
    public boolean isQueryable() {return (queryStatement != null && queryStatement.length() > 0);}
//    public boolean hasReadPermissions() {return ((usage & USE_READ) == USE_READ);}
//    public boolean hasWritePermissions() {return ((usage & USE_WRITE) == USE_WRITE);}
//    public boolean hasExecutePermissions() {return ((usage & USE_EXECUTE) == USE_EXECUTE);}
//    public boolean hasDeletePermissions() {return ((usage & USE_DELETE) == USE_DELETE);}


//    public void addParameter(int ordinal, int type, String name) {
//        parameters.add(new ConfigurationParameter(ordinal, type, name));
//    }

        // todo: implement field list limiter (only return certian fields)
        // todo: consider implementing sortby parameter
        // todo: consider implementing orderby parameter
        // todo: consider implementing filter parameter
        // todo: consider implementing pagenation

        // todo: figure out if good idea to add update / delete validation or post processing
        // todo: idea is maybe to look into javascript runtime like did for ISRM subscription rules to allow post processing before return

        // todo: figure out if templates are the best way to have custom return types; map to mime type accept (note: templates should key off of a mime type)
        // todo: instead of just printing errors figure out how to return errors in the return mime type requested (unless globally set) {auto=accept header defaulting to text, mime type=mime type

    public boolean hasKeyword(String filter) {
        if (filter == null || filter.length() == 0 || filter.equalsIgnoreCase("*"))
            return true;
        String[] keywordArray = keywords.split(",");
        for (String keyword : keywordArray)
            if (keyword.trim().equalsIgnoreCase(filter.trim()))
                return true;
        return false;
    }

    // takes an action and return type (accept header format) and returns a string in the format specified
    public String execute(HttpServletRequest request, String action) throws SQLException, NamingException, IOException {
        // get the responseType and parameterMap from the request and pass to other call
        String responseType = request.getHeader("accept");
        Map<String, String[]> parameterMap = request.getParameterMap();
        return execute(parameterMap, responseType, action);
    }

    public String execute(Map <String, String[]> parameterMap, String responseType, String action) throws SQLException, NamingException, IOException {
        String sql;
        if (action == null)
            action = "";
        if (action.equalsIgnoreCase("insert"))
            sql = this.insertStatement;
        else if (action.equalsIgnoreCase("update"))
            sql = this.updateStatement;
        else if (action.equalsIgnoreCase("delete"))
            sql = this.deleteStatement;
        else if (action.equalsIgnoreCase("query")) {
            sql = this.queryStatement;
            if (!this.isQueryable())
                throw new SQLException("[" + this.path + "] does not support queries.");
        }
        else
            throw new SQLException ("Requested action [" + action + "] was not found.");

        if (sql == null || sql.length() == 0)
            throw new SQLException("[" + this.path + "] does not have [" + action + "] sql defined.");

        int updatedRecs = 0;
        String cache = "";
        java.sql.Connection con = null;
        PreparedStatement ps = null;
        try
        {
            log.debug("getting connection for configuration...");
            con = ConnectionHandler.getConnection(this.connectionName);
            log.debug("getting prepared statement for : " + sql);
            ps = con.prepareStatement(sql);
            // if we have question marks in the sql then lets set parameters one at a time for each ?
            int paramIdx = 0;
            int posStart = sql.indexOf("?");
            if (posStart > -1) {
                // we have parameters so lets replace them in the order the parameter was received
                // NOTE: if a parameter is sent 2x then we only pick the first
                //      ex: a=1,b=2,a=3,c=3 : ?1=[a->1] ?2=[b->2] ?3=[c->3] ?4=error
                // todo : consider allowing special pipe extension for type and n[] for specifying multiple element values
                // get our string keys in order array
                Set<String> keyset = parameterMap.keySet();
                String[] keys = keyset.toArray(new String[keyset.size()]);
                while (posStart > -1) {
                    // setting each ? parameter in the prepared statement according to the parameter passed to us by position
                    if (keys.length < paramIdx)
                        throw new SQLException("Exception setting passed parameters to sql statement.  Expected [" + paramIdx + "] but only found [" + keys.length + "].");
                    log.debug("param index [" + paramIdx + "]: " + keys[paramIdx] + " - " + parameterMap.get(keys[paramIdx])[0]);
                    // todo : currently getting 0 value, however, in the future look for [i] given pipe extension in the configuration
                    // look for a pipe extension after the ? but don't pick up double pipe since that is oracle concatenator

                    ps.setString(paramIdx + 1, parameterMap.get(keys[paramIdx])[0]);
                    paramIdx++;
                    posStart = sql.indexOf("?", posStart + 1);
                }
            }

            if (action.equalsIgnoreCase("query")) {
                log.debug("executing query...");
                // NOTE: move setting of caching to the toX() method based on the responseType
                cache = toResponse(responseType, ps.executeQuery());
            }
            else {
                log.debug("executing update...");
                updatedRecs = ps.executeUpdate();
                cache = toResponse(responseType, updatedRecs);
            }
        }
        finally
        {
            if (ps != null) {
                try {ps.close();}
                catch (SQLException stmtex) {log.warn("exception attempting to close non-null statement: " + stmtex);}
            }
            if (con != null) {
                try {con.close();}
                catch (SQLException conex) {log.warn("exception attempting to close non-null connection: " + conex);}
            }
        }

        // some system cleanup - if we updated /configurations or /connections then we need to clear our system cache for new requests
        if ((this.path.equalsIgnoreCase("/configurations") || this.path.equalsIgnoreCase("configurations")) && updatedRecs > 0) {
            try {ConfigurationHandler.init();}
            catch (Exception ex) {log.fatal("Exception attempting to reset system configurations: " + ex);}
        }
        else if ((this.path.equalsIgnoreCase("/connections") || this.path.equalsIgnoreCase("connections")) && updatedRecs > 0) {
            try {
                ConnectionHandler.destroy();
                ConnectionHandler.init();
            }
            catch (Exception ex) {log.fatal("Exception attempting to reset system connections: " + ex);}
        }

        // if we are set for caching then reset the cache if we have updated
        if (this.cached && updatedRecs > 0)
            this.cachedResult = cache;
        return cache;
    }

    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("connectionName=").append(connectionName).append(", ");
        buffer.append("path=").append(path).append(", ");
        buffer.append("query=").append(queryStatement).append(", ");
        buffer.append("insert=").append(insertStatement).append(", ");
        buffer.append("update=").append(updateStatement).append(", ");
        buffer.append("delete=").append(deleteStatement).append(", ");
        buffer.append("cached=").append(cached).append(", ");
        buffer.append("keywords={").append(keywords).append("} ");
        return buffer.toString();
    }
    public String toXML() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append("<configuration>");
        buffer.append("<connectionName>").append(this.connectionName == null ? "" : this.connectionName).append("</connectionName>");
        buffer.append("<path>").append(this.path == null ? "" : this.path).append("</path>");
        buffer.append("<querySql>").append(this.queryStatement == null ? "" : this.queryStatement).append("</querySql>");
        buffer.append("<insertSql>").append(this.insertStatement == null ? "" : this.insertStatement).append("</insertSql>");
        buffer.append("<updateSql>").append(this.updateStatement == null ? "" : this.updateStatement).append("</updateSql>");
        buffer.append("<deleteSql>").append(this.deleteStatement == null ? "" : this.deleteStatement).append("</deleteSql>");
        buffer.append("<keywords>").append(this.keywords == null ? "" : this.keywords).append("</keywords>");
        buffer.append("</configuration>");
        log.debug("toXML string: " + buffer.toString());
        return  buffer.toString();
    }

    public String toJSON() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append("{\"connection_name\":\"").append(this.connectionName == null ? "" : this.connectionName).append("\", \"path\":\"").append(this.path);
        buffer.append("\", \"query_statement\":\"").append(this.queryStatement == null ? "" : this.queryStatement);
        buffer.append("\", \"insert_statement\":\"").append(this.insertStatement == null ? "" : this.insertStatement);
        buffer.append("\", \"update_statement\":\"").append(this.updateStatement == null ? "" : this.updateStatement);
        buffer.append("\", \"delete_statement\":\"").append(this.deleteStatement == null ? "" : this.deleteStatement);
        buffer.append("\", \"keywords\":\"").append(this.keywords == null ? "" : this.keywords).append("\"}");
        log.debug("toJSON string: " + buffer.toString());
        return buffer.toString();
    }
    // note: adding id parameter again at the end for update statements (we are simulating a form submit)
    public String toQueryString() {
        StringBuilder sb = new StringBuilder(400);
        sb.append("connectionName=").append(this.connectionName == null ? "" : this.connectionName)
                .append("&path=").append(this.path)
                .append("&querySql=").append(this.queryStatement == null ? "" : this.queryStatement)
                .append("&insertSql=").append(this.insertStatement == null ? "" : this.insertStatement)
                .append("&updateSql=").append(this.updateStatement == null ? "" : this.updateStatement)
                .append("&deleteSql=").append(this.deleteStatement == null ? "" : this.deleteStatement)
                .append("&keywords=").append(this.keywords == null ? "" : this.keywords)
                .append("&id=").append(this.path);
        return sb.toString();
    }


    private String toResponse(String returnType, ResultSet rs) throws SQLException {
        // thin wrapper to handle shifting between JSON and XML etc.
        return toJSON(rs);
    }
    private String toResponse(String returnType, int recordsUpdated) {
       // return a thin wrapper to handle different return types
       return toJSON(recordsUpdated);
    }

    private String toJSON(int updateCount) {
        StringBuilder buffer = new StringBuilder(50);
        buffer.append("{\"update_count\":\"").append(updateCount).append("\"}");
        return buffer.toString();
    }
    private String toJSON(ResultSet rs) throws SQLException
    {
        log.debug("converting to json: " + rs);
        if (rs == null)
            return "[]";
        StringBuilder buffer = new StringBuilder(200);
        while (rs.next())
        {
            // will pass an array of objects; if problems then convert to table name with array of objects for each record
            if (buffer.length() == 0)
                buffer.append("[");
            if (buffer.length() > 1)
                buffer.append(", ");
            buffer.append("{");
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i=1; i<=metaData.getColumnCount(); i++)
            {
                if (i > 1)
                    buffer.append(",");
//                buffer.append("\"").append(metaData.getColumnLabel(i)).append("\":\"").append(rs.getString(i)).append("\"");
                // JSON deals with nulls... don't wrap in strings (also for integers and such not either once syntax is complete add here
                buffer.append("\"").append(metaData.getColumnLabel(i).toLowerCase()).append("\":");
                if (rs.getString(i) == null)
                    buffer.append(rs.getString(i));
                else
                    buffer.append("\"").append(toJSONString(rs.getString(i))).append("\"");
            }
            buffer.append("}");
        }
        if (buffer.length() > 0) {
            buffer.append("]");
            return buffer.toString();
        }
        else
            return "[]";
    }

         // --------------------------------------------------------- replace -
     /**
      * Replaces all occurances of the specified sub-string to find with the
      * specified sub-string to replace with.
      *
      * @return The modified string.
      * @param	string			 The original string.
      * @param	replaceString The string to replace.
      * @param	substring			 The string to replace with.
      */
    public static String replace(String string, String replaceString, String substring)
    {
        return replace(string, replaceString, substring, null);
    }
    public static String replace(String string, String replaceString, String substring, String omit_string)
    {
	    int pos_start = 0;
	    int pos_stop;
        StringBuilder sb = new StringBuilder();

	    if (string == null)
	        string = "";
	    if (replaceString == null)
	        replaceString = "";
	    if (substring == null)
	        substring = "";
	    if (omit_string == null)
	        omit_string = "";
	    if (string.length() > 0 && replaceString.length() > 0)
	    {
	        while ((pos_stop = string.indexOf(replaceString, pos_start)) >= 0)
	        {
	            sb.append(string.substring(pos_start, pos_stop));
	            // we might have an omitted value
	            if (omit_string.length() > 0)
	            {
	                if (string.startsWith(omit_string, pos_stop))
	                {
	                    // omitted value: prepend the original string and move start to stop + omitstring length
	                    sb.append(omit_string);
	                    pos_start = pos_stop + omit_string.length();
	                    continue;
	                }
	            }
	            sb.append(substring);
	            pos_start = pos_stop + replaceString.length();
	        }
	        sb.append(string.substring(pos_start));
	    }
	    return sb.toString();
    }

    private String toJSONString(String string) {
        // convert all crlf and lf to newline; escape with extra \
        string = replace(string, "\r\n", "\n");
        string = replace(string, "\r", "\n");
        string = replace(string, "\n", "\\n");
//        string = replace(string, "'", "\\'", "\\'");
//        System.out.println(string);
      	return string;

    }

}
