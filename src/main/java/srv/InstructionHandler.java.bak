package srv;

/**
 * Handles instructions.
 * <p>
 * Instructions should be of the form:
 * 'COMMAND':'PARAMETERS;
 * </p>
 * <p>
 * Multiple instructions can be handled by passing
 * in a string of the form:
 * 'Instruction1';'Instruction2'
 * When multiple instructions are passed, they will be
 * handled sequentially, in the order they appear in the
 * string.
 * </p>
 */
public abstract class InstructionHandler {
	
	/** The instruction list delimiter */
	public static final String LIST_DELIM = ";";
	
	/** The instruction delimiter */
	public static final String DELIM = ":";
	
	
	/**
	 * Handles instructions.
	 * <p>
	 * Takes a semicolon-delimited list of instructions and
	 * processes them sequentially.
	 * </p>
	 * @param client - the client the instruction was sent by
	 * @param instruction - the instruction(s) to handle
	 * @return the message to send back to the client
	 */
	public static String handleInstruction(Client client,
			String instruction) {
		String response = "";
		
		if (instruction != null) {
			// Split the instruction string into individual instructions
			String[] instructionList = instruction.split(LIST_DELIM);

			// Check that there is at least one instruction
			if (instructionList != null) {
				if (instructionList.length > 0) {
					// Loop through the instructions, handling them
					// sequentially
					for (String instr : instructionList) {
						if (!response.equals("")) {
							response += LIST_DELIM;
						}
						
						response += handleIndividualInstruction(
								client, instr);
					}
				}
			}
		}
		
		return response;
	}
	
	/**
	 * Handles an instruction.
	 * <p>
	 * Breaks an instruction down into an instruction part
	 * and a parameter part, and passes these to the
	 * appropriate method (as specified in the instruction
	 * part.
	 * </p>
	 * @param client - the client the instruction was sent by
	 * @param instruction - the instruction to handle
	 * @return the message to send back to the client
	 */
	private static String handleIndividualInstruction(Client client,
			String instruction) {
		String response = "";
		
		// Get the instruction
		String instr = instruction.split(DELIM)[0];
		
		// Return immediately if the instruction is invalid
		if (instr == null) return "";
		
		// Check if the received data has parameters
		String parameters = null;
		if (instruction != null && instruction.contains(DELIM)) {
			parameters = instruction.substring(instruction.indexOf(DELIM) + 1);
		}
		
		// Otherwise, switch to the appropriate method
		switch (instr) {
		case "GET_OPEN_CONNECTIONS":
			response = handleGetOpenConnections(client);
			break;
		case "GET_HIGH_SCORES":
			response = handleGetHighScores(client);
			break;
		case "JOIN":
			response = handleJoin(client, parameters);
			break;
		case "GAME_OVER":
			response = handleGameOver(client, parameters);
			break;
		case "END_GAME":
			response = handleEndGame(client);
			break;
		}
		
		return response;
	}
	
	
	/**
	 * Handles a GET_OPEN_CONNECTIONS instruction.
	 * <p>
	 * GET_OPEN_CONNECTIONS instructions cause the server to reply
	 * with a hash-delimited list of clients which are waiting
	 * for connections.
	 * </p>
	 * <p>
	 * If no connections are available, "NO CONNECTIONS" will be returned.
	 * </p>
	 * @param client - the client sending the instruction
	 * @return the message to send back to the client
	 */
	private static String handleGetOpenConnections(Client client) {
		if (client != null) {
			// Obtain a lock on the client array
			synchronized (Server.getClients()) {
				String response = "";

				// Find the clients which are searching for partner
				response += Server.collapseAvailableHosts(
						Server.maxConnections, client);

				if (response.equals("")) {
					return "NO_CONNECTIONS";
				} else {
					return response;
				}
			}
		} else {
			return "INVALID_CLIENT";
		}
	}
	
	/**
	 * Handles a GET_HIGH_SCORES instruction.
	 * <p>
	 * GET_HIGH_SCORES instructions cause the server to reply
	 * with a hash-delimited list of high scores.
	 * </p>
	 * <p>
	 * The highest 15 scores will be returned.
	 * </p>
	 * @param client - the client sending the instruction
	 * @return the message to send back to the client
	 */
	private static String handleGetHighScores(Client client) {
		if (client != null) {
			// Obtain a lock on the list of high scores
			synchronized (Server.getHighScores()) {
				String response = "";

				// Get a collapsed list of the high scores
				response += Server.collapseHighScores(Server.maxConnections);

				if (response.equals("")) {
					return "NO_HIGH_SCORES";
				} else {
					return response;
				}
			}
		} else {
			return "INVALID_CLIENT";
		}
	}
	
