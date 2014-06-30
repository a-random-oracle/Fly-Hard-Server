<%@ page language="java"%>
<%@ page contentType="text/html; charset=ISO-8859-1"%>
<%@ page buffer="20kb"%>

<%@ page import="srv.Server"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<title>Server View Page | Team GOA</title>
<link rel="stylesheet" type="text/css" href="bootstrap.css">
</head>

<body>
	<h1>Welcome to the Tomcat server</h1>

	<p>
		Reset: <a href="http://tomcat-teamgoa.rhcloud.com/reset">click here</a>
	</p>

	<section class='col-xs-12 col-sm-6 col-md-6'>
	
	<p>Current players:</p>
	
	<table border="0" style="width: 400px">

		<%
			synchronized (Server.getClients()) {
				for (int i = 0; i < Server.getClients().size(); i++) {
					if (Server.getClients().get(i).getName() != null) {
						out.print("<tr>");
						out.print("<td>");
						out.print(Server.getClients().get(i).getName());
						out.print("</td>");
						out.print("<td>");
						if (Server.getClients().get(i).isHost()) {
							out.print("HOST");
						}
						out.print("</td>");
						out.print("</tr>");
					}
				}
			}
		%>

	</table>

	</section>
	
	<section class='col-xs-12 col-sm-6 col-md-6'>

		<p>Output:</p>
		
		<%
			synchronized (Server.getSysout()) {
				for (int i = 0; i < Server.getSysout().size(); i++) {
					out.print(Server.getSysout().get(i));
					out.print("<br>");
				}
			}
		%>
		
	</section>

</body>
</html>