package tst;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import srv.MessageServlet;
import srv.Server;

public class MessageServletTest {
	
	/** The servlet config to use */
	private static final ServletConfig servletConfig = mock(ServletConfig.class);
	
	/** The servlet under test */
	private static final MessageServlet testServlet = new MessageServlet();
	
	/** The valid user agent */
	private static final String testUserAgent = "TESTING";
	
	
	/**
	 * Sets up the server and the servlet.
	 */
	@Before
	public void setUpServlet() {
		// Reset the server
		Server.reset();
		Server.clearHighScores();
		
		try {
			testServlet.init(servletConfig);
			
			// Add a test user agent to the list of accepted user agents
			Server.addVersion(testUserAgent);
		} catch (ServletException e) {
			e.printStackTrace();
			fail("Servlet was not set up correctly");
		}
	}

	
	/**
	 * Tests that the message servlet responds to HTTP GET requests with a
	 * 404 'page not found' error.
	 */
	@Test
	public void testDoGet() {
		// Create mock request and response objects
		HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        // Perform the HTTP GET
        testServlet.doGet(request, response);
        
        // Check that the server returned a 404 error
        try {
			verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test that when a post request is received, clients with
	 * a valid user agent are accepted.
	 */
	@Test
	public void testDoPostSuccess() {
		// Create mock request and response objects
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		
		// Set the request user agent
		when(request.getHeader("user-agent")).thenReturn(testUserAgent);

		// Perform the HTTP POST
		testServlet.doPost(request, response);

		// Check that the server *didn't* return a 404 error
		try {
			verify(response, times(0)).sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test that when a post request is received, clients with
	 * an invalid user agent are rejected.
	 * <p>
	 * The user agents tried here are taken from:
	 * http://techblog.willshouse.com/2012/01/03/most-common-user-agents/
	 * which is published under the creative commons Attribution-ShareAlike
	 * 2.5 Generic licence.
	 * </p>
	 */
	@Test
	public void testDoPostFail() {
		// Add a test user agent to the list of accepted user agents
		String testUserAgent = "TESTING";
		Server.addVersion(testUserAgent);
		
		// Create a list of user agents which should be rejected
		String[] invalidUserAgents = new String[] {
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36"
				+ "(KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit"
				+ "/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/537.75.14",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 "
				+ "(KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:28.0) Gecko/20100101 Firefox/28.0",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko"
		};
		
		// Test each of the invalid user agents
		for (String userAgent : invalidUserAgents) {
			// Create mock request and response objects
			HttpServletRequest request = mock(HttpServletRequest.class);
			HttpServletResponse response = mock(HttpServletResponse.class);

			// Set the request user agent
			when(request.getHeader("user-agent")).thenReturn(userAgent);

			// Perform the HTTP POST
			testServlet.doPost(request, response);

			// Check that the server returned a 404 error
			try {
				verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Tests that if a client tries to connect to the server with an ID which
	 * hasn't been issued, that they are rejected with INVALID_CLIENT.
	 */
	@Test
	public void testPlayerPostInvalidClient() {
		// Send a mock request
		String response1 = sendMockRequest("10", "false",
				"GET_OPEN_CONNECTIONS", "0", "0");
		
		// Check that INVALID_CLIENT is returned
		assertTrue("The expected response (INVALID_CLIENT) was not returned",
				response1.equals("INVALID_CLIENT"));

		// Send a mock request
		String response2 = sendMockRequest("10", "false",
				"GET_HIGH_SCORES", "0", "0");

		// Check that INVALID_CLIENT is returned
		assertTrue("The expected response (INVALID_CLIENT) was not returned",
				response2.equals("INVALID_CLIENT"));

		// Send a mock request
		String response3 = sendMockRequest("10", "false",
				"JOIN", "0", "0");

		// Check that INVALID_CLIENT is returned
		assertTrue("The expected response (INVALID_CLIENT) was not returned",
				response3.equals("INVALID_CLIENT"));
		
		// Send a mock request
		String response4 = sendMockRequest("10", "false",
				"GAME_OVER", "0", "0");

		// Check that INVALID_CLIENT is returned
		assertTrue("The expected response (INVALID_CLIENT) was not returned",
				response4.equals("INVALID_CLIENT"));

		// Send a mock request
		String response5 = sendMockRequest("10", "false",
				"END_GAME", "0", "0");

		// Check that INVALID_CLIENT is returned
		assertTrue("The expected response (INVALID_CLIENT) was not returned",
				response5.equals("INVALID_CLIENT"));
	}
	
	/**
	 * Tests that if a client tries to send an unsupported message to the
	 * server, that it is rejected with INVALID_REQUEST.
	 */
	@Test
	public void testPlayerPostInvalidRequest() {
		// Send a mock request
		String response = sendMockRequest("-1", "false",
				"TEST_INSTRUCTION", "0", "0");

		// Check that INVALID_REQUEST is returned
		assertTrue("The expected response (INVALID_REQUEST) was not returned",
				response.equals("INVALID_REQUEST"));
	}

	/**
	 * Tests that an initial GET_OPEN_CONNECTIONS request returns with NO_CONNECTIONS.
	 */
	@Test
	public void testPlayerPostGetConnectionsNoConnections() {
		// Send a mock request
		String response = sendMockRequest("-1", "false",
				"GET_OPEN_CONNECTIONS", "0", "0");

		// Check that NO_CONNECTIONS is returned
		assertTrue("The expected response (NO_CONNECTIONS) was not returned",
				response.equals("NO_CONNECTIONS"));
	}
	
	/**
	 * Tests that a continuing GET_OPEN_CONNECTIONS request returns with a
	 * list of the open connections.
	 */
	@Test
	public void testPlayerPostGetConnectionsValidConnections() {
		// Send a mock request
		String response1 = sendMockRequest("-1", "true",
				"GET_OPEN_CONNECTIONS", "0", "0");
		String response2 = sendMockRequest("-1", "false",
				"GET_OPEN_CONNECTIONS", "0", "0");
		
		// Check that NO_CONNECTIONS is returned to the first client
		assertTrue("The expected response (NO_CONNECTIONS) was not returned",
				response1.equals("NO_CONNECTIONS"));
		
		// Check that a connection is returned to the second client
		assertTrue("The expected response (0=TEST_CLIENT#) was not returned",
				response2.equals("0=TEST_CLIENT#"));
	}
	
	/**
	 * Tests that an initial GET_HIGH_SCORES request returns with NO_HIGH_SCORES.
	 */
	@Test
	public void testPlayerPostGetHighScoresNoHighScores() {
		// Send a mock request
		String response = sendMockRequest("-1", "true",
				"GET_HIGH_SCORES", "0", "0");

		// Check that NO_HIGH_SCORES is returned
		assertTrue("The expected response (NO_HIGH_SCORES) was not returned",
				response.equals("NO_HIGH_SCORES"));
	}
	
	/**
	 * Tests that a continuing GET_HIGH_SCORES request returns with a
	 * list of the current high scores.
	 */
	@Test
	public void testPlayerPostGetHighScoresHighScoresPresent() {
		// Add some high scores
		Server.addHighScore("TestBot1", 9999);
		Server.addHighScore("TestBot2", 1024);
		Server.addHighScore("TestBot3", 835);
		Server.addHighScore("TestBot1", -200);
		
		// Send a mock request
		String response = sendMockRequest("-1", "true",
				"GET_HIGH_SCORES", "0", "0");

		// Check that a list of high scores is returned
		assertTrue("The expected response (TestBot1=9999#TestBot2=1024#"
				+ "TestBot3=835#TestBot1=-200#) was not returned",
				response.equals("TestBot1=9999#TestBot2=1024#"
						+ "TestBot3=835#TestBot1=-200#"));
	}
	
	/**
	 * Tests that a JOIN request specifying an invalid partner returns with
	 * INVALID_PARTNER.
	 * <p>
	 * An invalid partner is one which isn't connected to the server,
	 * is equal to the client, or which is already connected to
	 * another client.
	 * </p>
	 */
	@Test
	public void testPlayerPostJoinInvalidPartner() {
		// Send a mock request to the current client
		// ClientID = 0
		String response1 = sendMockRequest("-1", "false",
				"JOIN:0", "0", "0");

		// Check that INVALID_PARTNER is returned
		assertTrue("The expected response (INVALID_PARTNER) was not returned",
				response1.equals("INVALID_PARTNER"));
		
		// Send a mock request to an invalid client
		// ClientID = 1
		String response2 = sendMockRequest("-1", "false",
				"JOIN:15", "0", "0");

		// Check that INVALID_PARTNER is returned
		assertTrue("The expected response (INVALID_PARTNER) was not returned",
				response2.equals("INVALID_PARTNER"));

		// Send mock requests to set up a connection
		// ClientID = 2
		sendMockRequest("-1", "true", "GET_OPEN_CONNECTIONS", "0", "0");
		// ClientID = 3
		sendMockRequest("-1", "false", "JOIN:2", "0", "0");
		
		// Send mock requests to set up a connection to a client who has
		// already entered a connection
		// ClientID = 4
		String response5 = sendMockRequest("-1", "false",
				"JOIN:3", "0", "0");

		// Check that INVALID_PARTNER is returned
		assertTrue("The expected response (INVALID_PARTNER) was not returned",
				response5.equals("INVALID_PARTNER"));

		// Send mock requests to set up a connection to a client who has
		// already entered a connection
		// ClientID = 4
		String response6 = sendMockRequest("-1", "false",
				"JOIN:ABC", "0", "0");

		// Check that INVALID_PARTNER is returned
		assertTrue("The expected response (INVALID_PARTNER) was not returned",
				response6.equals("INVALID_PARTNER"));
	}
	
	/**
	 * Tests that a JOIN request specifying a valid partner returns with
	 * both SET_SEED and START_GAME instructions.
	 */
	@Test
	public void testPlayerPostJoinValidPartner() {
		// Send mock requests to set up a connection
		// ClientID = 0
		sendMockRequest("-1", "true", "GET_OPEN_CONNECTIONS", "0", "0");
		// ClientID = 1
		String response2 = sendMockRequest("-1", "false",
				"JOIN:0", "0", "0");

		// Check that SET_SEED and START_GAME instructions are returned
		assertTrue("The expected response (SET_SEED) was not returned",
				response2.contains("SET_SEED"));
		assertTrue("The expected response (START_GAME) was not returned",
				response2.contains("START_GAME"));
		
		// Check that SET_SEED and START_GAME instructions have been written
		// to the partner's message buffer
		String partnersMessageBuffer = Server.getClientFromID(0).readMessages();
		
		assertTrue("The partner's message buffer doesn't contain a"
				+ "SET_SEED instruction",
				partnersMessageBuffer.contains("SET_SEED"));
		assertTrue("The partner's message buffer doesn't contain a"
				+ "START_GAME instruction",
				partnersMessageBuffer.contains("START_GAME"));
	}
	
	/**
	 * Tests that an GAME_OVER request returns with an ENDED_GAME instruction,
	 * even when the parameters supplied are invalid.
	 * <p>
	 * Parameters are invalid when they are not present (i.e. null),
	 * or an incorrect number of them (too few) is passed.
	 * </p>
	 */
	@Test
	public void testPlayerPostGameOverInvalidParameters() {
		// Send a mock request with null parameters
		String response1 = sendMockRequest("-1", "false",
				"GAME_OVER", "0", "0");

		// Check that an ENDED_GAME instruction is returned
		assertTrue("The expected response (ENDED_GAME) was not returned",
				response1.equals("ENDED_GAME"));
		
		// Send a mock request with too few
		String response2 = sendMockRequest("-1", "false",
				"GAME_OVER:PARAM1", "0", "0");

		// Check that an ENDED_GAME instruction is returned
		assertTrue("The expected response (ENDED_GAME) was not returned",
				response2.equals("ENDED_GAME"));
	}
	
	/**
	 * Tests that an GAME_OVER request returns with an ENDED_GAME instruction.
	 */
	@Test
	public void testPlayerPostGameOverValidParameters() {
		// Send a mock request with null parameters
		String response = sendMockRequest("-1", "false",
				"GAME_OVER:P1:P2", "0", "0");

		// Check that an ENDED_GAME instruction is returned
		assertTrue("The expected response (ENDED_GAME) was not returned",
				response.equals("ENDED_GAME"));
	}
	
	/**
	 * Tests that an GAME_OVER request returns with an ENDED_GAME instruction,
	 * and that the client's partner is passed a GAME_OVER instruction.
	 */
	@Test
	public void testPlayerPostGameOverValidParametersAndClient() {
		// Send mock requests to set up a connection
		// ClientID = 0
		sendMockRequest("-1", "true", "GET_OPEN_CONNECTIONS", "0", "0");
		// ClientID = 1
		sendMockRequest("-1", "false", "JOIN:0", "0", "0");
		
		// Send mock request to end a connection
		// ClientID = 1
		String response3 = sendMockRequest("1", "false",
				"GAME_OVER:P1:P2", "0", "0");

		// Check that an ENDED_GAME instruction is returned
		assertTrue("The expected response (INVALID_PARTNER) was not returned",
				response3.contains("ENDED_GAME"));

		// Check that a GAME_OVER instruction has been written to the partner's
		// message buffer, with the correct parameters
		String partnersMessageBuffer = Server.getClientFromID(0).readMessages();

		assertTrue("The partner's message buffer doesn't contain a"
				+ "GAME_OVER instruction",
				partnersMessageBuffer.contains("GAME_OVER"));
		assertTrue("The partner's message buffer doesn't contain the"
				+ "correct parameters",
				partnersMessageBuffer.contains("P1:P2"));
	}
	
	/**
	 * Tests that a GAME_OVER request returns with an ENDED_GAME instruction,
	 * and that the client's partner is passed a GAME_OVER instruction.
	 * <p>
	 * This also checks that when the client gets a higher score and a higher
	 * life value than their partner that they get added to the list of
	 * high scores.
	 * </p>
	 */
	@Test
	public void testPlayerPostGameOverClientWins() {
		// Send mock requests to set up a connection
		// ClientID = 0
		sendMockRequest("-1", "true", "GET_OPEN_CONNECTIONS", "0", "0");
		// ClientID = 1
		sendMockRequest("-1", "false", "JOIN:0", "0", "0");

		// The first client has more lives and more score than the second client
		// ClientID = 0
		sendMockRequest("0", "true", "GET_OPEN_CONNECTIONS", "2", "300");
		// ClientID = 1
		sendMockRequest("1", "false", "JOIN:0", "0", "-1200");
		
		// Send mock request to end a connection
		// ClientID = 1
		String response3 = sendMockRequest("1", "false",
				"GAME_OVER:P1:P2", "0", "-1200");

		// Check that an ENDED_GAME instruction is returned
		assertTrue("The expected response (INVALID_PARTNER) was not returned",
				response3.contains("ENDED_GAME"));

		// Check that a GAME_OVER instruction has been written to the partner's
		// message buffer, with the correct parameters
		String partnersMessageBuffer = Server.getClientFromID(0).readMessages();

		assertTrue("The partner's message buffer doesn't contain a"
				+ "GAME_OVER instruction",
				partnersMessageBuffer.contains("GAME_OVER"));
		assertTrue("The partner's message buffer doesn't contain the"
				+ "correct parameters",
				partnersMessageBuffer.contains("P1:P2"));
		
		// Check that the client's score has been added to the list of
		// high scores
		assertTrue("Client's score hasn't been added to the list of high scores",
				Server.getHighScores().get(300L) != null);
		assertTrue("Client's score hasn't been added to the list of high scores",
				Server.getHighScores().get(300L).contains("TEST_CLIENT"));
	}
	
	/**
	 * Tests that a GAME_OVER request returns with an ENDED_GAME instruction,
	 * and that the client's partner is passed a GAME_OVER instruction.
	 * <p>
	 * This also checks that when the client's partner gets a higher score
	 * and a higher life value than the client that they get added to the list of
	 * high scores.
	 * </p>
	 */
	@Test
	public void testPlayerPostGameOverPartnerWins() {
		// Send mock requests to set up a connection
		// ClientID = 0
		sendMockRequest("-1", "true", "GET_OPEN_CONNECTIONS", "0", "0");
		// ClientID = 1
		sendMockRequest("-1", "false", "JOIN:0", "0", "0");

		// The second client has more lives and more score than the first client
		// ClientID = 0
		sendMockRequest("0", "true", "GET_OPEN_CONNECTIONS", "0", "-100");
		// ClientID = 1
		sendMockRequest("1", "false", "JOIN:0", "3", "0");
		
		// Send mock request to end a connection
		// ClientID = 1
		String response3 = sendMockRequest("1", "false",
				"GAME_OVER:P1:P2", "3", "0");

		// Check that an ENDED_GAME instruction is returned
		assertTrue("The expected response (INVALID_PARTNER) was not returned",
				response3.contains("ENDED_GAME"));

		// Check that a GAME_OVER instruction has been written to the partner's
		// message buffer, with the correct parameters
		String partnersMessageBuffer = Server.getClientFromID(0).readMessages();

		assertTrue("The partner's message buffer doesn't contain a"
				+ "GAME_OVER instruction",
				partnersMessageBuffer.contains("GAME_OVER"));
		assertTrue("The partner's message buffer doesn't contain the"
				+ "correct parameters",
				partnersMessageBuffer.contains("P1:P2"));
		
		// Check that the client's partner's score has been added to the list of
		// high scores
		assertTrue("Client's partner's score hasn't been added to the list of high scores",
				Server.getHighScores().get(0L) != null);
		assertTrue("Client's partner's score hasn't been added to the list of high scores",
				Server.getHighScores().get(0L).contains("TEST_CLIENT"));
	}
	
	/**
	 * Tests that a GAME_OVER request returns with an ENDED_GAME instruction,
	 * and that the client's partner is passed a GAME_OVER instruction.
	 * <p>
	 * This also checks that when the client gets a higher score <b> but not
	 * </b> a higher life value than their partner that they <b> do not </b>
	 * get added to the list of high scores.
	 * </p>
	 */
	@Test
	public void testPlayerPostGameOverClientDoesntWin() {
		// Send mock requests to set up a connection
		// ClientID = 0
		sendMockRequest("-1", "true", "GET_OPEN_CONNECTIONS", "0", "0");
		// ClientID = 1
		sendMockRequest("-1", "false", "JOIN:0", "0", "0");

		// The first client has more lives and more score than the second client
		// ClientID = 0
		sendMockRequest("0", "true", "GET_OPEN_CONNECTIONS", "0", "300");
		// ClientID = 1
		sendMockRequest("1", "false", "JOIN:0", "2", "-1200");
		
		// Send mock request to end a connection
		// ClientID = 1
		String response3 = sendMockRequest("1", "false",
				"GAME_OVER:P1:P2", "2", "-1200");

		// Check that an ENDED_GAME instruction is returned
		assertTrue("The expected response (INVALID_PARTNER) was not returned",
				response3.contains("ENDED_GAME"));

		// Check that a GAME_OVER instruction has been written to the partner's
		// message buffer, with the correct parameters
		String partnersMessageBuffer = Server.getClientFromID(0).readMessages();

		assertTrue("The partner's message buffer doesn't contain a"
				+ "GAME_OVER instruction",
				partnersMessageBuffer.contains("GAME_OVER"));
		assertTrue("The partner's message buffer doesn't contain the"
				+ "correct parameters",
				partnersMessageBuffer.contains("P1:P2"));
		
		// Check that the client's score hasn't been added to the list of
		// high scores
		if (Server.getHighScores().get(300L) != null) {
			assertFalse("Client's score *has* been added to the list of high scores",
					Server.getHighScores().get(300L).contains("TEST_CLIENT"));
		}
	}
	
	/**
	 * Tests that a GAME_OVER request returns with an ENDED_GAME instruction,
	 * and that the client's partner is passed a GAME_OVER instruction.
	 * <p>
	 * This also checks that when the client's partner gets a higher score
	 * <b> but not </b> a higher life value than the client that they
	 * <b> do not </b> get added to the list of high scores.
	 * </p>
	 */
	@Test
	public void testPlayerPostGameOverPartnerDoesntWin() {
		// Send mock requests to set up a connection
		// ClientID = 0
		sendMockRequest("-1", "true", "GET_OPEN_CONNECTIONS", "0", "0");
		// ClientID = 1
		sendMockRequest("-1", "false", "JOIN:0", "0", "0");

		// The second client has more lives and more score than the first client
		// ClientID = 0
		sendMockRequest("0", "true", "GET_OPEN_CONNECTIONS", "3", "-100");
		// ClientID = 1
		sendMockRequest("1", "false", "JOIN:0", "0", "0");
		
		// Send mock request to end a connection
		// ClientID = 1
		String response3 = sendMockRequest("1", "false",
				"GAME_OVER:P1:P2", "0", "0");

		// Check that an ENDED_GAME instruction is returned
		assertTrue("The expected response (INVALID_PARTNER) was not returned",
				response3.contains("ENDED_GAME"));

		// Check that a GAME_OVER instruction has been written to the partner's
		// message buffer, with the correct parameters
		String partnersMessageBuffer = Server.getClientFromID(0).readMessages();

		assertTrue("The partner's message buffer doesn't contain a"
				+ "GAME_OVER instruction",
				partnersMessageBuffer.contains("GAME_OVER"));
		assertTrue("The partner's message buffer doesn't contain the"
				+ "correct parameters",
				partnersMessageBuffer.contains("P1:P2"));
		
		// Check that the client's partner's score hasn't been added to the list of
		// high scores
		if (Server.getHighScores().get(0L) != null) {
		assertFalse("Client's partner's score *has* been added"
				+ "to the list of high scores",
				Server.getHighScores().get(0L).contains("TEST_CLIENT"));
		}
	}
	
	/**
	 * Tests that an END_GAME request returns with an ENDED_GAME instruction,
	 * and that the client is then removed from the server.
	 */
	@Test
	public void testPlayerPostEndGame() {
		// Send a mock request
		String response = sendMockRequest("-1", "false",
				"END_GAME", "0", "0");

		// Check that an ENDED_GAME instruction is returned
		assertTrue("The expected response (ENDED_GAME) was not returned",
				response.equals("ENDED_GAME"));
		
		// Check that the client has been removed from the server
		assertTrue("The client is still connected to the server",
				Server.getClientFromID(0) == null);
	}
	
	/**
	 * Tests that when more than one message is passed that they are
	 * all handled successfully.
	 */
	@Test
	public void testPlayerPostMultipleInstructions() {
		// Send a mock request withtwo instructions
		String response1 = sendMockRequest("-1", "false",
				"GET_OPEN_CONNECTIONS;GET_HIGH_SCORES", "0", "0");

		// Check that a NO_CONNECTIONS instruction is returned
		assertTrue("The expected response (NO_CONNECTIONS) was not returned",
				response1.contains("NO_CONNECTIONS"));
		
		// Check that a NO_HIGH_SCORES instruction is returned
		assertTrue("The expected response (NO_HIGH_SCORES) was not returned",
				response1.contains("NO_HIGH_SCORES"));
	}
	
	
	/**
	 * Sends a mock request to the servlet.
	 * @param clientID - the client ID to send in the request headers
	 * @param isHost - the host status to send in the request headers
	 * @param instruction - the instruction to send to the sevlet
	 * @param lives - the lives to send in the request headers
	 * @param score - the score to send in the request headers
	 * @return the servlet's response
	 */
	private static String sendMockRequest(String clientID,
			String isHost, String instruction, String lives,
			String score) {
		// Create mock request and response objects
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		MockServletOutputStream servletOutputStream =
				new MockServletOutputStream();
		
		// Set the request headers
		setValidHeaders(request, clientID, isHost, lives, score);
		
		// Set up the input stream
		try {
			when(request.getInputStream()).thenReturn(
					new MockServletInputStream(instruction));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Set up the output stream
		try {
			when(response.getOutputStream()).thenReturn(
					servletOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Perform the HTTP POST
		testServlet.doPost(request, response);

		// Check that the request headers are read
		checkHeaders(request);
		
		return servletOutputStream.getAsString();
	}
	
	/**
	 * Set valid request headers.
	 * <p>
	 * This allows the client's lives and score to be specified.
	 * </p>
	 * @param request - the request to set the headers for
	 */
	private static void setValidHeaders(HttpServletRequest request,
			String clientID, String isHosting, String lives, String score) {
		when(request.getHeader("user-agent")).thenReturn(testUserAgent);
		when(request.getHeader("fh-client-id")).thenReturn(clientID);
		when(request.getHeader("fh-client-name")).thenReturn("TEST_CLIENT");
		when(request.getHeader("fh-client-host")).thenReturn(isHosting);
		when(request.getHeader("fh-client-lives")).thenReturn(lives);
		when(request.getHeader("fh-client-score")).thenReturn(score);
	}
	
	/**
	 * Check that request headers are read.
	 * @param request - the request to check the headers for
	 */
	private static void checkHeaders(HttpServletRequest request) {
		verify(request).getHeader("user-agent");
		verify(request).getHeader("fh-client-id");
		verify(request).getHeader("fh-client-name");
		verify(request, atMost(2)).getHeader("fh-client-host");
		verify(request).getHeader("fh-client-lives");
		verify(request).getHeader("fh-client-score");
	}

}
