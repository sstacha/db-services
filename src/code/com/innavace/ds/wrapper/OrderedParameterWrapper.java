/* Copyright (c) 2013, Stephen Stacha
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that you give me credit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL STEPHEN STACHA,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.innavace.ds.wrapper;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.net.URLDecoder;
import java.util.*;
import java.io.UnsupportedEncodingException;

/**
 * User: sstacha
 * Date: Mar 5, 2013
 * Encapsulates decoding and parsing a string into a parameter map (ordered) for later retrieval
 */
public class OrderedParameterWrapper {
    public static Logger log = Logger.getLogger(OrderedParameterWrapper.class);
    private LinkedHashMap<String, String[]> parameters = new LinkedHashMap<String, String[]>();

    private static final String FIELDNAME_PREFIX = " name=\"";
    private static final String FILENAME_PREFIX = " filename=\"";
    private static final String HEADER_SUFFIX = "\r\n\r\n";
    private static final String CRLF = "\r\n";

    public OrderedParameterWrapper(String contentType, String queryString, String bodyString) {
        // first parse the query string and add any parameters found; NOTE: if param already exists add a new value
        if (queryString != null && queryString.length() > 0)
            parseUrlEncodedParameters(queryString);
        else
            log.debug("no query string found.");
        // lets determine what to do with the body based on contentType
        // contentType: MULTIPART/FORM-DATA
        if (bodyString != null && contentType != null && contentType.contains("multipart/form-data;")) {
            String boundary = "Content-Disposition";
            int posStart;
            int pos;
            // if a form boundary is defined then break the body into pieces as we search
            if ((pos = contentType.indexOf("boundary=")) > -1) {
                posStart = pos + 9;
                // reset the boundary to whatever the browser has told us
                // assume to either the next semi colon or the end of the string value
                if ((pos = contentType.indexOf(";", posStart)) > -1)
                    boundary = contentType.substring(posStart, pos);
                else
                    boundary = contentType.substring(posStart);
            }
            log.debug("boundary: " + boundary);
            // for each boundary look for a name= (field) or filename= (file) as the parameter name and then set the content as value
            int bpos1;  // tracks the boundary start position (note: may not be the start of the line)
            int bpos2;  // tracks the boundary end postion
            int ppos1;  // tracks the parameter start postion (note: used for both key and value)
            int ppos2;  // tracks the parameter end position
            int hpos;   // tracks the header suffix position
            int ctpos;  // tracks the content type position to test for text type if given
            int npos;   // tracks the newline position; used to figure out the next new line until we cross the current boundary
            int pnpos;  // tracks the previous newline position; used to figure out the previous newline before we cross the boundary
            boolean isFile = false;
            String paramName;
            String paramValue;
            String contentParamValue;
            String fileNameParamValue;
            bpos1 = bodyString.indexOf(boundary);
            while (bpos1 != -1) {
                paramName = "";
                contentParamValue = "";
                fileNameParamValue = "";
                bpos2 = bodyString.indexOf(boundary, bpos1 + boundary.length());
                hpos = bodyString.indexOf(HEADER_SUFFIX, bpos1);
                // look for any filename indicating this is a file (NOTE: must do this first since it can have
                //      a name= parameter as well)
                ppos1 = bodyString.indexOf(FILENAME_PREFIX, bpos1);
                if (ppos1 != -1 && (bpos2 == -1 || ppos1 <= hpos)) {
                    log.debug("found file name position: " + ppos1);
                    isFile = true;
                    // the content type value = the next ; or CRLF
                    npos = bodyString.indexOf(";", ppos1);
                    pnpos = -1;
                    if (npos == -1)
                        npos = bodyString.indexOf(CRLF, ppos1);
                    else
                        pnpos = bodyString.indexOf(CRLF, ppos1);
                    npos = (npos == -1 ? pnpos : npos);
                    if (pnpos != -1 && pnpos < npos)
                        npos = pnpos;
                    // NOTE: i am removing 1 extra character since the filename is wrapped in quotes
                    fileNameParamValue = bodyString.substring(ppos1 + FILENAME_PREFIX.length(), npos-1);
                    log.debug("file body filename: " + fileNameParamValue);

                    // we will store all files in a parameter called file
                    // we only care if our contentType is set with text/<something>
                    log.debug("file Content-Type position: " + bodyString.indexOf("Content-Type: "));
                    if ((ctpos = bodyString.indexOf("Content-Type: ")) != -1) {
                        ctpos += "Content-Type: ".length();
                        // the content type value = the next ; or CRLF
                        npos = bodyString.indexOf(";", ctpos);
                        pnpos = -1;
                        if (npos == -1)
                            npos = bodyString.indexOf(CRLF, ctpos);
                        else
                            pnpos = bodyString.indexOf(CRLF, ctpos);
                        npos = (npos == -1 ? pnpos : npos);
                        if (pnpos != -1 && pnpos < npos)
                            npos = pnpos;
                        contentParamValue = bodyString.substring(ctpos, npos);
                        log.debug("file body Content-Type: " + contentParamValue);
                        if (contentParamValue.length() >= 4 && contentParamValue.substring(0, 4).equalsIgnoreCase("text"))
                            paramName = "file";
                    }
                    else
                        paramName = "file";
                }
                else {
                    isFile = false;
                    ppos1 = bodyString.indexOf(FIELDNAME_PREFIX, bpos1);
                    // first deal with a name= where within boundaries
                    if (ppos1 != -1 && (bpos2 == -1 || ppos1 <= hpos)) {
                        // the parameter is good and in scope of boundary so lets set the parameter name to everything after the " to the next "
                        if ((ppos2 = bodyString.indexOf("\"", ppos1 + FIELDNAME_PREFIX.length())) > -1) {
                            log.debug("found field name: " + ppos2);
                            paramName = bodyString.substring(ppos1 + FIELDNAME_PREFIX.length(), ppos2);
                        }
                    }
                }
                if (paramName == null || paramName.length() == 0) {
                    log.debug("parameter name was not found in boundary; skipping to next boundary...");
                } else {
                    // the value should be the data following the next blank line (starts with /r/n x 2 to the next /r/n
                    ppos1 = hpos + HEADER_SUFFIX.length();
                    // track each newline before we hit bpos2 the last one before we cross is our endpoint;
                    npos = bodyString.indexOf(CRLF, ppos1);
                    pnpos = -1;
                    while (npos != -1 && npos < bpos2) {
                        pnpos = npos;
                        npos = bodyString.indexOf(CRLF, npos + CRLF.length());
                    }
                    ppos2 = (pnpos == -1 ? bpos2 : pnpos);
                    if (ppos1 != -1 && ppos2 != -1)
                        paramValue = bodyString.substring(ppos1, ppos2);
                    else
                        paramValue = bodyString.substring(ppos1);
                    // save off the parameter
                    log.debug("found [" + paramName + "]: [" + paramValue + "]");
                    addParameter(paramName, paramValue);
                    if (isFile) {
                        // add our filename and content type for this file (should always be at the same location)
                        addParameter("file.name", fileNameParamValue);
                        addParameter("file.contenttype", contentParamValue);
                    }
                }

                // set up our new boundary limits for the next pass
                bpos1 = bpos2;
            }

        } else {
            // all remaining content types (assume urlencoded utf8 string format
            if (bodyString != null && bodyString.length() > 0)
                parseUrlEncodedParameters(bodyString);
            else
                log.debug("no body parameter string found.");
        }
    }

