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
package com.innavace.ds.filter;

import com.innavace.ds.config.ConfigurationHandler;
import com.innavace.ds.wrapper.OrderedParameterRequestWrapper;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * User: sstacha
 * Date: Mar 5, 2013
 * Filter to evaluate URL's and determine if we need to route to our DataGenerator servlet.  NOTE: configured data
 * services will over-ride other normal processing if exists and is enabled.  NOTE: this should separate the logic
 * for what gets processed as data and regular web app pages.
 */
public class DataFilter implements Filter {
    public static Logger log = Logger.getLogger(DataFilter.class);

    public void init(FilterConfig config) throws ServletException { }
    public void destroy() { }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        log.debug("in data filter...");
        if (request instanceof HttpServletRequest) {
            HttpServletRequest _req = (HttpServletRequest) request;

            // get our configuration via path; path = uri - context
            String dsPath = getDataServicePath(_req);
            log.debug("filter dsPath: " + dsPath);

            // if we have a configuration for this path then route to the /_data/<path> and that is it
            if (ConfigurationHandler.getConfiguration(dsPath) != null)
                request.getRequestDispatcher("/_data" + dsPath).forward(request, response);
            else {
                // if we are handling our data then set the attribute before passing through to servlet and make sure to wrapper the request
                if (dsPath.startsWith("/_data")) {
                    // NOTE: we need to strip out the data prefix since it has been forwarded to us with it but the table doesn't have it
                    request.setAttribute("_DS_PATH", dsPath.substring("/_data".length()));
                    log.debug("dsPath in filter: " + dsPath);
                    log.debug("attribute in filter: " + request.getAttribute("_DS_PATH"));
                    chain.doFilter(new OrderedParameterRequestWrapper(_req), response);
                }
                else {
                    log.debug("data filter dspath: " + dsPath);
                    request.setAttribute("_DS_PATH", dsPath);
                    if (dsPath.startsWith("/_system"))
                        chain.doFilter(new OrderedParameterRequestWrapper(_req), response);
                    else
                        chain.doFilter(request, response);
                }
            }
        }
        else // finally if we didn't route pass through like we weren't even here
            chain.doFilter(request, response);
    }

    private String getDataServicePath(HttpServletRequest httpServletRequest)
    {
        String uri = httpServletRequest.getRequestURI();
        String contextPath = httpServletRequest.getContextPath();
        log.debug("request url: " + httpServletRequest.getRequestURL().toString());
        log.debug("request uri: " + uri);
        log.debug("context path: " + contextPath);
        if (uri.length() >= contextPath.length())
            return uri.substring(contextPath.length());
        return "";
    }

}
