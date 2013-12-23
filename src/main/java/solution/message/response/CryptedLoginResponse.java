package solution.message.response;

import message.Response;


public class CryptedLoginResponse implements Response {
	private static final long serialVersionUID = 3134831924072300109L;

	private final String clChallenge;
	private final String proxyChallenge;
	private final String key;
	private final String iv;

	public CryptedLoginResponse(String clChallenge, String proxyChallenge, String key, String iv) {
		this.clChallenge = clChallenge;
		this.proxyChallenge = proxyChallenge;
		this.key = key;
		this.iv = iv;
	}
	

	/**
	 * @return the clChallenge
	 */
	public String getClChallenge() {
		return clChallenge;
	}



	/**
	 * @return the proxyChallenge
	 */
	public String getProxyChallenge() {
		return proxyChallenge;
	}



	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}



	/**
	 * @return the iv
	 */
	public String getIv() {
		return iv;
	}



	@Override
	public String toString() {
		return String.format("!ok %s %s %s %s", getClChallenge(), getProxyChallenge(), getKey(), getIv());
	}
}
