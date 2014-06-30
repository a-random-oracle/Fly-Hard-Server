package tst;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import srv.Client;
import srv.DataServlet;
import srv.Server;

public class DataServletTest {

	/** The servlet config to use */
	private static final ServletConfig servletConfig = mock(ServletConfig.class);
	
	/** The servlet under test */
	private static final DataServlet testServlet = new DataServlet();
	
	/** The valid user agent */
	private static final String testUserAgent = "TESTING";
	
	/** The map to use for generating data entries */
	private static TreeMap<Long, byte[]> transientMap;
	
	
	/**
	 * Sets up the server and the servlet.
	 */
	@Before
	public void setUpServlet() {
		// Reset the server
		Server.reset();
		Server.clearHighScores();
		
		// Clear the transient map
		transientMap = new TreeMap<Long, byte[]>();
		
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
	 * Tests that the data servlet responds to HTTP GET requests with a
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
		Entry<Long, byte[]> response = sendMockRequest("10", "false",
				null, "0", "0");
		
		// Check that no data is returned
		assertTrue("Data was returned", response == null);
	}
	
	/**
	 * Tests that when the client sends null data to the data servlet
	 * (whilst the client has a partner) that the data is rejected.
	 */
	@Test
	public void testPlayerPostNullDataWithConnection() {
		// Set up a connection
		Client client = Server.handleClient(-1, "TEST_CLIENT1",
				true, 0, 0);
		Client partner = Server.handleClient(-1, "TEST_CLIENT2",
				true, 0, 0);
		client.setPartner(partner);
		client.setPosition(0);
		partner.setPartner(client);
		partner.setPosition(1);
		
		// Send a mock request
		Entry<Long, byte[]> response = sendMockRequest("0", "true", null, "0", "0");
		
		// Check that no data is returned
		assertTrue("Data was returned", response == null);
	}
	
	/**
	 * Tests that when the client sends valid data to the data servlet
	 * (whilst the client has a partner) that the data is added to
	 * the client's partner's data buffer.
	 */
	@Test
	public void testPlayerPostValidDataWithConnection() {
		// Set up a connection
		Client client = Server.handleClient(-1, "TEST_CLIENT1",
				true, 0, 0);
		Client partner = Server.handleClient(-1, "TEST_CLIENT2",
				true, 0, 0);
		client.setPartner(partner);
		client.setPosition(0);
		partner.setPartner(client);
		partner.setPosition(1);
		
		// Send a mock request
		transientMap.put(1000L, "TEST_DATA".getBytes());
		sendMockRequest("0", "true", transientMap.firstEntry(), "0", "0");
		
		// Check that the data was written to the client's partner's data buffer
		assertTrue("The data was not written to the client's partner's data buffer",
				"TEST_DATA".equals(new String(partner.readLatestData().getValue())));
	}
	
	/**
	 * Tests that when the client sends valid data to the data servlet
	 * (whilst the client has a partner) that the data is added to
	 * the client's partner's data buffer.
	 * <p>
	 * This also tests that when there is data on the client's data buffer
	 * that this data is returned.
	 * </p>
	 */
	@Test
	public void testPlayerPostValidDataWithConnectionAndRecieveData() {
		// Set up a connection
		Client client = Server.handleClient(-1, "TEST_CLIENT1",
				true, 0, 0);
		Client partner = Server.handleClient(-1, "TEST_CLIENT2",
				true, 0, 0);
		client.setPartner(partner);
		client.setPosition(0);
		partner.setPartner(client);
		partner.setPosition(1);
		
		// Send a mock request
		transientMap.put(1000L, "TEST_DATA".getBytes());
		sendMockRequest("0", "false", transientMap.firstEntry(), "0", "0");
		
		// Send a mock request to retrieve the data
		transientMap.put(2000L, "TEST_DATA_TWO".getBytes());
		Entry<Long, byte[]> response1 = sendMockRequest("1", "false", transientMap.lastEntry(), "0", "0");
		
		// Send a mock request to retrieve the data
		transientMap.put(2000L, "TEST_DATA_TWO".getBytes());
		Entry<Long, byte[]> response2 = sendMockRequest("0", "false", transientMap.lastEntry(), "0", "0");
		
		// Check that the received data is the same as the sent data
		assertTrue("The received data doesn't match the sent data",
				"TEST_DATA".equals(new String(response1.getValue())));
		
		assertTrue("The received data doesn't match the sent data",
				"TEST_DATA_TWO".equals(new String(response2.getValue())));
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
	private static Entry<Long, byte[]> sendMockRequest(String clientID,
			String isHost, Entry<Long, byte[]> data, String lives,
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
					new MockServletInputStream(data));
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
		
		return servletOutputStream.getAsByteArray();
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
