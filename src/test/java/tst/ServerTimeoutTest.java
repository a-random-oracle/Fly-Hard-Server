package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import srv.Client;
import srv.Server;

public class ServerTimeoutTest {
	
	/**
	 * Ensure that the timer clear before each test.
	 */
	@Before
	public void setUpTests() {
		// Reset the server
		Server.reset();
		Server.clearRemoveClientsTimer();
	}
	
	/**
	 * Tests that the timer removes clients correctly.
	 * <p>
	 * NOTE: this waits for the timer to fire twice, and so may take several
	 * seconds to complete.
	 * </p>
	 */
	@Test
	public void testClientTimeout() {
		// Add a test client
		Client testClient = new Client();
		Server.getClients().add(testClient);
		
		// Wait for the timer to fire twice
		try {
			Thread.sleep((long) (Server.timeout * (12d/5d)));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Check that the client has been removed
		assertFalse("The test client is still connected to the server",
				Server.getClients().contains(testClient));
	}
	
	/**
	 * Tests that the timer removes only clients which haven't connected
	 * recently.
	 * <p>
	 * NOTE: this waits for the timer to fire twice, and so may take several
	 * seconds to complete.
	 * </p>
	 */
	@Test
	public void testCorrectClientTimeout() {
		// Add some test clients
		Client testClient1 = new Client();
		Client testClient2 = new Client();
		Client testClient3 = new Client();
		
		Server.getClients().add(testClient1);
		Server.getClients().add(testClient2);
		Server.getClients().add(testClient3);
		
		// Delay starting the timer
		Server.clearRemoveClientsTimer();
		try {
			// -3 seconds
			Thread.sleep((long) (Server.timeout * (3d/5d)));
			// 0 seconds
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Server.startRemoveClientsTimer();
		
		// Wait until just before the timer fires
		try {
			// 0 seconds
			Thread.sleep((long) (Server.timeout * (3d/5d)));
			// 3 seconds
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Activate the first and third test clients
		testClient1.updateLastConnectionTime();
		testClient3.updateLastConnectionTime();

		// Wait until just before the timer fires for a second time
		try {
			// 3 seconds
			Thread.sleep((long) (Server.timeout * (5d/5d)));
			// 8 seconds
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Check that the second test client has been removed,
		// and that both the first and third test clients remain
		assertTrue("The first test client is not connected to the server",
				Server.getClients().contains(testClient1));
		assertFalse("The second test client is still connected to the server",
				Server.getClients().contains(testClient2));
		assertTrue("The third test client is not connected to the server",
				Server.getClients().contains(testClient3));

		// Activate the third test client
		testClient3.updateLastConnectionTime();
		
		// Wait for the timer to fire for a third time
		try {
			// 8 seconds
			Thread.sleep((long) (Server.timeout * (4d/5d)));
			// 12 seconds
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Check that the first and second test clients have been removed,
		// and that the third test client remains
		assertFalse("The first test client is still connected to the server",
				Server.getClients().contains(testClient1));
		assertFalse("The second test client is still connected to the server",
				Server.getClients().contains(testClient2));
		assertTrue("The third test client is not connected to the server",
				Server.getClients().contains(testClient3));
	}

}
