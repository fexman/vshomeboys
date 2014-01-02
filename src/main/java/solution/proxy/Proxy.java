package solution.proxy;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import cli.Command;
import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.BuyResponse;
import message.response.CreditsResponse;
import message.response.DownloadTicketResponse;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import model.DownloadTicket;
import proxy.IProxy;
import solution.AbstractServer;
import solution.communication.AESOperator;
import solution.communication.Channel;
import solution.communication.BiDirectionalRsaOperator;
import solution.communication.TcpChannel;
import solution.message.request.CryptedLoginRequest;
import solution.message.request.HMacRequest;
import solution.message.response.CryptedLoginConfirmationResponse;
import solution.message.response.CryptedLoginResponse;
import solution.message.response.HMacErrorResponse;
import solution.message.response.HMacResponse;
import solution.model.MyFileServerInfo;
import solution.model.MyUserInfo;
import util.ChecksumUtils;

public class Proxy extends AbstractServer implements IProxy {

	private ConcurrentHashMap<String, MyUserInfo> users;
	private ConcurrentHashMap<MyFileServerInfo, Long> fileservers;
	
	private String pathToPrivateKey;
	private String pathToKeys;
	
	private Channel channel;

	private MyUserInfo user;
	private static final MessageResponse RESPONSE_NOT_LOGGED_IN = new MessageResponse(
			"Error: No user logged in. Login first!");

/*
	public Proxy(final Channel channel, Set<AbstractServer> connections, Key HMacKey) throws IOException {
		super(channel, connections, HMacKey);
		throw new IOException("Sorry, can't construct Proxy that way! :(");
	}
*/
	public Proxy(final Channel channel, final Set<AbstractServer> connections, Key HMacKey, final ConcurrentHashMap<String, MyUserInfo> users,
			final ConcurrentHashMap<MyFileServerInfo, Long> fileservers, String pathToPrivateKey, String pathToKeys)
			throws IOException {
		super(channel, connections, HMacKey);

		this.channel = channel;
		this.users = users;
		this.fileservers = fileservers;
		
		this.pathToPrivateKey = pathToPrivateKey;
		this.pathToKeys = pathToKeys;
	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		println("Got outdated login request for: " + request.getUsername());
		println("This service is no longer supported.");
		return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
	}
	
