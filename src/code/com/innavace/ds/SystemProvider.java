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
package com.innavace.ds;

import com.innavace.ds.config.Configuration;
import com.innavace.ds.config.ConfigurationHandler;
import com.innavace.ds.config.ConnectionHandler;
import com.innavace.ds.upload.XMLConfigurationUploader;
import com.innavace.ds.upload.XMLConnectionUploader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


/**
 * User: sstacha
 * Date: Mar 5, 2013
 * The system provider is responsible for handling requests for the system level objects (connections, configurations,
 * registry entries, etc.)  These operations are outside of user space so user can not modify SQL.  They always use the
 * default connection.
 */
public class SystemProvider extends HttpServlet
{
	public static Logger log = Logger.getLogger(SystemProvider.class);

    @Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
	{
        // todo : figure out how to return response headers indicating the changed date for caching
        // note : REST specifies get should do query by default
		doDebug(httpServletRequest);
        // todo: add sysreg vars to console and look for a sysreg "debug" = true, 1, -1 or T (case insensitive)
		doSystemMethod(httpServletRequest, httpServletResponse, "query");
	}

	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
	{
        // todo : figure out how to return response headers indicating the changed date for caching
        // note : REST specifies post should do insert by default
		doDebug(httpServletRequest);
        doSystemMethod(httpServletRequest, httpServletResponse, "insert");
	}

