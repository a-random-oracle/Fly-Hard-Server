package srv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The servlet responsible for handling data.
 * <p>
 * This servlet is connected to by clients when they wish to
 * pass data to another client.
 * </p>
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

	/** The serialisation identifier */
	private static final long serialVersionUID = 2L;
	
	/** The log file */
	private static final File LOG_FILE = new File("datalog.txt");
	

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
	 * Adds POST data to the data buffer, and replies with remaining data.
	 * @param request - the HTTP POST request received
	 * @param response - the response to send
	 */
	@SuppressWarnings("unchecked")
	public void playerPost(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			// Set the connection's input stream
			ServletInputStream srvInputStream = request.getInputStream();
			ObjectInputStream inputStream = null;
			try {
				inputStream = new ObjectInputStream(srvInputStream);
			} catch (IOException | NullPointerException e) {
				Server.print(e);
				return;
			}

			// Set the connection's output stream
			ServletOutputStream srvOutputStream = response.getOutputStream();
			ObjectOutputStream outputStream = null;
			try {
				outputStream = new ObjectOutputStream(srvOutputStream);
			} catch (IOException | NullPointerException e) {
				Server.print(e);
				return;
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

			// Get the data from the connection's input stream
			Entry<Long, byte[]> receivedDataEntry = null;
			try {
				receivedDataEntry = (Entry<Long, byte[]>) inputStream.readObject();
			} catch (ClassNotFoundException | ClassCastException | IOException e) {
				Server.print(e);
			}

			if (client != null) {
				// Add client information headers
				response.setHeader("fh-client-id",
						String.valueOf(client.getID()));
				response.setHeader("fh-client-messages",
						client.readMessages());
			} else {
				response.setHeader("fh-client-id",
						String.valueOf(-1));
			}

			// Check that the data is not null, that the client exists, and that
			// the client has a partner
			if ((client != null) && (client.getPartner() != null)
					&& (receivedDataEntry != null)) {
				// Add the data to the client's partner's data buffer
				client.getPartner().writeData(receivedDataEntry);

				Server.print("Added data to client "
						+ client.getPartner().getID() + "'s queue");

				try {
					// Reply with the next object in the client's data buffer
					Entry<Long, byte[]> latestData = client.readLatestData();
					outputStream.writeObject(latestData);
					Server.print("Sending data to client " + client.getID());
					
					// Add this data to the log
					if (latestData != null && latestData.getValue() != null) {
						FileOutputStream logStream = new FileOutputStream(LOG_FILE, true);
						try {
							logStream.write((System.currentTimeMillis()
									+ "-" + client.getID() + ":").getBytes());
							logStream.write(latestData.getValue());
							logStream.write(("\n").getBytes());
						} finally {
							logStream.close();
						}
					}
				} catch (IOException e) {
					Server.print(e);
				}
			}
		} catch (Exception e) {
			Server.print(e);
		}
	}

}