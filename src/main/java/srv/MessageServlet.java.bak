package srv;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The servlet responsible for handling messages.
 * <p>
 * Messages are also referred to as instructions.
 * </p>
 * <p>
 * This servlet is connected to by clients when they wish to
 * send a message to the server.
 * </p>
 * <p>
 * Messages will be processed as instructions by the server
 * (with the effect of the instruction occurring on the server)
 * unless SEND instruction is used (in which case the message
 * will be passed to another client).
 * </p>
 */
@WebServlet("/msg")
public class MessageServlet extends HttpServlet {

	/** The serialisation identifier */
	private static final long serialVersionUID = 1L;

	
	/**
	 * Respond to HTTP GET requests.
	 * <p>
	 * The requester will be redirected to the 404.jsp page.
	 * </p>
	 * @param request - the HTTP GET request received
	 * @param response - the response to send
	 */
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			Server.print(e);
		}
	}

	/**
	 * Respond to HTTP POST requests.
	 * @param request - the HTTP POST request received
	 * @param response - the response to send
	 */
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response) {
		// Obtain a lock on the permitted versions array
		synchronized (Server.getPermittedVersions()) {
			if (Server.getPermittedVersions()
					.contains(request.getHeader("user-agent"))) {
				playerPost(request, response);
			} else {
				standardPost(request, response);
			}
		}
	}
	
	/**
	 * Replies with a 404 (page not found) status code.
	 * @param request - the HTTP POST request received
	 * @param response - the response to send
	 */
	public void standardPost(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			Server.print(e);
		}
	}
	
	/**
	 * Handles a POST message.
	 * @param request - the HTTP POST request received
	 * @param response - the response to send
	 */
	public void playerPost(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			// Set the connection's input stream
			ServletInputStream srvInputStream = request.getInputStream();
			if (srvInputStream == null) {
				Exception e = new Exception("Servlet input stream is null");
				Server.print(e);
				return;
			}
			
			ObjectInputStream inputStream = null;
			try {
				inputStream = new ObjectInputStream(srvInputStream);
			} catch (IOException | NullPointerException e) {
				Server.print(e);
				return;
			}

			// Set the connection's output stream
			ServletOutputStream srvOutputStream = response.getOutputStream();
			if (srvOutputStream == null) {
				Exception e = new Exception("Servlet output stream is null");
				Server.print(e);
				return;
			}
			
			ObjectOutputStream outputStream = null;
			try {
				outputStream = new ObjectOutputStream(srvOutputStream);
			} catch (IOException | NullPointerException e) {
				Server.print(e);
				return;
			}

			// Get the data from the connection's input stream
			Object receivedData = null;
			try {
				receivedData = inputStream.readObject();
			} catch (ClassNotFoundException | IOException e) {
				Server.print(e);
			}

			// Get the message content
			String instruction = null;
			if (receivedData != null && receivedData instanceof String) {
				// Get the received instruction
				instruction = (String) receivedData;
			}

			// Determine the client's ID
			long id = -1;
			try {
				id = Long.parseLong(request.getHeader("fh-client-id"));
			} catch (NumberFormatException e) {
				Server.print(e);
			}

			// Determine the client's name
			String name = request.getHeader("fh-client-name");

			// Determine whether the client is a host
			boolean isHost = false;
			if (request.getHeader("fh-client-host") != null) {
				isHost = request.getHeader("fh-client-host")
						.contains("true");
			}
			
			// Determine the client's lives
			int lives = -1;
			try {
				lives = Integer.parseInt(request.getHeader("fh-client-lives"));
			} catch (NumberFormatException e) {
				Server.print(e);
			}
			
			// Determine the client's score
			int score = 0;
			try {
				score = Integer.parseInt(request.getHeader("fh-client-score"));
			} catch (NumberFormatException e) {
				Server.print(e);
			}

			// Get the client from the header fields supplied
			Client client = Server.handleClient(id, name, isHost, lives, score);

			if (instruction != null && !instruction.equals("")) {
				if (client == null) {
					Server.print("Client NULL "
							+ " sent instruction: " + instruction);
				} else {
					Server.print("Client " + client.getID()
							+ " sent instruction: " + instruction);
				}
			}

			// Handle the message
			String responseMessage = InstructionHandler
					.handleInstruction(client, instruction);

			if (client != null) {
				if (client.checkForMessages()) {
					// Add any messages in the client's message string
					responseMessage += InstructionHandler.LIST_DELIM;
					responseMessage += client.readMessages();
				}

				// Add client information headers
				response.setHeader("fh-client-id",
						String.valueOf(client.getID()));
			} else {
				response.setHeader("fh-client-id",
						String.valueOf(-1));
			}

			// Send a response

			// If the response so far is null, respond with a message indicating
			// that the server received an invalid message
			if (responseMessage.equals("")) {
				responseMessage = "INVALID_REQUEST";
			}

			try {
				outputStream.writeObject(responseMessage);

				if (instruction != null && !instruction.equals("")) {
					if (client == null) {
						Server.print("Sent response: " + responseMessage
								+ " to client NULL");
					} else {
						Server.print("Sent response: " + responseMessage
								+ " to client " + client.getID());
					}
				}
			} catch (IOException e) {
				Server.print(e);
			}
		} catch (Exception e) {
			Server.print(e);
		}
	}
	
}