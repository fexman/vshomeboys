package solution.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class CryptoUtil {
	
	private CryptoUtil() {
		
	}
	
	public static String base64Encode(Serializable s) {
		return Base64.encode(serialize(s));
	}
	
	public static Serializable base64Decode(String encodedData) {
		try {
			return deserialize(Base64.decode(encodedData));
		} catch (Base64DecodingException e) {
			System.err.println("base64decode error");
			return null;
		}
	}
	
	public static byte[] serialize(Serializable s) {
		byte[] serialized = new byte[0];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baos).writeObject(s);
			serialized = baos.toByteArray();
		} catch (IOException e) {
			System.err.println("serialization error");
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				//Ignore
			}
		}
		return serialized;
	}
	
	public static Serializable deserialize(byte[] data) {
		ByteArrayInputStream baos = new ByteArrayInputStream(data);
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