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

import com.innavace.ds.config.ConfigurationHandler;
import com.innavace.ds.config.Configuration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;


/**
 * User: sstacha
 * Date: Mar 5, 2013
 * The data provider is responsible for handling requests for the data level objects defined in the database (connections, configurations,
 * registry entries, etc.)  These operations are outside of user space so user can not modify SQL.  They always use the
 * default connection.
 */
public class DataProvider extends HttpServlet
{
	public static Logger log = Logger.getLogger(DataProvider.class);

    @Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        // note : REST specifies get should do query by default
        log.debug("in doGet...");
		doDebug(httpServletRequest);
		doDataMethod(httpServletRequest, httpServletResponse, "query");
	}

	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        // note : REST specifies post should do insert by default
        log.debug("in doPost...");
		doDebug(httpServletRequest);
        doDataMethod(httpServletRequest, httpServletResponse, "insert");
	}

    @Override
    protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        // note : REST specifies put should do update by default
        log.debug("in doPut...");
        doDebug(httpServletRequest);
        doDataMethod(httpServletRequest, httpServletResponse, "update");
    }

    @Override
    protected void doDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        // note : REST specifies delete should delete only
        log.debug("in doDelete...");
        doDebug(httpServletRequest);
        doDataMethod(httpServletRequest, httpServletResponse, "delete");
    }

    protected void doDataMethod(HttpServletRequest request, HttpServletResponse response, String action) throws ServletException, IOException {
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

        Configuration configuration = ConfigurationHandler.getConfiguration(dsPath);
        if (configuration == null) {
            response.sendError(404, "[" + dsPath + "] configuration was not found!");
        }
        else {
            if (configuration.isSystem())
                response.sendError(403, "[" + dsPath + "] requires system privileges; try adding /_system/ prefix instead.");
            else {
            // attempt to execute the configuration requested action
                try {response.getOutputStream().println(configuration.execute(request, _action));}
                catch (Throwable ex)
                {
                    ex.printStackTrace();
                    // we want to return the update statement for pre-population of table fields if the error was
                    //  no table found
                    String emsg = ex.toString();
                    String uemsg = emsg.toUpperCase();
                    int pos = uemsg.indexOf("TABLE ");
                    int pos2 = uemsg.indexOf(" NOT FOUND");
                    if (pos != -1 && pos2 != -1 && pos < pos2)
                        emsg += "\nUPDATE: " + configuration.updateStatement;
                    response.sendError(500, emsg);
                }
            }
        }
    }

	protected void doDebug(HttpServletRequest httpServletRequest)
	{
        log.debug("request method: " + httpServletRequest.getMethod());
        log.debug("testing body: " + httpServletRequest.toString());
        log.debug("testing query string: " + httpServletRequest.getQueryString());
        log.debug("");
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
        log.debug("ds path: " + httpServletRequest.getAttribute("_DS_PATH"));
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
}
