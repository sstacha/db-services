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

import org.apache.log4j.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.*;

/**
 * User: sstacha
 * Date: Mar 5, 2013
 * I am attempting to ensure the parameters are ordered so I can access them later in the correct order
 *  since I intend to do parameter replacements 1-n.
 */
public class OrderedParameterRequestWrapper extends HttpServletRequestWrapper {
    private byte[] raw;
    private LinkedHashMap<String, String[]> parameterMap = new LinkedHashMap<String, String[]>();

    private static final int MAX_SIZE = 1024 * 1024 * 4;  // 1024B = 1KB * 1024KB = 1MB
    public static Logger log = Logger.getLogger(OrderedParameterRequestWrapper.class);

    public OrderedParameterRequestWrapper(HttpServletRequest request) {
        super(request);

        try {
            // read the request body and save it as a byte array
            InputStream in = super.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte buffer[] = new byte[1024];
            int total = 0;
            for(int s; ((s=in.read(buffer)) != -1 && total < MAX_SIZE); )
            {
              bos.write(buffer, 0, s);
              total += s;
              if (total > MAX_SIZE)
                  log.fatal("Input stream exceeded max size [" + MAX_SIZE + "]bytes.  Terminating...");
            }
            bos.flush();
            raw = bos.toByteArray();
            // next parse the strings and put into parameter map in order
            String body = toString();
            String qs = request.getQueryString();
            log.debug("body: " + body);
            log.debug("");
            log.debug("headers");
            log.debug("-------");
            // Print all headers
            List<String> headers = Collections.list(request.getHeaderNames());
            for (String header : headers) {
                log.debug(header + " - " + request.getHeader(header));
            }
            log.debug("qs: " + qs);
            log.debug("parameter body value: " + body);
            log.debug("content type header: " + request.getHeader("content-type"));
            OrderedParameterWrapper parameterWrapper = new OrderedParameterWrapper(request.getHeader("content-type"), qs, body);
            parameterMap = parameterWrapper.getParameterMap();
        }
        catch (IOException ioex) {log.fatal("Exception in request wrapper: " + ioex);}
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStreamImpl(new ByteArrayInputStream(raw));
    }

    @Override
    public BufferedReader getReader() throws IOException {
        String enc = getCharacterEncoding();
        if(enc == null) enc = "UTF-8";
        return new BufferedReader(new InputStreamReader(getInputStream(), enc));
    }

    private class ServletInputStreamImpl extends ServletInputStream {

        private InputStream is;

        public ServletInputStreamImpl(InputStream is) {
            this.is = is;
        }

        public int read() throws IOException {
            return is.read();
        }

        public boolean markSupported() {
            return false;
        }

        public synchronized void mark(int i) {
            throw new RuntimeException(new IOException("mark/reset not supported"));
        }

        public synchronized void reset() throws IOException {
            throw new IOException("mark/reset not supported");
        }
    }

    // allows user to get the raw bytes from the input stream if needed for evaluation or to save a image etc.
    public byte[] getBytes() { return raw; }

    @Override
    public String toString() {
        if (raw == null || raw.length == 0)
            return "";
        String enc = getCharacterEncoding();
        if(enc == null) enc = "UTF-8";
        try {return new String(raw, enc);}
        catch (UnsupportedEncodingException uex) {log.fatal("Exception attempting to convert byte array to string: " + uex);}
        return "";
    }

    @Override
    public String getParameter(String name) {
        if (name == null || name.length() == 0)
            return null;
        // return the first parameter we find
        String[] values = parameterMap.get(name);
        if (values == null || values.length == 0)
            return null;
        return values[0];
    }

    @Override
    public Map getParameterMap() {
        return parameterMap;
    }

    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }
}
