package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import srv.Client;
import srv.Server;

/**
 * This test class covers the server functionality which is not covered
 * by the client or servlet tests.
 * <p>
 * This functionality is mostly administrative, and was used during
 * server set up and integration with our application.
 * </p>
 * <p>
 * As such, ideally this code will not be used once the game is in
 * production.
 * </p>
 */
public class ServerTest {
	
	/**
	 * Sets up the server.
	 */
	@Before
	public void setUpServer() {
		// Reset the server
		Server.reset();
		Server.clearRemoveClientsTimer();
		Server.clearHighScores();
	}
	
	
	/**
	 * Tests that only the correct client is returned, even when
	 * the server's list of clients contains null values.
	 */
	@Test
	public void testGetClientFromIDWhereListContainsNullClient() {
		// Insert some values into the client array
		
		// ClientID = 0
		Client client0 = new Client();
		Server.getClients().add(client0);
		
		// ClientID = 1
		Client client1 = Server.handleClient(-1, "Test1", false, 0, 0);
		
		// ClientID = 2
		Client client2 = new Client();
		Server.getClients().add(client2);
		
		// Null
		Server.getClients().add(null);
		
		// ClientID = 3
		Client client3 = new Client();
		Server.getClients().add(client3);
		
		// ClientID = 4
		Client client4 = Server.handleClient(-1, "Test2", true, 0, 0);
		
		// This is also a null value, though that may not be obvious:
		Server.handleClient(100, "Test2", true, 0, 0);
		
		
		// Check that all the non-null clients can be found
		assertTrue("Client 0 doesn't match",
				client0.equals(Server.getClientFromID(0)));
		assertTrue("Client 1 doesn't match",
				client1.equals(Server.getClientFromID(1)));
		assertTrue("Client 2 doesn't match",
				client2.equals(Server.getClientFromID(2)));
		assertTrue("Client 3 doesn't match",
				client3.equals(Server.getClientFromID(3)));
		assertTrue("Client 4 doesn't match",
				client4.equals(Server.getClientFromID(4)));
	}
	
	/**
	 * Tests that only 30 clients can connect to the server at once.
	 */
	@Test
	public void testMaxClientsEnforced() {
		// Add thirty connections
		for (int i = 0; i < 30; i++) {
			Client c = Server.handleClient(-1, "TestClient" + i, false, 0, 0);
			
			// Check that the client has been created
			assertFalse("Client " + i + " has not been added",
					c == null);
		}
		
		// Try to add another client
		Client overflowClient = Server.handleClient(-1,
				"TestClientOverflow", false, 0, 0);
		
		// Check that the overflow client hasn't been created
		assertTrue("The overflow client has been added",
				overflowClient == null);
	}
	
	/**
	 * Tests that permitted versions are removed correctly.
	 */
	@Test
	public void testRemoveVersion() {
		// Add some allowed versions
		String ver1 = "Fly-Hard-0.1.0";
		String ver2 = "Fly-Hard-0.1.2";
		String ver3 = "Fly-Hard-0.1.8";
		String ver4 = "Fly-Hard-0.2.1";
		String ver5 = "Fly-Hard-0.2.5";
		String ver6 = "Fly-Hard-0.3.0";
		String ver7 = "Fly-Hard-0.4.0";
		String ver8 = "Fly-Hard-0.6.7";
		String ver9 = "Fly-Hard-0.9.1";
		String ver10 = "Fly-Hard-0.9.9";
		
		Server.addVersion(ver1);
		Server.addVersion(ver2);
		Server.addVersion(ver3);
		Server.addVersion(ver4);
		Server.addVersion(ver5);
		Server.addVersion(ver6);
		Server.addVersion(ver7);
		Server.addVersion(ver8);
		Server.addVersion(ver9);
		Server.addVersion(ver10);
		
		// Remove some versions
		Server.removeVersion(ver1);
		Server.removeVersion(ver4);
		Server.removeVersion(ver8);
		
		// Check that these versions have been removed
		assertFalse("Version 1 is still present",
				Server.getPermittedVersions().contains(ver1));
		assertFalse("Version 4 is still present",
				Server.getPermittedVersions().contains(ver4));
		assertFalse("Version 8 is still present",
				Server.getPermittedVersions().contains(ver8));
		
		// Check that the other versions are still present
		assertTrue("Version 2 is *not* present",
				Server.getPermittedVersions().contains(ver2));
		assertTrue("Version 3 is *not* present",
				Server.getPermittedVersions().contains(ver3));
		assertTrue("Version 5 is *not* present",
				Server.getPermittedVersions().contains(ver5));
		assertTrue("Version 6 is *not* present",
				Server.getPermittedVersions().contains(ver6));
		assertTrue("Version 7 is *not* present",
				Server.getPermittedVersions().contains(ver7));
		assertTrue("Version 9 is *not* present",
				Server.getPermittedVersions().contains(ver9));
		assertTrue("Version 10 is *not* present",
				Server.getPermittedVersions().contains(ver10));
	}
	
	/**
	 * Tests that high scores are removed correctly.
	 */
	@Test
	public void testRemoveHighScore() {
		// Add some high scores
		Server.addHighScore("TestClient1", 0);
		Server.addHighScore("TestClient2", 0);
		Server.addHighScore("TestClient1", 1000);
		Server.addHighScore("TestClient1", -200);
		Server.addHighScore("TestClient3", 1000);
		Server.addHighScore("TestClient4", 2400);
		
		// Remove some high scores
		Server.removeHighScore("TestClient3", 1000);
		Server.removeHighScore("TestClient1", -200);
		
		// Check that the high scores have been removed
		assertFalse("TestClient3's score of 1000 remains",
				Server.getHighScores().get(1000L).contains("TestClient3"));
		assertFalse("TestClient1's score of -200 remains",
				Server.getHighScores().get(-200L).contains("TestClient1"));
		
		// Check that the scores are still present
		assertTrue("TestClient1's score of 0 is missing",
				Server.getHighScores().get(0L).contains("TestClient1"));
		assertTrue("TestClient2's score of 0 is missing",
				Server.getHighScores().get(0L).contains("TestClient2"));
		assertTrue("TestClient1's score of 1000 is missing",
				Server.getHighScores().get(1000L).contains("TestClient1"));
		assertTrue("TestClient4's score of 2400 is missing",
				Server.getHighScores().get(2400L).contains("TestClient4"));
	}
	
}
