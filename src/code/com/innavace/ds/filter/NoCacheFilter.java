package com.innavace.ds.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: sstacha
 * Date: 6/20/13
 * Time: 1:46 PM
 * I am having issues with IE caching my json return data.  I read where server side headers may solve the problem
 * since telling the angularjs http service not to cache does not work on the client side for IE
 */
public class NoCacheFilter implements Filter {


    public void init(FilterConfig config) throws ServletException { }
    public void destroy() { }

    public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpresponse = (HttpServletResponse)response ;
                // Set the Cache-Control and Expires header
                httpresponse.setHeader("Cache-Control", "no-cache") ;
                httpresponse.setHeader("Expires", "0") ;
                // Print out the URL we're filtering
                String name = ((HttpServletRequest)request).getRequestURI();
                System.out.println("No Cache Filtering: " + name) ;
            }
            chain.doFilter (request, response);
        } catch (IOException e) {
            System.out.println ("IOException in NoCacheFilter");
            e.printStackTrace() ;
        } catch (ServletException e) {
            System.out.println ("ServletException in NoCacheFilter");
            e.printStackTrace() ;
        }
    }
}