    public LinkedHashMap<String, String[]> getParameterMap() {
        return parameters;
    }

    // parses a string into Map<string, string[]> for use later in getParameterMap()
    private void parseUrlEncodedParameters(String encodedParameterString) {
        String paramStrings[] = encodedParameterString.split("\\&");
        String key; String value;
        for (String paramString : paramStrings) {
            int pos = paramString.indexOf("=");
            log.debug("index of = in [" + paramString + "]: " + pos);
            if (pos == -1)
                log.error("Found a parameter without an equals sign.  This shouldn't happen; skipping...");
            else if (pos == 0)
                log.error("Found a parameter without a key value.  This shouldn't happen; skipping...");
            else if (pos >= 1) {
                // we have at least a key so lets save it off for insertion etc.
                try {key = URLDecoder.decode(paramString.substring(0, pos), "UTF-8");}
                catch (UnsupportedEncodingException uex) {
                    log.fatal("Exception attempting to url decode key parameter [" + paramString.substring(0, pos) + "]: " + uex);
                    key = "";
                }
                log.debug("found key [" + key + "] in [" + paramString + "]");
                if (paramString.length() <= pos + 1) {
                    log.debug("Found a missing value; setting value to empty string...");
                    value = "";
                }
                else {
                    try {value = URLDecoder.decode(paramString.substring(pos + 1), "UTF-8");}
                    catch (UnsupportedEncodingException uex) {
                        log.fatal("Exception attempting to url decode value parameter [" + paramString.substring(pos + 1) + "]: " + uex);
                        value = "";
                    }
                }
                log.debug("found value [" + value + "] in [" + paramString + "]");
                addParameter(key, value);
            }
        }
    }