	/**
	 * Handles a JOIN instruction.
	 * <p>
	 * JOIN instructions cause the calling client to be assigned as the
	 * partner of the waiting client specified in the parameters.
	 * </p>
	 * @param client - the client sending the instruction
	 * @param parameters - the parameters accompanying the instruction
	 * @return the message to send back to the client
	 */
	private static String handleJoin(Client client, String parameters) {
		if (client != null) {
			// Obtain a lock on the client array
			synchronized (Server.getClients()) {
				String response = "";
				
				// Get the client referenced by the ID given in the parameters
				Client clientToConnectTo = null;
				try {
					clientToConnectTo = Server
							.getClientFromID(Integer.parseInt(parameters));
				} catch (NumberFormatException e) {
					Server.print(e);
				}

				// Check that the clientToConnect to is not already connected
				// to another client
				if (clientToConnectTo != null
						&& !client.equals(clientToConnectTo)
						&& clientToConnectTo.getPartner() == null) {
					// Set the partner as the client's partner
					client.setPartner(clientToConnectTo);
					client.setPosition(1);

					// Set the client as the partner's partner
					clientToConnectTo.setPartner(client);
					clientToConnectTo.setPosition(0);

					// Synchronise the random seeds
					clientToConnectTo.setSeed(client.getSeed());

					// Add the client's random seed
					response += "SET_SEED:" + client.getSeed();
					clientToConnectTo.writeMessage("SET_SEED:" + client.getSeed());

					// Start a new game
					response += ";START_GAME:" + client.getPosition();
					clientToConnectTo.writeMessage("START_GAME:"
							+ clientToConnectTo.getPosition());

					return response;
				} else {
					return "INVALID_PARTNER";
				}
			}
		}  else {
			return "INVALID_CLIENT";
		}
	}
	
	/**
	 * Handles a GAME_OVER instruction.
	 * <p>
	 * GAME_OVER instructions cause the calling client exit the game (to the
	 * game over scene).
	 * </p>
	 * @param client - the client sending the instruction
	 * @param parameters - the parameters accompanying the instruction
	 * @return the message to send back to the client
	 */
	private static String handleGameOver(Client client, String parameters) {
		if (client != null) {
			// Split up the parameters
			String[] params= null;
			if (parameters != null) {
				params = parameters.split(DELIM);
			}
			
			String clientName = "";
			String partnerName = "";
			int clientLives = 0;
			int partnerLives = 0;
			int clientScore = 0;
			int partnerScore = 0;
			
			boolean partnerValid = false;
			
			// Obtain a lock on the client array
			synchronized (Server.getClients()) {
				if (client.getPartner() != null) {
					// Get the client's and the client's partner's
					// score details
					partnerValid = true;
					clientName = client.getName();
					partnerName = client.getPartner().getName();
					clientLives = client.getLives();
					partnerLives = client.getPartner().getLives();
					clientScore = client.getScore();
					partnerScore = client.getPartner().getScore();
				}
				
				// Remove the client
				if (params != null && params.length >= 2) {
					Server.removeClient(client,
							"GAME_OVER:" + params[0] + DELIM + params[1]);
				} else {
					Server.removeClient(client, "GAME_OVER");
				}
			}
			
			if (partnerValid) {
				// Obtain a lock on the list of high scores
				synchronized (Server.getHighScores()) {
					// Add the appropriate client's score to the list of
					// high scores
					if ((clientLives > partnerLives)
							&& (clientScore > partnerScore)) {
						// The client had both the higher score and higher lives
						// so add them to the list of high scores
						Server.addHighScore(clientName, clientScore);
					} else if ((partnerLives > clientLives)
							&& (partnerScore > clientScore)) {
						// The client's partner had both the higher score and
						// higher lives so add them to the list of high scores
						Server.addHighScore(partnerName, partnerScore);
					}
				}
			}
			
			return "ENDED_GAME";
		} else {
			return "INVALID_CLIENT";
		}
	}
	
	/**
	 * Handles a END_GAME instruction.
	 * <p>
	 * END_GAME instructions cause the calling client exit the game (to the
	 * lobby scene).
	 * </p>
	 * @param client - the client sending the instruction
	 * @return the message to send back to the client
	 */
	private static String handleEndGame(Client client) {
		if (client != null) {
			// Obtain a lock on the client array
			synchronized (Server.getClients()) {
				// Remove the client
				Server.removeClient(client, "END_GAME");
				
				return "ENDED_GAME";
			}
		} else {
			return "INVALID_CLIENT";
		}
	}
	
}
