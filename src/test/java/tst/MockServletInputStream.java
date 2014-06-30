package tst;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.servlet.ServletInputStream;

/**
 * Class used for testing servlet input streams.
 * <p>
 * ServletInputStreams cannot be used in their raw form -
 * this class provides a simple wrapper to enable their use
 * in the servlet tests.
 * </p>
 */
public class MockServletInputStream extends ServletInputStream {
	
	
	/** The underlying output stream */
	private ByteArrayInputStream byteArrayInputStream = null;
	
	
	/**
	 * Constructs a new mock servlet input stream.
	 * <p>
	 * The stream will be set up with the object specified.
	 * </p>
	 * @param object - the object to be stored by the stream
	 */
	public MockServletInputStream(Object object) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = null;
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(object);
			objectOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byteArrayInputStream = new ByteArrayInputStream(
				byteArrayOutputStream.toByteArray());
	}
	
	
	@Override
	public int read() throws IOException {
		return byteArrayInputStream.read();
	}
	
}