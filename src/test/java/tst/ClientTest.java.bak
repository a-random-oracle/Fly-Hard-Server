package tst;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import srv.Client;
import srv.Server;

public class ClientTest {
	
	/** The client to use for test operations */
	private Client testClient;
	
	/**
	 * Sets up a test client.
	 */
	@Before
	public void setUpClient() {
		// Reset the server
		Server.reset();
		
		testClient = new Client();
	}
	
	//public void test
	
	/**
	 * Tests that the last connection time is updated correctly
	 * (i.e. is greater than its previous value).
	 */
	@Test
	public void testUpdateLastConnectionTime() {
		// Get the current latest connection time
		long lastConnectionTime = testClient.getLastConnection();
		
		// Wait for 10 milliseconds
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Update the last connection time
		testClient.updateLastConnectionTime();
		
		// Get the new last connection time
		long newLastConnectionTime = testClient.getLastConnection();
		
		// Check that the last connection time is more recent than the
		// previously retrieved value
		assertTrue("Last connection time has not been updated",
				lastConnectionTime < newLastConnectionTime);
		
		// Check that the time difference between the two checks is roughly
		// 10 milliseconds
		assertTrue("Last connection time is outside (>) the expcted range",
				(newLastConnectionTime - lastConnectionTime) < 20);
		
		assertFalse("Last connection time is outside (<) the expcted range",
				(newLastConnectionTime - lastConnectionTime) < 10);
	}

	/**
	 * Tests that (basic) data is written to and read from the client's
	 * data buffer correctly.
	 */
	@Test
	public void testReadWriteDataBasic() {
		// Test with basic data -----------------------------------------------
		
		// Construct some test data
		String testData = "Test1";
		
		// Write the data to the test client's data buffer
		testReadWriteDataHelper(0, testData.getBytes());
		
		// Read data from the test client's data buffer
		byte[] readData = testClient.readLatestData().getValue();
		
		// Check that the read data matches the test data
		assertTrue("The data returned does not equal the test data",
				testData.equals(new String(readData)));
		
		// Check that the client's data buffer has been cleared
		assertTrue("The client's data buffer is not empty",
				testClient.readLatestData() == null);
	}
	
	/**
	 * Tests that (512 characters of) data is written to and read from the client's
	 * data buffer correctly.
	 */
	@Test
	public void testReadWriteDataLong() {
		// Construct some test data
		String testData = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

		// Write the data to the test client's data buffer
		testReadWriteDataHelper(0, testData.getBytes());

		// Read data from the test client's data buffer
		byte[] readData = testClient.readLatestData().getValue();

		// Check that the read data matches the test data
		assertTrue("The data returned does not equal the test data",
				testData.equals(new String(readData)));

		// Check that the client's data buffer has been cleared
		assertTrue("The client's data buffer is not empty",
				testClient.readLatestData() == null);
	}
	
	/**
	 * Tests that (multiple) data is written to and read from the client's
	 * data buffer correctly.
	 */
	@Test
	public void testReadWriteDataTwice() {
		// Construct some test data
		String testDataA = "Test3a";

		// Construct some more test data
		String testDataB = "Test3b";

		// Write the data to the test client's data buffer
		testReadWriteDataHelper(0, testDataA.getBytes());
		testReadWriteDataHelper(100, testDataB.getBytes());

		// Read data from the test client's data buffer
		byte[] readData = testClient.readLatestData().getValue();

		// Check that the read data matches the second test data
		assertTrue("The data returned does not equal the test data",
				testDataB.equals(new String(readData)));

		// Check that the client's data buffer has been cleared
		assertTrue("The client's data buffer is not empty",
				testClient.readLatestData() == null);
	}
	
	/**
	 * Tests that (priority) data is written to and read from the client's
	 * data buffer correctly.
	 */
	@Test
	public void testReadWritePriority() {
		// Construct some test data
		String testDataA = "TestA";

		// Construct some more test data
		String testDataB = "TestB";

		// Write the data to the test client's data buffer
		testReadWriteDataHelper(-1, testDataA.getBytes());
		testReadWriteDataHelper(100, testDataB.getBytes());

		// Read data from the test client's data buffer
		byte[] readData = testClient.readLatestData().getValue();

		// Check that the read data matches the second test data
		assertTrue("The data returned does not equal the test data",
				testDataA.equals(new String(readData)));

		// Check that the client's data buffer has *not* been cleared
		assertFalse("The client's data buffer is empty",
				testClient.readLatestData() == null);
	}
	
