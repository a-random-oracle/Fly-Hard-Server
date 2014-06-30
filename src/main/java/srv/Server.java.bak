package srv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * An abstract class representing the server's functionality.
 * <p>
 * The server manages connections (through the client array),
 * and contains the array of strings to be printed to the server
 * view page.
 * </p>
 */
public abstract class Server {
	
	/** The date format to use */
	public static SimpleDateFormat dateFormat =
			new SimpleDateFormat("yy/MM/dd HH:mm:ss:SSS", Locale.UK);
	
	/** The maximum number of clients to allow connections from */
	public static int maxClients = 30;
	
	/** The maximum number of connections to allow */
	public static int maxConnections = 15;
	
	public static int timeout = 5000;
	
	public static ArrayList<String> permittedVersions = new ArrayList<String>();
	
	/** The timer which is used to close inactive client connections */
	private static Timer removeClientsTimer = null;
	
	/** The next client ID to issue */
	private static long nextClientID = 0;

	/** The clients currently connected to the server */
	private static ArrayList<Client> clients = new ArrayList<Client>();
	
	/** The list of high scores */
	private static TreeMap<Long, ArrayList<String>> highScores =
			new TreeMap<Long, ArrayList<String>>();
	
	/** The array of text to output to a server viewer */
	private static ArrayList<String> sysout = new ArrayList<String>();
	
	/** Whether to output data to the standard output */
	private static boolean verbose = true;
	
	/** Whether to output the date and time to the standard output */
	private static boolean printDateTime = true;
	
	
	/**
	 * Checks if a remove client timer is present.
	 * @return <code>true</code> if a remove clients time is active,
	 * 			otherwise <code>false</code>
	 */
	public static boolean isRemoveClientsTimerPresent() {
		return removeClientsTimer != null;
	}
	
