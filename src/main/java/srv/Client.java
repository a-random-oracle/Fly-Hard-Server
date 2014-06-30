package srv;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

/**
 * Represents a client connected to the server.
 * <p>
 * Once a client connects to the server, an instance of this class
 * is created to be used to refer to them.
 * </p>
 * <p>
 * Clients are associated with unique IDs, which are passed during
 * the connection phase.
 * </p>
 */
public class Client {
	
	/** The delimiter to use to separate messages in the message buffer */
	public static final String MESSAGE_DELIM = ";";
	
	/** The client's ID */
	private long id;
	
	/** The time the client last connected at */
	private long lastConnection;
	
	/** The client's screen name */
	private String name;
	
	/** The client's partner */
	private Client partner;
	
	/** The client's position */
	private int position;
	
	/** The client's random seed */
	private int seed;
	
	/** Whether the client is a host */
	private boolean host;
	
	/** The client's lives */
	private int lives;
	
	/** The client's score */
	private int score;
	
	/** Whether the client is closing or not */
	private boolean closing;
	
	/** The client's data buffer */
	private TreeMap<Long, byte[]> dataBuffer;
	
	/** The client's data buffer */
	private LinkedList<byte[]> priorityDataBuffer;
	
	/** The client's messages */
	private String messages;
	
	
	/**
	 * Creates a new client.
	 */
	public Client() {
		this.id = Server.getNewClientID();
		this.lastConnection = System.currentTimeMillis();
		this.name = null;
		this.seed = (new Random()).nextInt();
		this.host = false;
		this.lives = 0;
		this.score = 0;
		this.closing = false;
		this.dataBuffer = new TreeMap<Long, byte[]>();
		this.priorityDataBuffer = new LinkedList<byte[]>();
		this.messages = "";
		
		// Check if the server has a remove client timer in place
		if (!Server.isRemoveClientsTimerPresent()) {
			// If not, start one
			Server.startRemoveClientsTimer();
		}
	}
	
	
	/**
	 * Gets the client's ID.
	 * @return the client's ID
	 */
	public long getID() {
		return id;
	}
	
	/**
	 * Gets the time the client last connected at.
	 * @return the time the client last connected at
	 */
	public long getLastConnection() {
		return lastConnection;
	}
	
	/**
	 * Gets the client's name.
	 * @return the client's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the client's partner.
	 * @return the client's partner
	 */
	public Client getPartner() {
		return partner;
	}
	
	/**
	 * Gets the client's position.
	 * @return the client's position
	 */
	public int getPosition() {
		return position;
	}
	
	/**
	 * Gets the client's random seed.
	 * @return the client's random seed
	 */
	public int getSeed() {
		return seed;
	}
	
	/**
	 * Gets whether the client is a host.
	 * @return <code>true</code> if the client is a host,
	 * 			otherwise <code>false</code>
	 */
	public boolean isHost() {
		return host;
	}
	
	/**
	 * Gets the client's lives.
	 * @return the client's lives
	 */
	public int getLives() {
		return lives;
	}
	
	/**
	 * Gets the client's score.
	 * @return the client's score
	 */
	public int getScore() {
		return score;
	}
	
	/**
	 * Gets whether the client is closing.
	 * @return <code>true</code> if the client is closing,
	 * 			otherwise <code>false</code>
	 */
	public boolean isClosing() {
		return closing;
	}
	
	
	/**
	 * Sets the time the client last connected at.
	 */
	public void updateLastConnectionTime() {
		this.lastConnection = System.currentTimeMillis();
	}
	
	/**
	 * Sets the client's name.
	 * @param name - the client's name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the client's partner
	 * @param partner - the partner to set
	 */
	public void setPartner(Client partner) {
		this.partner = partner;
	}
	
	/**
	 * Sets the client's position.
	 * @param position - the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	
	/**
	 * Sets the client's random seed.
	 * @param seed - the seed to set
	 */
	public void setSeed(int seed) {
		this.seed = seed;
	}
	
	/**
	 * Sets whether the client is a host.
	 * @param host - <code>true</code> if the client is a host,
	 * 					otherwise <code>false</code>
	 */
	public void setHost(boolean host) {
		this.host = host;
	}
	
	/**
	 * Sets the client's lives.
	 * @param lives - the lives to set
	 */
	public void setLives(int lives) {
		this.lives = lives;
	}
	
	/**
	 * Sets the client's score.
	 * @param score - the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}
	
	/**
	 * Sets whether the client is closing.
	 * @param host - <code>true</code> if the client is closing,
	 * 					otherwise <code>false</code>
	 */
	public void setClosing(boolean closing) {
		this.closing = closing;
	}
	
	
	/**
	 * Writes a data entry to the data buffer.
	 * @param dataEntry - the data entry to write to the buffer
	 */
	public void writeData(Entry<Long, byte[]> dataEntry) {
		// Check if data has priority
		if (dataEntry.getKey() == -1) {
			// Obtain a lock on the priority data buffer
			synchronized (priorityDataBuffer) {
				// Write the data element to the data buffer
				priorityDataBuffer.add(dataEntry.getValue());
			}
		} else{
			// Obtain a lock on the data buffer
			synchronized (dataBuffer) {
				// Write the data element to the data buffer
				dataBuffer.put(dataEntry.getKey(), dataEntry.getValue());
			}
		}
	}
	
	/**
	 * Reads an individual data element from the data buffer.
	 * <p>
	 * The buffer is then cleared.
	 * </p>
	 * <p>
	 * If the buffer is empty, this will return null.
	 * </p>
	 * @return the last data element in the data buffer
	 */
	public Entry<Long, byte[]> readLatestData() {
		TreeMap<Long, byte[]> transientMap =
				new TreeMap<Long, byte[]>();
		Entry<Long, byte[]> dataEntry = null;
		
		// Obtain a lock on the priority data buffer
		synchronized (priorityDataBuffer) {
			// Check for priority data
			if (priorityDataBuffer.size() > 0) {
				transientMap.put(-1L, priorityDataBuffer.removeFirst());
				dataEntry = (transientMap.firstEntry());
			}
		}
		
		// If there was no priority data
		if (dataEntry == null) {
			// Obtain a lock on the data buffer
			synchronized (dataBuffer) {
				// Get the latest data entry from the data buffer
				dataEntry = dataBuffer.lastEntry();

				// Clear the data buffer
				dataBuffer.clear();
			}
		}
		
		// Return the data entry
		return dataEntry;
	}
	
	
	/**
	 * Checks if there are any messages to read.
	 * @return <code>true</code> if there are messages to read,
	 * 			otherwise <code>false</code>
	 */
	public boolean checkForMessages() {
		return (messages.equals("")) ? false : true;
	}
	
	/**
	 * Writes a message to the string of messages.
	 * @param message - the message to write to the string
	 */
	public void writeMessage(String message) {
		// Obtain a lock on the string of messages
		synchronized (messages) {
			// Add the message to the end of the string
			if (messages.equals("")) {
				messages = message;
			} else {
				messages += MESSAGE_DELIM + message;
			}
		}
	}
	
	/**
	 * Reads the string of messages.
	 * <p>
	 * The string is then cleared.
	 * </p>
	 * @return the string of messages
	 */
	public String readMessages() {
		// Obtain a lock on the string of messages
		synchronized (messages) {
			// Get the string of messages
			String messageString = messages;

			// Clear the string of messages
			messages = "";

			// Return the string of messages
			return messageString;
		}
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Client other = (Client) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
