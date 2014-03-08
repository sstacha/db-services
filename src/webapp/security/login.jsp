<%--
  Created by IntelliJ IDEA.
  User: stevestacha
  Date: Jan 3, 2010
  Time: 4:58:11 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="model" value="${requestScope.swafModel}" />
<html>
  <head><title>Simple jsp page</title></head>
  <body>
  <form id="app" name="app" action="display.jsp" method="get" >
	  <%--<p><strong>Bold</strong> fields are required.</p>--%>
	  <%-- HIDDEN FORM FIELDS FOR BACKEND IDS --%>
	  <fieldset>
		  <legend>Personal Information</legend>
		  <div class="optional">
			  <label for="login">user name: </label>
			  <input type="text" name="login" id="login" value="${model.login}"/>
		  </div>
		  <div class="optional">
			  <label for="pwd">password: </label>
			  <input type="text" name="pwd" id="pwd" value="${model.password}">
		  </div>
          <input type="submit" value="Submit">
	  </fieldset>
  </form>
  </body>
  <a href="<%=request.getContextPath()%>/util/config.do">Print Config</a>
</html>