<%@ page import="java.util.Enumeration" %>
<%--
  Created by IntelliJ IDEA.
  User: stevestacha
  Date: Feb 23, 2010
  Time: 6:12:28 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Simple jsp page</title></head>
  <body>
  <%
    Enumeration headerNames = request.getHeaderNames();
    String headerName;
    out.println("---------- headers ----------<br>");
    while (headerNames.hasMoreElements())
    {
        headerName = (String)headerNames.nextElement();
        out.println("     " + headerName + ": " + request.getHeader(headerName) + "<br>");
    }
    Enumeration parameterNames = request.getParameterNames();
    String parameterName;
    out.println("---------- parameters ---------<br>");
    while (parameterNames.hasMoreElements())
    {
        parameterName = (String)parameterNames.nextElement();
        out.println("     " + parameterName + ": " + request.getParameter(parameterName) + "<br>");
    }
    out.println("------------------------------<br>");

    Enumeration attibuteNames = request.getAttributeNames();
    String attributeName;
    out.println("---------- attributes  ---------<br>");
    while (attibuteNames.hasMoreElements())
    {
        attributeName = (String)attibuteNames.nextElement();
        out.println("     " + attributeName + ": " + request.getAttribute(attributeName) + "<br>");
    }
    out.println("------------------------------<br>");

%>
  </body>
</html>