	/**
	 * Writes the specified data to the test client's data buffer.
	 * @param index - the index at which to insert the data
	 * @param data - the data to write to the test client's data buffer
	 */
	private void testReadWriteDataHelper(long index, byte[] data) {
		// Construct a test map
		TreeMap<Long, byte[]> testMap = new TreeMap<Long, byte[]>();
		
		// Add the test data to the map
		testMap.put(index, data);
		
		// Add test data to the test client's data buffer
		testClient.writeData(testMap.firstEntry());
	}
	
	/**
	 * Tests that the check for messages is performed correctly.
	 * <p>
	 * In order to work, this method requires the messages attribute in Client
	 * to be made public.
	 * </p>
	 */
	@Test
	public void testCheckForMessages() {
		// Check that the message string is empty to begin with
		assertFalse("Message string has started with content",
				testClient.checkForMessages());
		
		// Check that after data has been written, that it returns true
		testClient.writeMessage("TEST_MESSAGE");
		
		assertTrue("Message string is returning that there is no content",
				testClient.checkForMessages());
		
		// Check that after data has been read, that it returns false
		testClient.readMessages();
		assertFalse(testClient.checkForMessages());
	}

	/**
	 * Tests that (basic) messages are written to and read from the client's
	 * message buffer correctly.
	 */
	@Test
	public void testReadWriteMessagesBasic() {
		// Test with a simple message -----------------------------------------
		
		// Write the message to the test client's message buffer
		String testMessage1 = "TEST 1";
		testClient.writeMessage(testMessage1);
		
		// Read messages from the test client's message buffer
		String readMessage1 = testClient.readMessages();
		
		// Check that the read message matches the test message
		assertTrue("The message returned does not equal the test message",
				readMessage1.equals(new String(testMessage1)));
		
		// Check that the client's message buffer has been cleared
		assertFalse(testClient.checkForMessages());
	}
	
	/**
	 * Tests that (long) messages are written to and read from the client's
	 * message buffer correctly.
	 */
	@Test
	public void testReadWriteMessagesLong() {
		// Construct some test messages
		String testMessage2 = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		
		// Write the message to the test client's message buffer
		testClient.writeMessage(testMessage2);
		
		// Read messages from the test client's message buffer
		String readMessage2 = testClient.readMessages();
		
		// Check that the read message matches the test message
		assertTrue("The message returned does not equal the test message",
				readMessage2.equals(new String(testMessage2)));

		// Check that the client's message buffer has been cleared
		assertFalse(testClient.checkForMessages());
	}
	
	/**
	 * Tests that (multiple) messages are written to and read from the client's
	 * message buffer correctly.
	 */
	@Test
	public void testReadWriteMessagesMultiple() {
		// Construct some test messages
		String testMessage3a = "TEST 3a";
		String testMessage3b = "TEST 3b";

		// Write the messages to the test client's message buffer
		testClient.writeMessage(testMessage3a);
		testClient.writeMessage(testMessage3b);

		// Read messages from the test client's message buffer
		String readMessage3 = testClient.readMessages();

		// Check that the read message matches the concatenation of
		// the test messages
		assertTrue("The message returned does not equal the test message",
				readMessage3.equals(new String(testMessage3a)
				+ Client.MESSAGE_DELIM + new String(testMessage3b)));

		// Check that the client's message buffer has been cleared
		assertFalse(testClient.checkForMessages());
	}
	
	/**
	 * Tests that (many multiple) messages are written to and read from the client's
	 * message buffer correctly.
	 */
	@Test
	public void testReadWriteMessagesManyMultiple() {
		// Construct some test messages
		ArrayList<String> testMessageArray = new ArrayList<String>();

		for (int i = 0; i < 1000; i++) {
			testMessageArray.add("TEST 4." + i);
		}

		// Write the messages to the test client's message buffer
		for (int i = 0; i < 1000; i++) {
			testClient.writeMessage(testMessageArray.get(i));
		}
		
		// Construct the expected result
		String expectedResult = "";
		for (int i = 0; i < 1000; i++) {
			if (i == 0) {
				expectedResult = testMessageArray.get(i);
			} else {
				expectedResult += Client.MESSAGE_DELIM + testMessageArray.get(i);
			}
		}

		// Read messages from the test client's message buffer
		String readMessage4 = testClient.readMessages();

		// Check that the read message matches the concatenation of
		// the test messages
		assertTrue("The message returned does not equal the test message",
				readMessage4.equals(expectedResult));

		// Check that the client's message buffer has been cleared
		assertFalse(testClient.checkForMessages());
	}

}