	@Command
	public LoginResponse login(CryptedLoginRequest request) {
		
		println("Got login request for: " + request.getUsername());
		MyUserInfo u = users.get(request.getUsername());
		
		if (u.isOnline()) {
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
		
		try {
			//Set 1dRSA to 2dRSA
			channel.getOperators().set(0, new BiDirectionalRsaOperator(pathToKeys+"/"+u.getName()+".pub.pem",pathToPrivateKey,"12345"));
		} catch (IOException e) {
			println("RSA-Channel creation failed: " + e.getMessage());
			shutDown();
		}
		
		//CHALLENGE
		SecureRandom secureRandom = new SecureRandom(); 
		byte[] number = new byte[32]; 
		secureRandom.nextBytes(number);
		String challenge = Base64.encode(number);
		
		//SECRET KEY
		KeyGenerator generator = null;
		try {
			generator = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e1) {
			//WON'T HAPPEN!
		} 
		generator.init(128); 
		SecretKey key = generator.generateKey(); 
		String keyStr = Base64.encode(key.getEncoded());
		
		//IV
		number = new byte[16]; 
		secureRandom.nextBytes(number);
		String iv = Base64.encode(number);
		
		try {
			channel.transmit(new CryptedLoginResponse(request.getChallenge(),challenge,keyStr,iv));
		} catch (IOException e) {
			println("CryptedLoginResponse transmit failed!");
			shutDown();
		}
		
		//SWITCH TO AES
		try {
			channel.getOperators().set(0, new AESOperator(key,number));
		} catch (IOException e1) {
			println("AES-Channel creation failed: " + e1.getMessage());
			shutDown();
		}
		
		
		try {
			CryptedLoginConfirmationResponse clcr = (CryptedLoginConfirmationResponse)channel.receive();
			if (!clcr.getProxyChallenge().equals(challenge)) {
				println("Wrong challenge received!");
				shutDown();
			}
		} catch (IOException e) {
			println("Receiving CryptedLoginConformationResponse failed!");
			shutDown();
		} catch (ClassCastException e) {
			println("Receiving CryptedLoginConformationResponse failed - received shit!");
			shutDown();
		}
		
		println("Everything okay! AES is up an running!");
		this.user = u;
		user.login();
		return new LoginResponse(LoginResponse.Type.SUCCESS);
	}

	@Override
	public Response credits() throws IOException {
		println("Got credits request.");
		if (user == null) {
			return RESPONSE_NOT_LOGGED_IN;
		}
		return new CreditsResponse(user.getCredits());
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		println("Got buy request.");
		if (user == null) {
			return RESPONSE_NOT_LOGGED_IN;
		}
		user.modifyCredits(credits.getCredits());
		return new BuyResponse(user.getCredits());
	}

	@Override
	public Response list() throws IOException {

		println("Got list request.");
		if (user == null) {
			return RESPONSE_NOT_LOGGED_IN;
		}

		List<MyFileServerInfo> servers = getAllOnlineFileServersByUsage();
		Set<String> fileNames = new HashSet<String>();

		for (MyFileServerInfo i : servers) {

			ListResponse resp = receiveListResponseFromServer(i, new ListRequest());
			if (resp != null) {
				for (String s : resp.getFileNames()) {
					fileNames.add(s);
				}
			}
		}

		return new ListResponse(fileNames);
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		println("Got download request for: " + request.getFilename());
		if (user == null) {
			return RESPONSE_NOT_LOGGED_IN;
		}

		List<MyFileServerInfo> servers = getOnlineFileServersByUsage(computeReadQ());
		println("servers online: " + getAllOnlineFileServersByUsage().size());
		println("read quorum = " + computeReadQ());
		println("write quorum = " + computeWriteQ());
		/*
		 * first, determine the highest version number
		 */
		List<MyFileServerInfo> chosen = new ArrayList<MyFileServerInfo>();
		int latestVersion = -1;

		for (MyFileServerInfo i : servers) {

			VersionResponse resp = receiveVersionResponseFromServer(i, new VersionRequest(request.getFilename()));
			if (resp != null && resp.getVersion() > latestVersion) {
				println("added server: " + i + ", new version: " + latestVersion);
				latestVersion = resp.getVersion();
				chosen.add(i);
			}
		}

		/*
		 * Because servers is sorted by usage, chosen also has to be sorted
		 * already. Therefore the last element of chosen is the one with the
		 * lowest usage of all servers, that store the file in its highest
		 * version.
		 */

		if (!chosen.isEmpty()) { // so the file exists

			MyFileServerInfo server = chosen.get(chosen.size() - 1);
			println("Chosen server : " + server + ", latest fileversion: " + latestVersion);

			InfoResponse resp = receiveInfoResponseFromServer(server, new InfoRequest(request.getFilename()));

			if (resp != null) {
				if (resp.getSize() > 0) {

					long size = resp.getSize();
					if (user.getCredits() < resp.getSize()) {
						return new MessageResponse("Not enough credits!");
					}

					String checksum = ChecksumUtils.generateChecksum(user.getName(), request.getFilename(),
							latestVersion, size);

					DownloadTicket ticket = new DownloadTicket(user.getName(), request.getFilename(), checksum,
							server.getAddress(), server.getPort());
					user.modifyCredits(-size);
					server.use(size);

					return new DownloadTicketResponse(ticket);
				}
			}
		}
		return new MessageResponse("File not found!");
	}

	@Override
	// TODO usage of servers is not increased after upload (commented)
	public MessageResponse upload(UploadRequest request) throws IOException {

		String filename = request.getFilename();
		println("Got upload request for: " + filename);
		println("write quorum: " + computeWriteQ());

		if (user == null) {
			return RESPONSE_NOT_LOGGED_IN;
		}

		boolean uploadFailed = false;
		long size = request.getContent().length;

		List<MyFileServerInfo> servers = getOnlineFileServersByUsage(computeReadQ());
		int version = -1;

		/*
		 * first, determine the appropriate version number, according to
		 * Gifford's scheme
		 */
		for (MyFileServerInfo i : servers) {

			VersionResponse resp = receiveVersionResponseFromServer(i, new VersionRequest(filename));

			if (resp != null) {

				version = Math.max(resp.getVersion(), version);
			}
		}
		println("latest version so far: " + version);

		/*
		 * next, the file is uploaded to the write quorum
		 */
		servers = getOnlineFileServersByUsage(computeWriteQ());
		println("number of servers, that receive the file = " + servers.size() + " (should be write-quorum)");
		for (MyFileServerInfo i : servers) {

			MessageResponse resp = receiveMessageResponseFromServer(i, new UploadRequest(request.getFilename(),
					version + 1, request.getContent()));
			if (resp != null) {
				if (resp.getMessage().equals("Error: Fileserver could not write file.")) {
					// currentFS.use(size);
					uploadFailed = true;
				}
			}
		}

		if (!uploadFailed) {
			user.modifyCredits(2 * size);
			return new MessageResponse("Upload successful!");
		} else {
			return new MessageResponse("Could not upload file!");
		}
	}

	@Override
	public MessageResponse logout() throws IOException {
		println("Got logout request.");
		if (user == null) {
			return RESPONSE_NOT_LOGGED_IN;
		}
		user.logout();
		stopListening();
		return new MessageResponse("Successfully logged out.");
	}

	protected void customShutDown() {
		if (user != null) {
			user.logout();
		}
	}

	/**
	 * Send request to given fileserver and return it's response
	 * 
	 * @param mfs
	 * @param request
	 * @return null in case of transmit errors (connection-errors, received data
	 *         is no response-object)
	 * @throws IOException
	 */
	private Response receiveResponseFromServer(MyFileServerInfo mfs, Request request) throws IOException {
		TcpChannel fsChannel = null;
		boolean receivedValid = false;
		try {
			Response resp = null;
			int i = 1;
			while (!receivedValid && i <= 10) { //Repeat complete request-operation if haven't received valid response yet, will repeat 10 times
				
				fsChannel = new TcpChannel(mfs.getAddress(),mfs.getPort());
				
				try {
					resp = (Response)fsChannel.contact(new HMacRequest(request,hMACKey));
					if (resp instanceof HMacErrorResponse) {
						
						println("Received HMacErrorResponse from fileserver.");
						
					} else {
						
						HMacResponse hresp = (HMacResponse)resp;
						resp = hresp.getResponse();
						HMacResponse verif = new HMacResponse(resp,hMACKey);
						if (Arrays.equals(hresp.getHMac(),verif.getHMac())) {
							receivedValid = true;
						} else {
							println("Received HMAC-response from fileserver: Invalid HMAC!\nRECEIVED: " + hresp.toString() + "\nEXPECTED: " + verif.toString());
						}
						println("Received HMAC-response from fileserver: HMAC was ok.");
					}
					
				} catch (InvalidKeyException e) {
					println("Invalid HMAC-Key!");
				}
				fsChannel.close();
				i++;
			}
			if (i >= 10) {
				return null;
			}
			return resp;
		} catch (ClassCastException e) {
			fsChannel.close();
			println("Received strange data via TCP, a ClassCastException occured!");
			return null;
		}
	}

	private MessageResponse receiveMessageResponseFromServer(MyFileServerInfo mfs, Request request) throws IOException {

		Response r = receiveResponseFromServer(mfs, request);

		if (r instanceof MessageResponse) {
			return (MessageResponse) r;
		} else {
			return null;
		}
	}

	private VersionResponse receiveVersionResponseFromServer(MyFileServerInfo mfs, Request request) throws IOException {

		Response r = receiveResponseFromServer(mfs, request);

		if (r instanceof VersionResponse) {
			return (VersionResponse) r;
		} else {
			println("return null");
			return null;
		}
	}

	private InfoResponse receiveInfoResponseFromServer(MyFileServerInfo mfs, Request request) throws IOException {

		Response r = receiveResponseFromServer(mfs, request);

		if (r instanceof InfoResponse) {
			return (InfoResponse) r;
		} else {
			return null;
		}
	}

	private ListResponse receiveListResponseFromServer(MyFileServerInfo mfs, Request request) throws IOException {

		Response r = receiveResponseFromServer(mfs, request);

		if (r instanceof ListResponse) {
			return (ListResponse) r;
		} else {
			return null;
		}
	}

	/**
	 * Returns a map with all fileservers that are online.
	 */
	private ConcurrentHashMap<MyFileServerInfo, Long> onlineFileServers() {

		ConcurrentHashMap<MyFileServerInfo, Long> servers = new ConcurrentHashMap<MyFileServerInfo, Long>();

		for (MyFileServerInfo i : fileservers.keySet()) {

			if (i.isOnline()) {

				servers.put(i, fileservers.get(i));
			}
		}

		return servers;
	}

	/**
	 * Returns a List of all available (online) fileservers, sorted ascendingly
	 * by their usage.
	 */
	private List<MyFileServerInfo> getAllOnlineFileServersByUsage() {
		ArrayList<MyFileServerInfo> servers = new ArrayList<MyFileServerInfo>(onlineFileServers().keySet());
		java.util.Collections.sort(servers);
		return servers;
	}

	/**
	 * Returns a List of the first n available (online) fileservers, sorted
	 * ascendingly by their usage.
	 */
	private List<MyFileServerInfo> getOnlineFileServersByUsage(int n) {
		List<MyFileServerInfo> servers = getAllOnlineFileServersByUsage();
		return servers.subList(0, n);
	}

	/**
	 * Computes the read quorum, needed for Gifford's scheme
	 * 
	 * @return int > 0, that computeWriteQ() + computeReadQ() > serversOnline(),
	 *         but never more than the number of online servers.
	 */
	private int computeReadQ() {
		return Math.min(serversOnline() - computeWriteQ() + 1,serversOnline());
	}

	/**
	 * Computes the write quorum, needed for Gifford's scheme
	 * 
	 * @return int > 0, that computeWriteQ() > serversOnline() / 2, but never
	 *         more than the number of online servers.
	 */
	private int computeWriteQ() {
		return Math.min(((int) serversOnline() / 2) + 1, serversOnline());
	}

	/**
	 * Returns the number of servers online
	 * 
	 * @return int > 0, number of servers, where isOnline == true
	 */
	private int serversOnline() {

		return onlineFileServers().size();
	}
}