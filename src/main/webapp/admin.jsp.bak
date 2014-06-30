<%@ page language="java"%>
<%@ page contentType="text/html; charset=ISO-8859-1"%>
<%@ page buffer="20kb"%>

<%@ page import="srv.Server"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<title>Server Admin | Team GOA</title>
<link rel="stylesheet" type="text/css" href="bootstrap.css">

<%! private String pass = ""; %>

</head>

<body>
	<h1>Admin Panel</h1>

	<p>
		Server view: <a href="http://tomcat-teamgoa.rhcloud.com/view.jsp">click here</a>
	</p>

	<section class='col-xs-12 col-sm-6 col-md-6'>

	<p>Modify allowed versions:</p>

	<form action="admin" method="GET">
		<table border="0" style="width: 300px">

			<tr>
				<td>Version to add:</td>
				<td><input type="text" name="addver"></td>
				<td><input type="submit" value="Add" /></td>
			</tr>

			<tr>
				<td>Version to remove:</td>
				<td><input type="text" name="remver"></td>
				<td><input type="submit" value="Remove" /></td>
			</tr>
		</table>

		<%
			out.println("<input type=\"hidden\" name=\"pass\" value=\""
					+ ((request.getParameter("pass") != null) ? request
							.getParameter("pass") : "") + "\" />");
		%>
	</form>

	<br>
	<br>

	<p>Allowed versions:</p>
	
	<%
		synchronized (Server.getPermittedVersions()) {
			if (Server.getPermittedVersions() != null) {
				for (int i = 0; i < Server.getPermittedVersions().size(); i++) {
					out.println(Server.getPermittedVersions().get(i));
					out.println("<br>");
				}
			}
		}
	%>
	
	</section>

	<section class='col-xs-12 col-sm-6 col-md-6'>
	
	<p>Modify high scores:</p>

	<form action="admin" method="GET">
		Name: <input type="text" name="addname"><br>
		Score: <input type="text" name="addscore">
		<%
			out.println("<input type=\"hidden\" name=\"pass\" value=\""
					+ ((request.getParameter("pass") != null) ? request
							.getParameter("pass") : "") + "\" />");
		%>
		<input type="submit" value="Add" />
	</form>

	<br>
	<br>
	
	<p>High scores:</p>

	<table border="0" style="width: 300px">

		<%
			synchronized (Server.getHighScores()) {
				if (Server.getHighScores() != null) {
					for (Long score : Server.getHighScores().descendingKeySet()) {
						for (String name : Server.getHighScores().get(score)) {
							out.println("<tr>");
							out.println("<td>");
							out.print(name);
							out.println("</td>");
							out.println("<td>");
							out.print(score);
							out.println("</td>");
							out.println("<td>");
							out.println("<form action=\"admin\" method=\"GET\">");
							out.println("<input type=\"hidden\" name=\"remscore\""
									+ "value=\"" + name + "#" + score + "\" />");
							out.println("<input type=\"hidden\" name=\"pass\""
									+ "value=\"" + ((request.getParameter("pass")
											!= null) ? request.getParameter("pass")
													: "") + "\" />");
							out.println("<input type=\"submit\""
									+ "value=\"Remove\" />");
							out.println("</form>");
							out.println("</tr>");
						}
					}
				}
			}
		%>

	</table>
	
	<footer>
		<form action="admin.jsp" method="GET">
			<input type="text" name="pass">
			<input type="submit" value="Enter" />
		</form>
	</footer>

	</section>

</body>
</html>