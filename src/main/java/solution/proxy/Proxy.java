package solution.proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
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
import solution.model.MyFileServerInfo;
import solution.model.MyUserInfo;
import util.ChecksumUtils;

public class Proxy extends AbstractTcpServer implements IProxy  {
	
	private ConcurrentHashMap<String,MyUserInfo> users;
	private ConcurrentHashMap<MyFileServerInfo,Long> fileservers;
	
	private MyUserInfo user;
	private static final MessageResponse RESPONSE_NOT_LOGGED_IN = new MessageResponse("Error: No user logged in. Login first!"); 

	public Proxy(Socket socket, Set<AbstractTcpServer> connections)
			throws IOException {
		super(socket, connections);
		throw new IOException("Sorry, can't construct Proxy that way! :(");
	}
	
	public Proxy (final Socket socket, final ConcurrentHashMap<String,MyUserInfo> users, final ConcurrentHashMap<MyFileServerInfo,Long> fileservers,final Set<AbstractTcpServer> connections) throws IOException {
		super(socket,connections);
		
		this.users = users;
		this.fileservers = fileservers;
		
	}
	
	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		println("Got login request for: " + request.getUsername());
		MyUserInfo u = users.get(request.getUsername());
		if (u == null) { //User non existent
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
		if(!u.login(request.getPassword())) { //Wrong password, or already logged in
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
		MyFileServerInfo[] fileServerArr = fileservers.keySet().toArray(new MyFileServerInfo[0]);
		HashSet<String> fileNames = new HashSet<String>();
		
		for (int i = fileServerArr.length -1;i >= 0; i--) {
			MyFileServerInfo currentFS = fileServerArr[fileServerArr.length - 1 - i];
			if (currentFS.isOnline()) {
					try {
						ListResponse resp = (ListResponse) contactFileServer(currentFS, new ListRequest());
						if (resp != null) {
							for (String s : resp.getFileNames()) {
								fileNames.add(s);
							}
						}
					} catch (ClassCastException e) { 
						//oops
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
		MyFileServerInfo[] fileServerArr = getFileServersByUsage();
		
		for (int i = fileServerArr.length -1;i >= 0; i--) {
			MyFileServerInfo currentFS = fileServerArr[fileServerArr.length - 1 - i];
			if (currentFS.isOnline()) {
				try {
					InfoResponse resp = (InfoResponse) contactFileServer(currentFS, new InfoRequest(request.getFilename()));
					if (resp != null)  {
						if (resp.getSize() > 0) {
							
		
							long size = resp.getSize();
							if (user.getCredits() < resp.getSize()) {
								return new MessageResponse("Not enough credits!");
							}
							
							VersionResponse resp1 = (VersionResponse) contactFileServer(currentFS, new VersionRequest(request.getFilename()));
							
							int version = resp1.getVersion();
							String checksum = ChecksumUtils.generateChecksum(user.getName(), request.getFilename(), version, size);
							
							DownloadTicket ticket = new DownloadTicket(user.getName(), request.getFilename(), checksum, currentFS.getAddress(),currentFS.getPort());
							user.modifyCredits(-size);
							currentFS.use(size);
							
							return new DownloadTicketResponse(ticket);
							
						}
					}
				} catch (ClassCastException e) { 
					//oops
				}
			}
		}
		return new MessageResponse("File not found!");
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {

		println("Got upload request for: " + request.getFilename());

		if (user == null) {
			return RESPONSE_NOT_LOGGED_IN;
		}
		
		boolean uploadHappened = false;
		long size = request.getContent().length;
		for (MyFileServerInfo currentFS : fileservers.keySet()) {
			if (currentFS.isOnline()) {
				try {
					MessageResponse resp = (MessageResponse) contactFileServer(currentFS, request);
					if (resp != null) {
						if (!resp.getMessage().equals("Error: Fileserver could not write file.")) {
							//currentFS.use(size);
							uploadHappened = true;
						}
					}
				} catch (ClassCastException e) { 
					//should not happen
				}
			}
		}

		if (uploadHappened) {
			user.modifyCredits(2*size);
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
	
	private MyFileServerInfo[] getFileServersByUsage() {
		MyFileServerInfo[] fileServerArr = fileservers.keySet().toArray(new MyFileServerInfo[0]);
		java.util.Arrays.sort(fileServerArr);
		return fileServerArr;
	}
	
	/**
	 * Send request to given fileserver and return it's response
	 * @param mfs 
	 * @param request
	 * @return null in case of transmit errors (connection-errors, received data is no response-object)
	 */
	private Response contactFileServer(MyFileServerInfo mfs, Request request) {
		Socket fSocket = null;
		ObjectOutputStream fOut = null;
		ObjectInputStream fIn = null;
		try {
			fSocket = new Socket(mfs.getAddress(),mfs.getPort());
			fOut = new ObjectOutputStream(fSocket.getOutputStream());
			fIn = new ObjectInputStream(fSocket.getInputStream());
			fOut.writeObject(request);
			Response resp = (Response)fIn.readObject();
			fSocket.close();
			return resp;
		} catch (IOException e) {
			return null;
		} catch (ClassCastException e) { 
			return null;
		} catch (ClassNotFoundException e) {
			return null;
			//should not happen
		} 

	}

}
