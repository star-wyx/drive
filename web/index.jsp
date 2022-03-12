<%--
  Created by IntelliJ IDEA.
  User: star_wyx
  Date: 3/11/22
  Time: 12:03
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<h1>
    HELLO WORLD!!!
</h1>
    <c:forEach items="${list}" var="user">
        ${user.userId}--${user.userName}
    </c:forEach>

</body>
</html>