    @Override
    protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        log.debug("in do put...");
        doDebug(httpServletRequest);
        // note : rest specifies put should do update by default / insert should be possible
        // note2 : since parameters are passed in request url format as the body of the message try to interpret and over-ride since params will be null always
        doSystemMethod(httpServletRequest, httpServletResponse, "update");
    }

    @Override
    protected void doDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        log.debug("in do delete...");
        doDebug(httpServletRequest);
        // note : rest specifies delete should ALWAYS just delete
        doSystemMethod(httpServletRequest, httpServletResponse, "delete");
    }

    protected void doSystemMethod(HttpServletRequest request, HttpServletResponse response, String action) throws ServletException, IOException {
        // used for system method calls; essentially determines sql based on fixed url pattern and calls the correct sql action (query, update, delete)
        // used for data method calls; essentially determines correct action and configuration then executes either query or update with the correct action
        // if no action default to get (should always be passed)
        // start by getting any passed parameter action element in either body or url
        String _action = request.getParameter("$action");
        // if we didn't get an action via parameter lets use the default for the GET/POST/PUT/DELETE operation
        if (_action == null || _action.length() == 0)
            _action = action;
        // now if the default action was delete only allow deletions (delete only allows delete)
        if (action.equalsIgnoreCase("delete"))
            _action = action;
        // if we have a default action of update then we don't want to query or delete (put only allows insert & update)
        if (action.equalsIgnoreCase("update") && (_action.equalsIgnoreCase("delete") || _action.equalsIgnoreCase("query")))
            _action = "update";
        // just incase someone passes something stupid lets reset it to query
        if (!(_action.equalsIgnoreCase("query") || _action.equalsIgnoreCase("insert") || _action.equalsIgnoreCase("update") || _action.equalsIgnoreCase("delete")))
            _action = "query";

        // now get our path and configuration needed for sql calls
        String dsPath = (String) request.getAttribute("_DS_PATH");
        log.debug("attribute dsPath: " + dsPath);
        if (dsPath == null || dsPath.length() == 0) {
            response.sendError(404);
            return;
        }

        // we have a good action and path so lets load the sql based on path and then call the appropriate sql action
        if (dsPath.equalsIgnoreCase("/_system/connections")) {
            Configuration configuration = ConfigurationHandler.getConfiguration("/connections");
            if (configuration == null) {
                response.sendError(404, "Configuration error: /connections configuration was not found!");
                return;
            }
            if (action.equalsIgnoreCase("query"))
                response.getOutputStream().println(ConnectionHandler.toJSON());
            else {
                // attempt to execute the connection requested action
                try {response.getOutputStream().println(configuration.execute(request, _action));}
                catch (Throwable ex)
                {
                    ex.printStackTrace();
                    response.sendError(500, ex.toString());
                }
            }
        }
        else if (dsPath.equalsIgnoreCase("/_system/connections/test")) {
            // attempt to get the parameters (assume always GET)
            try {
                String name = request.getParameter("name");
                String type = request.getParameter("type");
                if (name == null || name.length() == 0 || type == null || type.length() == 0)
                    throw new RuntimeException("name or type parameter was not passed!  connection is invalid!");
                ConnectionHandler.test(name, type, request.getParameter("jndi-context"),
                        request.getParameter("jndi-name"), request.getParameter("driver"), request.getParameter("url"),
                        request.getParameter("login"), request.getParameter("password"));
                response.setContentType("application/json");
                String sresult = "{\"status\": \"Connection Successful\"}";
                response.getOutputStream().write(sresult.getBytes("UTF-8"));
                response.flushBuffer();
            }
            catch (Exception ex) {response.sendError(500, ex.toString());}
            response.flushBuffer();
        }
        else if (dsPath.equalsIgnoreCase("/_system/connections/refresh")) {
            try {ConnectionHandler.destroy(); ConnectionHandler.init();}
            catch (Exception ex) {log.fatal("Exception trying to refresh connection list: " + ex);}
            response.getOutputStream().print("refreshed");
            response.flushBuffer();
        }
        else if (dsPath.equalsIgnoreCase("/_system/connections/download")) {
            try {
                String nameFilter = request.getParameter("nameFilter");
                response.setContentType("application/xml");
                StringBuffer extension = new StringBuffer();
                String xml = ConnectionHandler.toXML(nameFilter);
                // we tack on the filter part to the end of the filename to later identify the download better
                if (nameFilter == null || nameFilter.equalsIgnoreCase("*") || nameFilter.equalsIgnoreCase("all"))
                    nameFilter = "";
                // name filter just replace the spaces and special chars with underscores
                if (nameFilter.length() > 0) {
                    nameFilter = nameFilter.replaceAll(" ", "_").toLowerCase();
                    nameFilter = nameFilter.replaceAll("[^a-zA-Z0-9.-]", "_");
                    // few other tricks:
                    // cant start or end with a dot; while valid in unix we don't want our export files to ever be hidden
                    if (nameFilter.startsWith("."))
                        nameFilter = nameFilter.substring(1);
                    if (nameFilter.endsWith("."))
                        nameFilter = nameFilter.substring(0, nameFilter.length() - 2);
                    extension.append("-").append(nameFilter.toLowerCase());
                }
                // Last: cant be more than 240 characters - the static text for the filename
                int staticLength = "connections".length() + ".xml".length();
                if (extension.length() > (240 - staticLength))
                    extension.setLength((240 - staticLength));

                response.setHeader("Content-Disposition", "attachment; filename=\"connections" + extension.toString() + ".xml\"");
                response.setContentLength(xml.length());
                response.getOutputStream().print(xml);
                response.flushBuffer();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                response.sendError(500, "Exception downloading connections: " + ex);
            }
        }
        else if (dsPath.equalsIgnoreCase("/_system/connections/upload")) {
            try {
                // read in and parse the body content
                SAXParserFactory factory = SAXParserFactory.newInstance();
               	SAXParser saxParser = factory.newSAXParser();
               	XMLConnectionUploader handler = new XMLConnectionUploader();
                String xml = request.getParameter("file");
//                log.debug("using string: " + xml);
                InputSource is = new InputSource(new StringReader(xml));
                is.setEncoding("UTF-8");
                saxParser.parse(is, handler);
                response.getOutputStream().print(handler.getStatus());
                response.flushBuffer();
            }
            catch (SAXParseException err) {
                err.printStackTrace();
                response.sendError(500, "Exception parsing connections upload file: " + err);
            }
            catch (Throwable t) {
                t.printStackTrace();
                response.sendError(500, "Exception uploading connections: " + t);
            }
        }
        else if (dsPath.equalsIgnoreCase("/_system/configurations")) {
            Configuration configuration = ConfigurationHandler.getConfiguration("/configurations");
            if (configuration == null) {
                response.sendError(404, "Configuration error: /configurations configuration was not found!");
                return;
            }
            try {response.getOutputStream().println(configuration.execute(request, _action));}
            catch (Throwable ex)
            {
                ex.printStackTrace();
                response.sendError(500, ex.toString());
            }
        }
        else if (dsPath.equalsIgnoreCase("/_system/configurations/refresh")) {
            try {ConfigurationHandler.init();}
            catch (Exception ex) {log.fatal("Exception trying to refresh configuration list: " + ex);}
            response.getOutputStream().print("refreshed");
            response.flushBuffer();
        }
        else if (dsPath.equalsIgnoreCase("/_system/configurations/download")) {
            try {
                String pathFilter = request.getParameter("pathFilter");
                String tagFilter = request.getParameter("tagFilter");
                response.setContentType("application/xml");
                StringBuffer extension = new StringBuffer();
                String xml = ConfigurationHandler.toXML(pathFilter, tagFilter);
                // we tack on the filter part to the end of the filename to later identify the download better
                if (pathFilter == null || pathFilter.equalsIgnoreCase("*") || pathFilter.equalsIgnoreCase("all"))
                    pathFilter = "";
                if (pathFilter.length() > 0) {
                    // path filter just replace the spaces and special chars with underscores
                    pathFilter = pathFilter.replaceAll(" ", "_").toLowerCase();
                    pathFilter = pathFilter.replaceAll("[^a-zA-Z0-9.-]", "_");
                    // few other tricks:
                    // cant start or end with a dot; while valid in unix we don't want our export files to ever be hidden
                    if (pathFilter.startsWith("."))
                        pathFilter = pathFilter.substring(1);
                    if (pathFilter.endsWith("."))
                        pathFilter = pathFilter.substring(0, pathFilter.length() - 2);
                    extension.append("-").append(pathFilter.toLowerCase());
                }
                // tag filter is a bit more complicated as we have to only use directory approved characters
                //      we need to obfuscate && and || so they work.  we will use underscores for spaces and
                //      words for the && and || or.
                if (tagFilter == null)
                    tagFilter = "";
                if (tagFilter.length() > 0) {
                    tagFilter = tagFilter.replaceAll(" ", "_").toLowerCase();
                    tagFilter = tagFilter.replaceAll("&&", "and");
                    tagFilter = tagFilter.replaceAll("\\|\\|", "or");
                    tagFilter = tagFilter.replaceAll(":", "#");
                    tagFilter = tagFilter.replaceAll("[^a-zA-Z0-9.-\\\\!#]", "_");
                    // few other tricks:
                    // cant start or end with a dot; while valid in unix we don't want our export files to ever be hidden
                    if (tagFilter.startsWith("."))
                        tagFilter = tagFilter.substring(1);
                    if (tagFilter.endsWith("."))
                        tagFilter = tagFilter.substring(0, tagFilter.length() - 2);
                    extension.append("-").append(tagFilter);
                }
                // Last: cant be more than 240 characters - the static text for the filename
                int staticLength = "configurations".length() + ".xml".length();
                if (extension.length() > (240 - staticLength))
                    extension.setLength(240 - staticLength);

                response.setHeader("Content-Disposition", "attachment; filename=\"configurations" + extension.toString() + ".xml\"");
                response.setContentLength(xml.length());
                response.getOutputStream().print(xml);
                response.flushBuffer();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                response.sendError(500, "Exception downloading configurations: " + ex);
            }
        }
        else if (dsPath.equalsIgnoreCase("/_system/configurations/upload")) {
            try {
                // read in and parse the body content
                SAXParserFactory factory = SAXParserFactory.newInstance();
               	SAXParser saxParser = factory.newSAXParser();
               	XMLConfigurationUploader handler = new XMLConfigurationUploader();
                String xml = request.getParameter("file");
//                log.debug("using string: " + xml);
                InputSource is = new InputSource(new StringReader(xml));
                is.setEncoding("UTF-8");
                saxParser.parse(is, handler);
                response.getOutputStream().print(handler.getStatus());
                response.flushBuffer();
            }
            catch (SAXParseException err) {
                err.printStackTrace();
                response.sendError(500, "Exception parsing configurations: " + err);
            }
            catch (Throwable t) {
                t.printStackTrace ();
                response.sendError(500, "Exception uploading configurations: " + t);
            }
        }
        else if (dsPath.equalsIgnoreCase("/_system/sql/execute")) {
            try {
                // look for $sql to attempt to execute
                String con = request.getParameter("$con");
                String sql = request.getParameter("$sql");
                log.debug("found [" + con + "] connection request...");
                log.debug("found [" + sql + "] execute request...");
                if (con == null || sql == null || con.length() == 0 || sql.length() == 0)
                    throw new Exception("Reqeusted sql execution but didn't provide required parameters.");
                Connection connection = null;
                Statement stmt = null;
                try {
                    connection = ConnectionHandler.getConnection(con);
                    if (connection == null)
                        throw new SQLException("Connection [" + con + "] was not found or set up!");
                    stmt = connection.createStatement();
                    stmt.execute(sql);
                }
                finally {
                    if (stmt != null)
                        stmt.close();
                    if (connection != null)
                        connection.close();
                }

                response.getOutputStream().print("Statment Executed Successfully");
                response.flushBuffer();
            }
            catch (Throwable t) {
                t.printStackTrace ();
                response.sendError(500, t.toString());
            }
        }
        else if (dsPath.equalsIgnoreCase("/_system/log/level/set")) {
            // attempt to set the global log level runtime for the server
            String requestLevel = request.getParameter("log_level");
            if (requestLevel != null && (requestLevel.equalsIgnoreCase("debug") || requestLevel.equalsIgnoreCase("info") || requestLevel.equalsIgnoreCase("warn"))) {
                Logger rootLogger = Logger.getRootLogger();
                if (requestLevel.equalsIgnoreCase("debug"))
                    rootLogger.setLevel(Level.DEBUG);
                else if(requestLevel.equalsIgnoreCase("info"))
                    rootLogger.setLevel(Level.INFO);
                else if(requestLevel.equalsIgnoreCase("warn"))
                    rootLogger.setLevel(Level.WARN);
                response.getOutputStream().print("log level: " + rootLogger.getLevel());
            }
            else
                response.sendError(404, "Missing log_level parameter; must be one of ['debug', 'info', 'warn']");
        }
        else if (dsPath.equalsIgnoreCase("/_system/boom")) {
            response.sendError(500, "Test Exception ");
        }
        else
            response.sendError(404);
    }

	protected void doDebug(HttpServletRequest httpServletRequest)
	{
		// find the model to load based on the prefix for the request
        log.debug("context: " + httpServletRequest.getContextPath());
        log.debug("path info: " + httpServletRequest.getPathInfo());
        log.debug("translated: " + httpServletRequest.getPathTranslated());
        log.debug("scheme: " + httpServletRequest.getScheme());
        log.debug("server name: " + httpServletRequest.getServerName());
        log.debug("server port: " + httpServletRequest.getServerPort());
        log.debug("uri: " + httpServletRequest.getRequestURI());
        log.debug("url: " + httpServletRequest.getRequestURL().toString());
        log.debug("query string: " + httpServletRequest.getQueryString());
		Enumeration headerNames = httpServletRequest.getHeaderNames();
		String headerName;
        log.debug("---------- headers ----------");
		while (headerNames.hasMoreElements())
		{
			headerName = (String)headerNames.nextElement();
            log.debug("     " + headerName + ": " + httpServletRequest.getHeader(headerName));
		}
		Enumeration parameterNames = httpServletRequest.getParameterNames();
		String parameterName;
        log.debug("---------- parameters ---------");
		while (parameterNames.hasMoreElements())
		{
			parameterName = (String)parameterNames.nextElement();
            log.debug("     " + parameterName + ": " + httpServletRequest.getParameter(parameterName));
		}
        log.debug("------------------------------");
	}

    public static void main(String[] args) {
        String xml = ConfigurationHandler.toXML("all");
        System.out.println(xml);
//        try {
//            throw new Exception("500 - ORG.H2.JDBC.JDBCSQLEXCEPTION: TABLE 'TEST3' NOT FOUND; SQL STATEMENT: SELECT * FROM TEST3 [42102-168]");
//        }
//        catch (Throwable ex) {
//            ex.printStackTrace();
//            // we want to return the update statement for pre-population of table fields if the error was
//            //  no table found
//            String errmsg = ex.toString();
//            int pos = errmsg.indexOf("TABLE ");
//            int pos2 = errmsg.indexOf(" NOT FOUND");
//            if (pos != -1 && pos2 != -1 && pos < pos2)
//                errmsg += "\nUPDATE: ";
//            System.out.println("--------------------");
//            System.out.println(errmsg);
//        }
    }
}
