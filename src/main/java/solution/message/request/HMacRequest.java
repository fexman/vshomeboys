package solution.message.request;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import message.Request;

public class HMacRequest implements Request {

	private static final long serialVersionUID = -7543756483472725754L;
	
	private Request request;
	private byte[] hMac;
	private String base64hMac;
	
	public HMacRequest(Request request, Key secretKey) throws InvalidKeyException {
		this.request = request;
		Mac hMacCrypt;
		try {
			hMacCrypt = Mac.getInstance("HmacSHA256");
			hMacCrypt.init(secretKey);
			hMacCrypt.update(request.toString().getBytes());
			this.hMac = hMacCrypt.doFinal();
		} catch (NoSuchAlgorithmException e) {
			//Should not happen
		} 
		this.base64hMac = Base64.encode(hMac);

	}
	
	public Request getRequest() {
		return request;
	}
	
	public String getBase64HMac() {
		return base64hMac;
	}
	
	public byte[] getHMac() {
		return hMac;
	}

	public String toString() {
		return base64hMac + " " + request.toString();
	}

}