    // add the parameter to the map
    private void addParameter(String key, String value) {
        // make sure we have a key; if so and we have the parameter already then add an additional string array value; else add it
        if (key.equalsIgnoreCase(""))
            log.debug("key was not passed; skipping...");
        else {
            if (parameters.containsKey(key)) {
                String[] values = parameters.get(key);
                log.debug("original values: " + Arrays.toString(values));
                values = append(values, value);
                log.debug("new values: " + Arrays.toString(values));
                parameters.put(key, values);
            } else
                parameters.put(key, new String[]{value});
        }
    }

    // copies an existing array 1 bigger then adds the element (string in this case)
    private static <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }

    public static void main (String[] args) {
        BasicConfigurator.configure();
//        String contentType = "multipart/form-data; boundary=----WebKitFormBoundarycnF7xTfe8gqBnj4n";
//        String queryString = "";
//        String bodyString = "------WebKitFormBoundarycnF7xTfe8gqBnj4n\r\n" +
//                "Content-Disposition: form-data; name: \"file\"; filename=\"configurations (1).xml\"\r\n" +
//                "Content-Type: image/xml\r\n" +
//                "\r\n" +
//                "<configurations><configuration><connectionName>test2</connectionName><path>/test2</path><querySql>select * from test2</querySql><insertSql>insert into test2 values (?)</insertSql><updateSql></updateSql><deleteSql></deleteSql><keywords>test</keywords></configurations><configuration><connectionName>test2</connectionName><path>/test3</path><querySql>select * from test2</querySql><insertSql>insert into test2 values (?)</insertSql><updateSql></updateSql><deleteSql></deleteSql><keywords>test3</keywords></configurations><configuration><connectionName>test2</connectionName><path>/test4</path><querySql>select * from test2</querySql><insertSql>insert into test2 values (?)</insertSql><updateSql></updateSql><deleteSql></deleteSql><keywords>test4</keywords></configurations><configuration><connectionName>default</connectionName><path>/connections</path><querySql>SELECT * FROM CONNECTIONS</querySql><insertSql>INSERT INTO CONNECTIONS (NAME, TYPE, JNDI_NAME, JNDI_CONTEXT, JDBC_DRIVER, JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)</insertSql><updateSql>UPDATE CONNECTIONS SET NAME=?, TYPE=?, JNDI_NAME=?, JNDI_CONTEXT=?, JDBC_DRIVER=?, JDBC_URL=?, JDBC_USERNAME=?, JDBC_PASSWORD=?, DESCRIPTION=? WHERE NAME=?</updateSql><deleteSql>DELETE FROM CONNECTIONS WHERE NAME=?</deleteSql><keywords>system, product:console</keywords></configurations><configuration><connectionName>default</connectionName><path>/configurations</path><querySql>SELECT * FROM CONFIGURATIONS</querySql><insertSql>INSERT INTO CONFIGURATIONS (CONNECTION_NAME, PATH, QUERY_STATEMENT, INSERT_STATEMENT, UPDATE_STATEMENT, DELETE_STATEMENT, KEYWORDS) VALUES (?, ?, ?, ?, ?, ?, ?)</insertSql><updateSql>UPDATE CONFIGURATIONS SET CONNECTION_NAME=?, PATH=?, QUERY_STATEMENT=?, INSERT_STATEMENT=?, UPDATE_STATEMENT=?, DELETE_STATEMENT=?, KEYWORDS=? WHERE CONFIGURATION_ID=?</updateSql><deleteSql>DELETE FROM CONFIGURATIONS WHERE CONFIGURATION_ID=?</deleteSql><keywords>system, product:console</keywords></configurations></configurations>\r\n" +
//                "------WebKitFormBoundarycnF7xTfe8gqBnj4n\r\n" +
//                "Content-Disposition: form-data; name=\"action\"\r\n" +
//                "\r\n" +
//                "Upload\r\n" +
//                "------WebKitFormBoundarycnF7xTfe8gqBnj4n--";
        String contentType = "application/x-www-form-urlencoded; charset=UTF-8";
        String queryString = "";
        String bodyString = "name=%2Ftest2&type=jdbc&driver=org.h2.Driver&url=jdbc%3Ah2%3A~%2FdbServices%2Fds2&login=dsadmin&password=dsadmin&jndi-name=&jndi-context=";
        OrderedParameterWrapper wrapper = new OrderedParameterWrapper(contentType, queryString, bodyString);
        Map<String, String[]> parameterMap = wrapper.getParameterMap();
        Set<Map.Entry<String, String[]>> parameterSet = parameterMap.entrySet();
        for (Map.Entry<String, String[]> parameterEntry : parameterSet)
            System.out.println(parameterEntry.getKey() + ": " + Arrays.toString(parameterEntry.getValue()));

    }
}
