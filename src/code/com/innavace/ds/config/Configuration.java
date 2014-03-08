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

import com.innavace.ds.Convert;
import org.apache.log4j.Logger;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * User: sstacha
 * Date: Mar 5, 2013
 * Trying to keep this as simple and low memory as possible; holds the needed values for processing data storage/retrevial
 */
public class Configuration
{
    public static Logger log = Logger.getLogger(Configuration.class);
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
        String originalSql;
        if (action == null)
            action = "";
        if (action.equalsIgnoreCase("insert"))
            originalSql = this.insertStatement;
        else if (action.equalsIgnoreCase("update"))
            originalSql = this.updateStatement;
        else if (action.equalsIgnoreCase("delete"))
            originalSql = this.deleteStatement;
        else if (action.equalsIgnoreCase("query")) {
            originalSql = this.queryStatement;
            if (!this.isQueryable())
                throw new SQLException("[" + this.path + "] does not support queries.");
        }
        else
            throw new SQLException ("Requested action [" + action + "] was not found.");

        if (originalSql == null || originalSql.length() == 0)
            throw new SQLException("[" + this.path + "] does not have [" + action + "] sql defined.");

        int updatedRecs = 0;
        String cache = "";
        java.sql.Connection con = null;
        PreparedStatement ps = null;
        try
        {
            log.debug("getting connection for configuration...");
            con = ConnectionHandler.getConnection(this.connectionName);
            // strip out pre-processing directives before preparing the statement
            String sql = stripOptions(originalSql);
            log.debug("getting prepared statement for : " + sql);
            ps = con.prepareStatement(sql);

            // if we have question marks in the sql then lets set parameters one at a time for each ?
            // while we are at it re-parse the original sql to look for options
            List<String> options = getOptions(originalSql);
            int paramIdx = 0;
            // if we have parameters lets replace them in the order the parameter was received
            // NOTE: if a parameter is sent 2x then we only pick the first
            //      ex: a=1,b=2,a=3,c=3 : ?1=[a->1] ?2=[b->2] ?3=[c->3] ?4=error
            // get our string keys in order array
            Set<String> keyset = parameterMap.keySet();
            String[] keys = keyset.toArray(new String[keyset.size()]);
            for (String option : options) {
                // setting each ? parameter in the prepared statement according to the parameter passed to us by position
                if (keys.length < paramIdx)
                    throw new SQLException("Exception setting passed parameters to sql statement.  Expected [" + paramIdx + "] but only found [" + keys.length + "].");
                log.debug("param index [" + paramIdx + "]: " + keys[paramIdx] + " - " + parameterMap.get(keys[paramIdx])[0]);
                // todo : currently getting 0 value, however, in the future look for [i] given pipe extension in the configuration
                if (option.startsWith("l") || option.startsWith("L"))
                    ps.setLong(paramIdx + 1, Convert.toLng(parameterMap.get(keys[paramIdx])[0]));
                else if (option.startsWith("i") || option.startsWith("I"))
                    ps.setInt(paramIdx + 1, Convert.toInt(parameterMap.get(keys[paramIdx])[0]));
                else if (option.startsWith("f") || option.startsWith("F"))
                    ps.setFloat(paramIdx + 1, Convert.toFlt(parameterMap.get(keys[paramIdx])[0]));
                else if (option.startsWith("d") || option.startsWith("D"))
                    ps.setDouble(paramIdx + 1, Convert.toDbl(parameterMap.get(keys[paramIdx])[0]));
                else if (option.startsWith("t") || option.startsWith("T")) {
                    Date date = Convert.toDate(parameterMap.get(keys[paramIdx])[0]);
                    if (date == null)
                        ps.setTimestamp(paramIdx + 1, null);
                    else
                        ps.setTimestamp(paramIdx + 1, new Timestamp(date.getTime()));
                }
                else
                    ps.setString(paramIdx + 1, parameterMap.get(keys[paramIdx])[0]);
                paramIdx++;
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

    // strips the options out of a sql statement
    // options must follow a ? and begin and end with a pipe to be stripped out
    public String stripOptions(String originalSql) {
        // first find our ?
        int qstart = originalSql.indexOf("?");
        // if we don't have a ? then always return the originally passed sql back
        if (qstart == -1)
            return originalSql;
        // otherwise build a string buffer up that stores the sql string omitting the options
        StringBuilder buffer = new StringBuilder (originalSql.length());
        int oend, ostart;
        int send=qstart, sstart=0;
        int i;
        buffer.append(originalSql.substring(sstart, send));
        sstart = send;
        while (qstart != -1) {
            // set our next qstart
            if (send != originalSql.length() - 1)
                qstart = originalSql.indexOf("?", send + 1);
            else
                qstart = -1;
            send = qstart;
            if (send == -1)
                send = originalSql.length();
            // look for any options between sstart & send (either the next ? or the end of the string)
            ostart = sstart;
            oend = -1;
            for (i=sstart+1; i<send; i++) {
                // find the next non-whitespace character and check if it is a pipe
                if (!Character.isWhitespace(originalSql.charAt(i)))
                    break;
            }
            if (i < originalSql.length() && originalSql.charAt(i) == '|') {
                // we have a possible option so set our ostart to this char and look for the end pipe for oend + 1
                ostart = i;
                oend = originalSql.indexOf("|", ostart + 1);
                if (oend == -1 || oend > send)
                    ostart = -1;
            }
            // either copy from sstart (?) to ostart and then oend to send or copy from sstart to send depending if set
            if (ostart != -1 && oend != -1) {
                buffer.append(originalSql.substring(sstart, ostart));
                if (oend < send)
                    buffer.append(originalSql.substring(oend + 1, send));
                sstart=send;
            }
            else {
                buffer.append(originalSql.substring(sstart, send));
                sstart = send;
            }
        }

        return buffer.toString();
    }

    // returns an array of strings for options there is one element for each ? with options set or blank for each
    public List<String> getOptions(String originalSql) {
        List<String> options = new ArrayList<String>();
        // first find our ?
        int qstart = originalSql.indexOf("?");
        // if we don't have a ? then always return the empty options list back
        if (qstart == -1)
            return options;
        // otherwise build a string list of the options in order
        int oend, ostart;
        int send=qstart, sstart=0;
        int i;
        sstart = send;
        while (qstart != -1) {
            // set our next qstart
            if (send != originalSql.length() - 1)
                qstart = originalSql.indexOf("?", send + 1);
            else
                qstart = -1;
            send = qstart;
            if (send == -1)
                send = originalSql.length();
            // look for any options between sstart & send (either the next ? or the end of the string)
            ostart = sstart;
            oend = -1;
            for (i=sstart+1; i<send; i++) {
                // find the next non-whitespace character and check if it is a pipe
                if (!Character.isWhitespace(originalSql.charAt(i)))
                    break;
            }
            if (i < originalSql.length() && originalSql.charAt(i) == '|') {
                // we have a possible option so set our ostart to this char and look for the end pipe for oend + 1
                ostart = i;
                oend = originalSql.indexOf("|", ostart + 1);
                if (oend == -1 || oend > send)
                    ostart = -1;
            }
            // either append ostart and then oend to option list or an empty option so we keep the order of ?'s
            if (ostart != -1 && oend != -1 && ostart != oend) {
                options.add(originalSql.substring(ostart + 1, oend));
                sstart=send;
            }
            else {
                options.add("");
                sstart = send;
            }
        }
        return options;
    }
    public String getOption(String originalSql, int index) {
        // this should be like the strip option but return just the one we want and then bail
        // first find our correct ?
        int qstart = 0;
        for (int i=0; i<index; i++)
            if (qstart != -1)
                qstart = originalSql.indexOf("?", qstart + 1);

        // if we don't have a ? at this location then always return no options
        if (qstart == -1)
            return "";

        // we found the ? so now lets look for an option immediately following the ? but before the next one
        int oend, ostart;
        int send=qstart, sstart=qstart;
        int i;

        // set our next qstart
        if (send != originalSql.length() - 1)
            qstart = originalSql.indexOf("?", send + 1);
        else
            qstart = -1;
        send = qstart;
        if (send == -1)
            send = originalSql.length() - 1;
        // look for any options between sstart & send (either the next ? or the end of the string)
        ostart = sstart;
        oend = -1;
        for (i=sstart+1; i<send; i++) {
            // find the next non-whitespace character and check if it is a pipe
            if (!Character.isWhitespace(originalSql.charAt(i)))
                break;
        }
        if (originalSql.charAt(i) == '|') {
            // we have a possible option so set our ostart to this char and look for the end pipe for oend + 1
            ostart = i;
            oend = originalSql.indexOf("|", ostart + 1);
            if (oend == -1 || oend > send)
                ostart = -1;
        }
        // either copy from ostart and then oend to send options for this ? or send nothing
        if (ostart != -1 && oend != -1 && ostart != oend)
            return originalSql.substring(ostart + 1, oend);
        else
            return "";
    }

    private String toJSONString(String string) {
        // convert all crlf and lf to newline; escape with extra \
        string = Convert.replace(string, "\r\n", "\n");
        string = Convert.replace(string, "\r", "\n");
        string = Convert.replace(string, "\n", "\\n");
//        string = replace(string, "'", "\\'", "\\'");
//        System.out.println(string);
      	return string;
    }

    public static void main (String[] args) {
        Configuration configuration = new Configuration();
//        String originalSql = "update table bla set values (?) and id=?|l:[0]| where key = ? |i|";
        String originalSql = "UPDATE CONFIGURATIONS SET  KEYWORDS=? WHERE PATH=?";
//        String strippedSql = configuration.stripOptions("update table bla set id=?|l:[0]| where key = ? |i|");
        System.out.println(configuration.stripOptions(originalSql));
//        System.out.println("strippedOptionString: " + strippedSql);
//        System.out.println(configuration.getOption(originalSql, 1));
//        System.out.println(configuration.getOption(originalSql, 2));
//        System.out.println(configuration.getOption(originalSql, 3));
//        System.out.println(configuration.getOption(originalSql, 10));
        System.out.println(configuration.getOptions(originalSql));
    }

}
