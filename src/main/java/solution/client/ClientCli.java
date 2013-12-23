package solution.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.MissingResourceException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadFileRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.DownloadFileResponse;
import message.response.DownloadTicketResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import model.DownloadTicket;
import solution.communication.AESOperator;
import solution.communication.Base64Operator;
import solution.communication.Channel;
import solution.communication.ChannelOperator;
import solution.communication.BiDirectionalRsaOperator;
import solution.communication.TcpChannel;
import solution.message.request.CryptedLoginRequest;
import solution.message.response.CryptedLoginConfirmationResponse;
import solution.message.response.CryptedLoginResponse;
import solution.util.FileUtils;
import util.Config;
import cli.Command;
import cli.Shell;
import client.IClientCli;

public class ClientCli implements IClientCli {

	private Thread shellThread;
	private String path;
	private Channel proxyChannel;
	private String proxyHost;
	private int proxyPort;
	private boolean loggedIn;
	private String keyPath;
	private String proxyKeyPath;
	
	public static void main (String[] args) {
		try {
			new ClientCli(new Config("client"),new Shell("client",System.out, System.in));
		} catch (IOException e) {
			System.out.println("FAILED TO START CLIENT");
		}
	}
	
	public ClientCli(Config conf, Shell shell) throws IOException {
		
		try {
		path = conf.getString("download.dir");
		
		proxyPort = conf.getInt("proxy.tcp.port");
		proxyHost = conf.getString("proxy.host");
		
		keyPath = conf.getString("keys.dir") + "/";
		proxyKeyPath = conf.getString("proxy.key");
		
		loggedIn = false;
		
		shell.register(this);
		shellThread = new Thread(shell);
		shellThread.start();
		} catch (MissingResourceException e) {
			
			System.out.println("Invalid usage! Missing resource: " + e.getKey());
			
			try {
				exit();
			} catch (IOException e1) { //Should not happen!
				System.out.println("Boy, that escalated quickly!");
			}
			
		} 

	}
	
	@Command
	@Override
	public LoginResponse login(String username, String password) throws IOException {
	
		SecureRandom secureRandom = new SecureRandom(); 
		final byte[] number = new byte[32]; 
		secureRandom.nextBytes(number);
		String challenge = Base64.encode(number);
		
		try {
			if (proxyChannel == null) {
				 connectToProxy(username, password); //First time
				} else {
				if (!proxyChannel.connected()) { //Connection lost or cancelled
					 connectToProxy(username, password); //
				} else { //Already connected
					System.out.println("Already connected, logout first.");
					return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
		
		Response r = contactProxy(new CryptedLoginRequest(username,challenge));
		
		if (!(r instanceof CryptedLoginResponse)) {
			System.out.println("Proxy did not respond with CryptedLoginResponse when expected.");
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS); 
		}
		CryptedLoginResponse clr = (CryptedLoginResponse)r;
		
		if (!clr.getClChallenge().equals(challenge)) {
			System.out.println("Challenge was not returned correctly!");
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS); 
		}
		
		byte[] encodedKey = null;
		try {
			encodedKey = Base64.decode(clr.getKey());
		} catch (Base64DecodingException e) {
			System.out.println("AES-key decoding error!");
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS); 
		}
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		
		byte[] iv = null;
		try {
			iv = Base64.decode(clr.getIv());
		} catch (Base64DecodingException e) {
			System.out.println("AES-Iv decoding error!");
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS); 
		}
		
