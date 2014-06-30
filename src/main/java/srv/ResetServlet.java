package srv;

import java.io.IOException;
 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/**
 * The servlet responsible for clearing the server.
 * <p>
 * The servlet will then redirect the user to whichever page
 * the request originated at.
 * </p>
 */
@WebServlet("/reset")
public class ResetServlet extends HttpServlet {
	
	/** The serialisation identifier */
	private static final long serialVersionUID = 3L;
	
	/** The URL to redirect to */
	private static final String redirectURL =
			"http://tomcat-teamgoa.rhcloud.com/view.jsp";

	/**
	 * Respond to HTTP GET requests.
	 * <p>
	 * Server data will be cleared, then the requester will be redirected
	 * to the server view page.
	 * </p>
	 * @param request - the HTTP GET request received
	 * @param response - the response to send
	 */
	protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
		// Reset the server
		Server.reset();
		
		// Reset the mock servlet
		//AdminServlet.reset();
		
		// Redirect the client to the server view page
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_FOUND);
		response.setHeader("Location", redirectURL);
	}
	
}