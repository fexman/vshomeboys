package solution.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Base64TCPChannel extends TCPChannel implements Channel{

	public Base64TCPChannel(InetAddress address, int port) throws IOException {
		super(address, port);
	}
	
	
	public Base64TCPChannel(Socket socket) throws IOException {
		super(socket);
	}
	
	@Override
	public void transmit(Serializable s) throws IOException {
			super.transmit(base64encode(s));
	}

	@Override
	public Serializable receive() throws IOException {
			return base64decode((String) super.receive());
	}

	@Override
	public Serializable contact(Serializable s) throws IOException {
		transmit(s);
		return receive();
	}
	
	private String base64encode(Serializable s) {
		String encodedData = "";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baos).writeObject(s);
			encodedData = Base64.encode(baos.toByteArray());
		} catch (IOException e) {
			System.err.println("serialization error");
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				//Ignore
			}
		}
		return encodedData;
	}
	
	private Serializable base64decode(String encodedData) {
		byte[] bytes = new byte[0];
		try {
			bytes = Base64.decode(encodedData);
		} catch (Base64DecodingException e) {
			System.err.println("base64decode error");
		}
		ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
		Serializable s = null;
		try {
			s = (Serializable) new ObjectInputStream(baos).readObject();
			baos.close();
		} catch (IOException e) {
			System.err.println("serialization error");
		} catch (ClassNotFoundException e) {
			System.err.println("received non-serializable object");
		}
		return s;
	}

}
