package solution.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import solution.AbstractTcpServer;
import solution.communication.TCPChannel;
import solution.fileserver.FileServer;
import solution.model.MyFileServerInfo;
import solution.model.MyUserInfo;
import util.ChecksumUtils;

public class Proxy extends AbstractTcpServer implements IProxy {

	private ConcurrentHashMap<String, MyUserInfo> users;
	private ConcurrentHashMap<MyFileServerInfo, Long> fileservers;

	private MyUserInfo user;
	private static final MessageResponse RESPONSE_NOT_LOGGED_IN = new MessageResponse(
			"Error: No user logged in. Login first!");

	// TODO what is this?
	public Proxy(final TCPChannel tcpChannel, Set<AbstractTcpServer> connections) throws IOException {
		super(tcpChannel, connections);
		throw new IOException("Sorry, can't construct Proxy that way! :(");
	}

	public Proxy(final TCPChannel tcpChannel, final ConcurrentHashMap<String, MyUserInfo> users,
			final ConcurrentHashMap<MyFileServerInfo, Long> fileservers, final Set<AbstractTcpServer> connections)
			throws IOException {
		super(tcpChannel, connections);

		this.users = users;
		this.fileservers = fileservers;
	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		println("Got login request for: " + request.getUsername());
		MyUserInfo u = users.get(request.getUsername());
		if (u == null) { // User non existent
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
		if (!u.login(request.getPassword())) { // Wrong password, or already
												// logged in
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
		this.user = u;
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
		TCPChannel fsChannel = null;
		try {
			fsChannel = new TCPChannel(mfs.getAddress(),mfs.getPort());
			Response resp = (Response)fsChannel.contact(request);
			fsChannel.close();
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