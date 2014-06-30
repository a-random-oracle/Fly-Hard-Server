package tst;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;

/**
 * Class used for testing servlet output streams.
 * <p>
 * ServletOutputStreams cannot be used in their raw form -
 * this class provides a simple wrapper to enable their use
 * in the servlet tests.
 * </p>
 */
public class MockServletOutputStream extends ServletOutputStream {
	
	
	/** The underlying output stream */
	private ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();
	
	
	@Override
	public void write(int b) throws IOException {
		byteArrayOutputStream.write(b);
	}
	
	/**
	 * Reads a string value from the stream.
	 * @return the stream read as a string
	 */
	public String getAsString() {
		final ByteArrayInputStream byteArrayInputStream =
				new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		
		try {
			return (String)
					(new ObjectInputStream(byteArrayInputStream)).readObject();
		} catch (ClassNotFoundException | IOException e) {
			//
		}
		
		return null;
	}
	
	/**
	 * Reads a byte array from the stream.
	 * @return the stream read as a byte array
	 */
	@SuppressWarnings("unchecked")
	public Entry<Long, byte[]> getAsByteArray() {
		final ByteArrayInputStream byteArrayInputStream =
				new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		try {
			return (Entry<Long, byte[]>)
					(new ObjectInputStream(byteArrayInputStream)).readObject();
		} catch (ClassNotFoundException | IOException e) {
			//
		}
		
		return null;
	}
	
}