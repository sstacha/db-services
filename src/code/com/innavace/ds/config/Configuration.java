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
import java.net.URLEncoder;
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

    private boolean hasOption (String option, String[] options) {
        for (String opt : options) {
            if (opt.trim().equalsIgnoreCase(option.trim()))
                return true;
        }
        return false;
    }

    private String getOptType (String[] opts) {
        // look for a one char option;
        String type = "s";
        for (String opt : opts) {
            if (opt.trim().length() == 1) {
                type = opt.trim();
            }
        }
        return type;
    }
    private int getSqlType(String type) {
        if (type.equalsIgnoreCase("i") || type.equalsIgnoreCase("l"))
            return Types.INTEGER;
        if (type.equalsIgnoreCase("f"))
            return Types.FLOAT;
        if (type.equalsIgnoreCase("d"))
            return Types.DOUBLE;
        if (type.equalsIgnoreCase("t"))
            return Types.TIMESTAMP;
        if (type.equalsIgnoreCase("t"))
            return Types.DATE;
        if (type.equalsIgnoreCase("a"))
            return Types.ARRAY;
        return Types.VARCHAR;
    }

    private String getOutParameterResults (PreparedStatement ps, Map<String, String[]> parameterMap, List<String> options)
        throws SQLException {
        int paramIdx = 0;
        // if we have parameters lets replace them in the order the parameter was received
        // NOTE: if a parameter is sent 2x then we only pick the first
        //      ex: a=1,b=2,a=3,c=3 : ?1=[a->1] ?2=[b->2] ?3=[c->3] ?4=error
        // get our string keys in order array
        Set<String> keyset = parameterMap.keySet();
        String[] keys = keyset.toArray(new String[options.size()]);
        String[] opts;
        String optType;
        // we are trying to map our passed parameter names to the ordinal questionmarks; if we have more questionmarks than keys lets make some up
        for (int i=0; i<options.size(); i++)
            // if we have a parameter set the key and value; else set defaults
            if (i >= parameterMap.size())
                keys[i] = "p" + i;

        StringBuilder buffer = new StringBuilder(100);
        buffer.append("{");
        buffer.append("\"cs\":true");
        for (String option : options) {
            opts = option.split(":");
            optType = getOptType(opts);
            if (ps instanceof CallableStatement) {
                if (hasOption("out", opts)) {
                    buffer.append(", ");
                    // add our key and return value according to data type
                    if (optType.equalsIgnoreCase("l"))
                        buffer.append("\"").append(keys[paramIdx]).append("\":").append(((CallableStatement) ps).getLong((paramIdx + 1)));
                    else if (optType.equalsIgnoreCase("i"))
                        buffer.append("\"").append(keys[paramIdx]).append("\":").append(((CallableStatement) ps).getInt((paramIdx + 1)));
                    else if (optType.equalsIgnoreCase("f"))
                        buffer.append("\"").append(keys[paramIdx]).append("\":").append(((CallableStatement) ps).getFloat((paramIdx + 1)));
                    else if (optType.equalsIgnoreCase("d"))
                        buffer.append("\"").append(keys[paramIdx]).append("\":").append(((CallableStatement) ps).getDouble((paramIdx + 1)));
                    else if (optType.equalsIgnoreCase("t"))
                        buffer.append("\"").append(keys[paramIdx]).append("\":").append(((CallableStatement) ps).getTimestamp((paramIdx + 1)));
                    else
                        buffer.append("\"").append(keys[paramIdx]).append("\":\"").append(((CallableStatement) ps).getString((paramIdx + 1))).append("\"");
                }
            } else {
                log.debug("parameter: " + paramIdx + " - not callable statements skipping setting out params...");
            }
            paramIdx++;
        }
        buffer.append("}");
        return buffer.toString();
    }

    private void setParametersStatic(PreparedStatement ps, Map<String, String[]> parameterMap, List<String> options)
        throws SQLException {
        ps.setString(1, "testing");
        ((CallableStatement) ps).registerOutParameter(2, Types.VARCHAR);
        ((CallableStatement) ps).registerOutParameter(3, Types.INTEGER);
    }

    private void setParameters(PreparedStatement ps, Map<String, String[]> parameterMap, List<String> options)
        throws SQLException {
        int paramIdx = 0;
        // if we have parameters lets replace them in the order the parameter was received
        // NOTE: if a parameter is sent 2x then we only pick the first
        //      ex: a=1,b=2,a=3,c=3 : ?1=[a->1] ?2=[b->2] ?3=[c->3] ?4=error
        // get our string keys in order array
        Set<String> keyset = parameterMap.keySet();
        String[] keys = keyset.toArray(new String[options.size()]);
        String[] values = new String[options.size()];
        String[] opts;
        String optType;
        // we are trying to map our passed parameter names to the ordinal questionmarks; if we have more questionmarks than keys lets make some up
        for (int i=0; i<options.size(); i++) {
            // if we have a parameter set the key and value; else set defaults
            if (i < parameterMap.size()) {
                values[i] = parameterMap.get(keys[i])[0];
            }
            else {
                keys[i] = "p" + i;
                values[i] = "";
            }
        }
        for (String option : options) {
            log.debug("param index [" + paramIdx + "]: " + keys[paramIdx] + " - " + showNulls(values[paramIdx]));
            // todo : currently getting 0 value, however, in the future look for [i] given pipe extension in the configuration
            opts = option.split(":");
            optType = getOptType(opts);
            if (ps instanceof CallableStatement) {
                if (hasOption("out", opts)) {
                    log.debug("registering output parameter: " + (paramIdx + 1) + " - " + getSqlType(optType));
                    ((CallableStatement) ps).registerOutParameter((paramIdx + 1), getSqlType(optType));
                    if (hasOption("in", opts))
                        setParameter(ps, optType, (paramIdx + 1), values[paramIdx]);
                }
                else
                    setParameter(ps, optType, (paramIdx + 1), values[paramIdx]);
            }
            else
                setParameter(ps, optType, (paramIdx + 1), values[paramIdx]);

            paramIdx++;
        }
    }

    // convert nulls to a printable value
    private String showNulls(String value) {
        if (value == null)
            return "#null#";
        return value;
    }

    // since we need to call the same logic in several places im pulling this out to a function
    private void setParameter(PreparedStatement ps, String optType, int index, String value)
        throws SQLException {

        log.debug("setting parameter: " + index + " - " + showNulls(value));
        log.debug("parameter value is null?: " + (value == null));
//        if (value == null) {
//            log.debug("detected null value; setting null parameter for type [" + optType + "]...");
//            ps.setNull(index, getSqlType(optType));
//            return;
//        }
        if (optType.equalsIgnoreCase("l"))
            ps.setLong(index, Convert.toLng(value));
        else if (optType.equalsIgnoreCase("i"))
            ps.setInt(index, Convert.toInt(value));
        else if (optType.equalsIgnoreCase("f"))
            ps.setFloat(index, Convert.toFlt(value));
        else if (optType.equalsIgnoreCase("d"))
            ps.setDouble(index, Convert.toDbl(value));
        else if (optType.equalsIgnoreCase("t")) {
            Date date = Convert.toDate(value);
            if (date == null)
                ps.setTimestamp(index, null);
            else
                ps.setTimestamp(index, new Timestamp(date.getTime()));
        }
        else
            ps.setString(index, value);

    }

    private void setParametersOld(PreparedStatement ps, Map<String, String[]> parameterMap, List<String> options)
        throws SQLException {
        // for each option; set the ordinal parameter to the correct type
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

    }

    private boolean isCallableStatement(String sql) {
        // if we match a pattern then we are a callable statement
        String regex = "\\s*\\{.*call.*\\}";
        return sql.matches(regex);
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
        // NEW: we need to evaluate the sql string to intelligently determine if we use a prepared statement or a callable one.
        CallableStatement cs = null;
        try
        {
            // determine if we have a callable statement or a prepared one
            boolean isCallable = isCallableStatement(originalSql);
            log.debug("getting connection for configuration...");
            con = ConnectionHandler.getConnection(this.connectionName);
            // strip out pre-processing directives before preparing the statement
            String sql = stripOptions(originalSql);
            // if we have question marks in the sql then lets set parameters one at a time for each ?
            // while we are at it re-parse the original sql to look for options
            List<String> options = getOptions(originalSql);
            if (isCallable) {
                log.debug("getting prepared statement for : " + sql);
                cs = con.prepareCall(sql);
                // callable statements need to set parameters as in, out or inout; also we skip setting out parameters
                // NOTE: this will be done in setParameters if we get a CallableStatement typeof as parameter
                setParameters(cs, parameterMap, options);
                log.debug("executing update...");
                boolean isResultset = cs.execute();
                // getOutParameters will register each parameter name passed and the value of the return within one javascript object
                String json = getOutParameterResults(cs, parameterMap, options);
                // results should contain all of our JSON string; NOTE: could be {}.
                //int idx = 0;
                int resultsetIdx = 0;
                StringBuilder buffer = new StringBuilder(200);
                int pos = json.lastIndexOf("}");
                if (pos != -1)
                    buffer.append(json.substring(0, pos));
                else
                    buffer.append("{").append(json);

                while (isResultset) {
                    // append results will copy all but the close tag and then add on a key of rs+index and the json of the resultset
                    if (buffer.length() > 2)
                        buffer.append(", ");
                    // for debugging move to an append after we see everything working
                    String jsonrs = toJSON(cs.getResultSet());
                    buffer.append("\"rs").append(resultsetIdx++).append("\":").append(jsonrs);
                    isResultset = cs.getMoreResults();
                }
                buffer.append("}");
                cache = buffer.toString();
            } else {
                log.debug("getting prepared statement for : " + sql);
                ps = con.prepareStatement(sql);
                setParameters(ps, parameterMap, options);
                if (action.equalsIgnoreCase("query")) {
                    log.debug("executing query...");
                    cache = toResponse(responseType, ps.executeQuery());
                }
                else {
                    log.debug("executing update...");
                    updatedRecs = ps.executeUpdate();
                    cache = toResponse(responseType, updatedRecs);
                }
            }
        }
        finally
        {
            if (cs != null) {
                try {cs.close();}
                catch (SQLException stmtex) {log.warn("exception attempting to close non-null callable statement: " + stmtex);}
            }
            if (ps != null) {
                try {ps.close();}
                catch (SQLException stmtex) {log.warn("exception attempting to close non-null prepared statement: " + stmtex);}
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
        StringBuilder buffer = new StringBuilder(200);
        buffer.append("connectionName=").append(connectionName).append(", ");
        buffer.append("path=").append(path).append(", ");
        buffer.append("query=").append(queryStatement).append(", ");
        buffer.append("insert=").append(insertStatement).append(", ");
        buffer.append("update=").append(updateStatement).append(", ");
        buffer.append("delete=").append(deleteStatement).append(", ");
        buffer.append("keywords={").append(keywords).append("} ");
        return buffer.toString();
    }
    public String toXML() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append("<configuration>");
        buffer.append("<connectionName><![CDATA[").append(this.connectionName == null ? "" : this.connectionName).append("]]></connectionName>");
        buffer.append("<path>").append(this.path == null ? "" : this.path).append("</path>");
        buffer.append("<querySql><![CDATA[").append(this.queryStatement == null ? "" : this.queryStatement).append("]]></querySql>");
        buffer.append("<insertSql><![CDATA[").append(this.insertStatement == null ? "" : this.insertStatement).append("]]></insertSql>");
        buffer.append("<updateSql><![CDATA[").append(this.updateStatement == null ? "" : this.updateStatement).append("]]></updateSql>");
        buffer.append("<deleteSql><![CDATA[").append(this.deleteStatement == null ? "" : this.deleteStatement).append("]]></deleteSql>");
        buffer.append("<keywords><![CDATA[").append(this.keywords == null ? "" : this.keywords).append("]]></keywords>");
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
    public String toQueryString() throws java.io.UnsupportedEncodingException{
        StringBuilder sb = new StringBuilder(400);
        sb.append("connectionName=").append(this.connectionName == null ? "" : URLEncoder.encode(this.connectionName, "UTF-8"))
                .append("&path=").append(this.path)
                .append("&querySql=").append(this.queryStatement == null ? "" : URLEncoder.encode(this.queryStatement, "UTF-8"))
                .append("&insertSql=").append(this.insertStatement == null ? "" : URLEncoder.encode(this.insertStatement, "UTF-8"))
                .append("&updateSql=").append(this.updateStatement == null ? "" : URLEncoder.encode(this.updateStatement, "UTF-8"))
                .append("&deleteSql=").append(this.deleteStatement == null ? "" : URLEncoder.encode(this.deleteStatement, "UTF-8"))
                .append("&keywords=").append(this.keywords == null ? "" : URLEncoder.encode(this.keywords, "UTF-8"))
                .append("&id=").append(URLEncoder.encode(this.path, "UTF-8"));
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
                log.debug(metaData.getColumnLabel(i).toLowerCase() + " : " + rs.getString(i));
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
        // convert all quotes to escaped quotes
//        string = Convert.replace(string, "\"", "\\\"", "\\\"");
        string = Convert.replace(string, "\"", "\\\"");
//        string = replace(string, "'", "\\'", "\\'");
//        System.out.println(string);
      	return string;
    }

    public static void main (String[] args) {
//        Configuration configuration = new Configuration();
//        String originalSql = "update table bla set values (?) and id=?|l:[0]| where key = ? |i|";
//        String originalSql = "UPDATE CONFIGURATIONS SET  KEYWORDS=? WHERE PATH=?";
//        String strippedSql = configuration.stripOptions("update table bla set id=?|l:[0]| where key = ? |i|");
//        System.out.println(configuration.stripOptions(originalSql));
//        System.out.println("strippedOptionString: " + strippedSql);
//        System.out.println(configuration.getOption(originalSql, 1));
//        System.out.println(configuration.getOption(originalSql, 2));
//        System.out.println(configuration.getOption(originalSql, 3));
//        System.out.println(configuration.getOption(originalSql, 10));
//        System.out.println(configuration.getOptions(originalSql));
    }

}
