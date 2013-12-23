package solution.message.response;

import message.Response;

public class CryptedLoginConfirmationResponse implements Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3409604284610052268L;
	
	private final String proxyChallenge;
	
	public CryptedLoginConfirmationResponse(String proxyChallenge) {
		this.proxyChallenge = proxyChallenge;
	}	
	
	/**
	 * @return the proxyChallenge
	 */
	public String getProxyChallenge() {
		return proxyChallenge;
	}

	public String toString() {
		return getProxyChallenge();
	}

}
