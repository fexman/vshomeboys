package solution.message.response;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

import message.Response;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class HMacResponse implements Response {

	private static final long serialVersionUID = -6340570061228788591L;
	
	private Response response;
	private byte[] hMac;
	private String base64hMac;
	
	public HMacResponse(Response response, Key secretKey) throws InvalidKeyException {
		this.response = response;
		Mac hMacCrypt;
		try {
			hMacCrypt = Mac.getInstance("HmacSHA256");
			hMacCrypt.init(secretKey);
			hMacCrypt.update(response.toString().getBytes());
			this.hMac = hMacCrypt.doFinal();
		} catch (NoSuchAlgorithmException e) {
			//Should not happen
		} 
		this.base64hMac = Base64.encode(hMac);
	}
	
	public Response getResponse() {
		return response;
	}
	
	public String getBase64HMac() {
		return base64hMac;
	}
	
	public byte[] getHMac() {
		return hMac;
	}

	public String toString() {
		return base64hMac + " " + response.toString();
	}

}