		try {
			proxyChannel.getOperators().set(0, new AESOperator(key,iv));
		} catch (IOException e1) {
			System.out.println("AES-Channel creation failed: " + e1.getMessage());
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
		
		proxyChannel.transmit(new CryptedLoginConfirmationResponse(clr.getProxyChallenge()));
		try {
		LoginResponse lr = (LoginResponse)proxyChannel.receive();
		} catch (ClassCastException e) {
			System.out.println("Last conformation was not as expected!");
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
		
		loggedIn = true;
		return new LoginResponse(LoginResponse.Type.SUCCESS);
	}

	@Command
	@Override
	public Response credits() throws IOException {
		return contactProxy(new CreditsRequest());
	}

	@Command
	@Override
	public Response buy(long credits) throws IOException {
		return contactProxy(new BuyRequest(credits));
	}

	@Command
	@Override
	public Response list() throws IOException {
		return contactProxy(new ListRequest());
	}

	@Command
	@Override
	public Response download(String filename) throws IOException {
		
		//Get ticket
		Response resp = contactProxy(new DownloadTicketRequest(filename));
		if (!(resp instanceof DownloadTicketResponse)) { //Probably messageresponse containing errormsg
			return resp;
		}
		
		//Get file
		DownloadTicket dt = ((DownloadTicketResponse)resp).getTicket();
		Response resp2 = contactFileServer(dt.getAddress(),dt.getPort(),new DownloadFileRequest(dt));
		if (!(resp2 instanceof DownloadFileResponse)) { //Probably messageresponse containing errormsg
			return resp;
		}
		DownloadFileResponse dr = (DownloadFileResponse) resp2;
		FileUtils.writeBytesToFile(path+ "/" + dt.getFilename(),dr.getContent());
		return dr;

	}

	@Command
	@Override
	public MessageResponse upload(String filename) throws IOException {
		try {
			return (MessageResponse)contactProxy(new UploadRequest(filename,0,FileUtils.getBytesFromFile(path + "/" + filename)));
		} catch (IOException e) {
			e.printStackTrace();
			return new MessageResponse("Can't locate " + filename + "!");
		}

	}

	@Command
	@Override
	public MessageResponse logout() throws IOException {
		MessageResponse r = (MessageResponse)contactProxy(new LogoutRequest());
		proxyChannel.close();
		return r;
	}

	@Command
	@Override
	public MessageResponse exit() throws IOException {
		
		if (loggedIn) {
			logout();
		}
		
		if (proxyChannel != null) //Have we been connected already?
		{		
			proxyChannel.close();
		}
		
		//this.shell.close();
		System.in.close();
		
		System.out.println("Goodbye.");
		return new MessageResponse("");
	}
	
	private Response contactProxy(Request request){
			
		if (proxyChannel == null) { //Has not logged in yet
			return new MessageResponse("Not connected. Log in first!") ;

		}

		if (!proxyChannel.connected()) { //Was logged in, but not connected
			return new MessageResponse("Couldn\'t connect to proxy. Is proxy online?") ;
		}
		
		try {
			return (Response)proxyChannel.contact(request);
		} catch (IOException e) { //Connection error
			return new MessageResponse("Connection to Proxy lost.");
		}

	}
	
	private Response contactFileServer(InetAddress address, int port, Request request) {
		Socket fSocket = null;
		ObjectOutputStream fOut = null;
		ObjectInputStream fIn = null;
		try {
			fSocket = new Socket(address,port);
			fOut = new ObjectOutputStream(fSocket.getOutputStream());
			fIn = new ObjectInputStream(fSocket.getInputStream());
			fOut.writeObject(request);
			Response resp = (Response)fIn.readObject();
			fSocket.close();
			return resp;
		} catch (IOException e) {
			return new MessageResponse("Lost connection to proxy. Maybe proxy went offline?") ;
		} catch (ClassCastException e) { 
			return new MessageResponse("Received strange packet, was not a Response!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return new MessageResponse("Well, that should NOT happen! ClassNotFoundException!") ;
		} 

	}
	
	private void connectToProxy(String username, String password) throws IOException {
		try {
			proxyChannel = new TcpChannel(InetAddress.getByName(proxyHost),proxyPort);
		} catch (IOException e) {
			throw new IOException("Connection attempt failed. Is proxy online?");
		}
		proxyChannel.getOperators().add(new BiDirectionalRsaOperator(proxyKeyPath,keyPath + username + ".pem",password));
		proxyChannel.getOperators().add(new Base64Operator());
	}
}