	/**
	 * Starts the remove clients timer.
	 */
	public static void startRemoveClientsTimer() {
		clearRemoveClientsTimer();
		
		removeClientsTimer = new Timer();
		
		removeClientsTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				runRemoveClientsTimer();
			}
		}, timeout - 1, timeout);
	}
	
	/**
	 * The remove clients timer periodically removes any inactive clients.
	 * <p>
	 * A client is inactive if they haven't connected to the server since the
	 * last timeout occurred.
	 * </p>
	 */
	private static void runRemoveClientsTimer() {
		print("TIMEOUT CHECK");
		
		synchronized (clients) {
			// Loop through the client array
			for (int i = clients.size() - 1; i >= 0; i--) {
				Client client = clients.get(i);
				
				// Check if the client has connected since the
				// last timeout occurred
				print("Client " + client.getID() + " last connected "
						+ (System.currentTimeMillis()
								- client.getLastConnection())
						+ " milliseconds ago");
				if (System.currentTimeMillis()
						- client.getLastConnection() > timeout) {
					// If so, remove the client
					print("Removing client " + client.getID()
							+ " (TIMEOUT)");
					removeClient(client, "END_GAME");
				}
			}
		}
	}
	
	public static void clearRemoveClientsTimer() {
		if (removeClientsTimer != null) {
			// Obtain a lock on the remove clients timer
			synchronized (removeClientsTimer) {
				removeClientsTimer.cancel();
				removeClientsTimer = null;
			}
		}
	}
	
	
	/**
	 * Gets the client array.
	 * @return the client array
	 */
	public static ArrayList<Client> getClients() {
		// Obtain a lock on the client array
		synchronized (clients) {
			return clients;
		}
	}
	
	/**
	 * Gets a client from their ID.
	 * @param id - the id to search for
	 * @return the client with the specified id
	 */
	public static Client getClientFromID(long id) {
		// Obtain a lock on the client array
		synchronized (clients) {
			// Loop through each client
			for (Client client : clients) {
				// If the client's ID matches the specified ID then
				// return the client
				if (client != null && client.getID() == id) {
					return client;
				}
			}

			return null;
		}
	}
	
	/**
	 * Gets a new client ID.
	 * @return a new client ID
	 */
	public static long getNewClientID() {
		long id = -1;
		
		// Obtain a lock on the client array
		synchronized (clients) {
			// Get the next available client ID
			id = nextClientID;
			
			// Increment the next client ID
			nextClientID++;
		}
		
		return id;
	}
	
	public static Client handleClient(long id, String name,
			boolean isHost, int lives, int score) {
		Client client = null;
		
		// Obtain a lock on the client array
		synchronized (clients) {
			if (id == -1) {
				// If the client's ID is -1, this is a new request
				client = addClient(name, isHost);
			} else {
				// Get the client from their ID
				client = getClientFromID(id);

				if (client != null) {
					// Update the time at which the client last connected
					client.updateLastConnectionTime();

					// Update the client's name
					client.setName(name);

					// Update the client's host status
					client.setHost(isHost);
					
					// Update the client's lives
					client.setLives(lives);
					
					// Update the client's score
					client.setScore(score);
				}
			}
		}

		return client;
	}
	
	/**
	 * Adds a client to the client array.
	 * @param name - the client's name
	 * @param isHost - <code>true</code> if the client is a host,
	 * 					otherwise <code>false</code>
	 */
	private static Client addClient(String name, boolean isHost) {
		// Obtain a lock on the client array
		synchronized (clients) {
			// Check that the client limit hasn't been reached
			if (clients.size() < maxClients) {
				// Construct the new client
				Client newClient = new Client();

				// Set the new client's name
				newClient.setName(name);

				// Set whether the new client is a host
				newClient.setHost(isHost);

				// Add the client to the client array
				clients.add(newClient);
				
				return newClient;
			}
		}
		
		return null;
	}
	
	/**
	 * Removes the specified client.
	 * @param clientToRemove - the client to remove
	 * @param message - the message to send to the client's partner
	 */
	public static void removeClient(Client clientToRemove, String message) {
		// Obtain a lock on the client array
		synchronized (clients) {
			// Clear up the connection
			if (clientToRemove.getPartner() != null) {
				clientToRemove.getPartner().writeMessage(message);
				clientToRemove.setPartner(null);
			}
			
			// Set up an iterator for the client array
			Iterator<Client> clientIterator = clients.iterator();
			
			// Iterate through the client list
			while (clientIterator.hasNext()) {
				// Progress the iterator
				Client client = clientIterator.next();
				
				// Check if the client iterator is pointing at
				// the client to remove
				if (client.equals(clientToRemove)) {
					clientIterator.remove();
				}
			}
			
			// If the client array is now empty, clear the remove client timer
			if (clients.size() == 0) {
				print("Clearing timeout timer.");
				clearRemoveClientsTimer();
			}
		}
	}
	
	/**
	 * Gets a list of available hosts.
	 * @return a list of available hosts
	 */
	private static ArrayList<Client> getAvailableHosts() {
		ArrayList<Client> hosts = new ArrayList<Client>();

		// Obtain a lock on the client array
		synchronized (clients) {
			// Find the clients which are searching for partners
			for (Client client : Server.getClients()) {
				if (client != null && (client.isHost())
						&& (client.getPartner() == null)) {
					hosts.add(client);
				}
			}

			return hosts;
		}
	}
	
	/**
	 * Gets a collapsed list of available hosts.
	 * <p>
	 * The list will be collapsed to the form:
	 * id1:name1#id2=name2 ...
	 * </p>
	 * @param limit - the number of records to return
	 * @param callingClient - the client requesting the list of available
	 * 							hosts - this client will not be included
	 * 							in the list
	 * @return the (collapsed) list of hosts
	 */
	public static String collapseAvailableHosts(int limit, Client callingClient) {
		// Obtain a lock on the client array
		synchronized (clients) {
			String collapsedAvailableHosts = "";
			
			int i = 0;
			for (Client client : Server.getAvailableHosts()) {
				if (client != null && !client.equals(callingClient)) {
					collapsedAvailableHosts += client.getID()
							+ "=" + client.getName() + "#";
					i++;
				}
				
				if (i >= limit) break;
			}
			
			return collapsedAvailableHosts;
		}
	}
	
	
	/**
	 * Gets the list of permitted versions.
	 * @return the list of permitted versions
	 */
	public static ArrayList<String> getPermittedVersions() {
		// Obtain a lock on the permitted versions array
		synchronized (permittedVersions) {
			if (!permittedVersions.contains("Fly-Hard-0.6")) {
				permittedVersions.add("Fly-Hard-0.6");
			}
			
			return permittedVersions;
		}
	}
	
	/**
	 * Adds a version to the list of permitted versions.
	 * @param version - the version to add
	 */
	public static void addVersion(String version) {
		// Obtain a lock on the permitted versions array
		synchronized (permittedVersions) {
			permittedVersions.add(version);
		}
	}
	
	/**
	 * Removes a version from the list of permitted versions.
	 * @param version - the version to remove
	 */
	public static void removeVersion(String version) {
		// Obtain a lock on the permitted versions array
		synchronized (permittedVersions) {
			for (int i = permittedVersions.size() - 1; i >= 0; i--) {
				if (version != null
						&& version.equals(permittedVersions.get(i))) {
					permittedVersions.remove(i);
				}
			}
		}
	}
	
	/**
	 * Clears the list of permitted versions.
	 */
	public static void clearPermittedVersions() {
		// Obtain a lock on the permitted versions array
		synchronized (permittedVersions) {
			permittedVersions = new ArrayList<String>();
		}
	}
	
	
	/**
	 * Gets the list of high scores.
	 * @return the list of high scores
	 */
	public static TreeMap<Long, ArrayList<String>> getHighScores() {
		// Obtain a lock on the list of high scores
		synchronized (highScores) {
			return highScores;
		}
	}
	
	/**
	 * Gets a collapsed list of high scores.
	 * <p>
	 * The list will be collapsed to the form:
	 * name1=score1#name2=score2 ...
	 * </p>
	 * @param limit - the number of records to return
	 * @return the (collapsed) list of high scores
	 */
	public static String collapseHighScores(int limit) {
		// Obtain a lock on the list of high scores
		synchronized (highScores) {
			String collapsedHighScores = "";
			
			int i = 0;
			for (Long key : highScores.descendingKeySet()) {
				for (String name : highScores.get(key)) {
					collapsedHighScores += name + "=" + key + "#";
					i++;
					
					if (i >= limit) break;
				}
				
				if (i >= limit) break;
			}
			
			return collapsedHighScores;
		}
	}
	
	/**
	 * Adds a name and score to the list of high scores.
	 * @param name - the name of the client achieving the score
	 * @param score - the score achieved
	 */
	public static void addHighScore(String name, long score) {
		print("<<< Adding score: " + score + " for player: " + name + " >>>");
		
		// Obtain a lock on the list of high scores
		synchronized (highScores) {
			if (!highScores.containsKey(score)) {
				// If the score is not already in the list of high scores,
				// create a new list of names with that score, add in the
				// specified name, and add the list and score to the hash map
				ArrayList<String> newNameList = new ArrayList<String>();
				newNameList.add(name);
				highScores.put(score, newNameList);
			} else {
				// Otherwise, add the specified name to the list of names with
				// the score specified
				highScores.get(score).add(name);
			}
		}
	}
	
	/**
	 * Removes a name and score combination from the list of high scores.
	 * @param name - the name of the client achieving the score
	 * @param score - the score achieved
	 */
	public static void removeHighScore(String name, long score) {
		// Obtain a lock on the list of high scores
		synchronized (highScores) {
			if (highScores.containsKey(score)) {
				highScores.get(score).remove(name);
			}
		}
	}
	
	/**
	 * Clears the list of high scores.
	 */
	public static void clearHighScores() {
		// Obtain a lock on the list of high scores
		synchronized (highScores) {
			highScores = new TreeMap<Long, ArrayList<String>>();
		}
	}
	
	
	/**
	 * Gets the data written to sysout.
	 * @return the array of data written to sysout
	 */
	public static ArrayList<String> getSysout() {
		// Obtain a lock on the standard output array
		synchronized (sysout) {
			return sysout;
		}
	}
	
	
	/**
	 * Adds a message to the sysout array.
	 * @param string - the string to write to the sysout array
	 */
	public static void print(String string) {
		// Set up the date format
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
		
		// Obtain a lock on the standard output array
		synchronized (sysout) {
			if (verbose) {
				if (printDateTime) {
					sysout.add(dateFormat.format(
							new Date(System.currentTimeMillis()))
							+ " : " + string);
				} else {
					sysout.add(string);
				}
			}
		}
	}

	/**
	 * Adds an exception to the sysout array.
	 * @param e - the exception to write to the sysout array
	 */
	public static void print(Exception e) {
		print(e.toString());
		for (int i = 0; i < e.getStackTrace().length; i++) {
			print("at " + e.getStackTrace()[i].toString());
		}
	}
	
	/**
	 * Sets whether to print the date and time to the standard output.
	 * @param printDateTime - <code>true</code> if the date and time
	 * 							should be printed to the standard output,
	 * 							otherwise <code>false</code>
	 */
	public static void setPrintDateTime(boolean printDateTime) {
		Server.printDateTime = printDateTime;
	}
	
	
	/**
	 * Resets the arrays and attributes held by this class.
	 * <p>
	 * These include:
	 * <ul>
	 * <li>The client removal timer</li>
	 * <li>The client array</li>
	 * <li>The sysout array</li>
	 * </ul>
	 * </p>
	 */
	public static void reset() {
		// Obtain a lock on the remove clients timer
		try {
			synchronized (removeClientsTimer) {
				// Reset the client removal timer
				removeClientsTimer = null;
			}
		} catch (NullPointerException e) {
			//
		}
		
		// Obtain a lock on the client array
		synchronized (clients) {
			// Reset the next client ID
			nextClientID = 0;
			
			// Reset the client array
			clients = new ArrayList<Client>();
		}

		// Obtain a lock on the standard output array
		synchronized (sysout) {
			// Reset the standard output array
			sysout = new ArrayList<String>();
		}
	}
}
