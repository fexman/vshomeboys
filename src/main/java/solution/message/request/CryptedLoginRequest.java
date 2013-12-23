package solution.message.request;

import message.Request;


public class CryptedLoginRequest implements Request {
	private static final long serialVersionUID = -1596776158259072949L;

	private final String username;
	private final String challenge;

	public CryptedLoginRequest(String username, String challenge) {
		this.username = username;
		this.challenge = challenge;
	}

	public String getUsername() {
		return username;
	}

	public String getChallenge() {
		return challenge;
	}

	@Override
	public String toString() {
		return String.format("!login %s %s", getUsername(), getChallenge());
	}
}
