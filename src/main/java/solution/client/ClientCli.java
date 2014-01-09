package solution.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
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
import solution.communication.BiDirectionalRsaOperator;
import solution.communication.TcpChannel;
import solution.message.request.CryptedLoginRequest;
import solution.message.response.CryptedLoginConfirmationResponse;
import solution.message.response.CryptedLoginResponse;
import solution.model.FileInfo;
import solution.model.Subscription;
import solution.util.FileUtils;
import util.Config;
import cli.Command;
import cli.Shell;
import client.IClientCli;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import solution.proxy.IManagementComponent;

public class ClientCli implements IClientCli {

	private Thread shellThread;
	private String path;
	private Channel proxyChannel;
	private String proxyHost;
	private int proxyPort;
	private boolean loggedIn;
	private String keyPath;
	private String proxyKeyPath;
	private IManagementComponent mc;
	private Registry clientReg;
	private ISubscribe stub;
	private ISubscribe rs;
	private String bindingName;
	private String username;

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

			// connect to server rmi
			Config mcConfig = new Config("mc");	
			Registry reg = LocateRegistry.getRegistry(mcConfig.getString("proxy.host"),
					mcConfig.getInt("proxy.rmi.port"));
			mc = (IManagementComponent) reg.lookup(mcConfig.getString("binding.name"));
			//////////

			// register client rmi
			int rmi_port = mcConfig.getInt("proxy.rmi.port")+1;
			try {
				clientReg = LocateRegistry.createRegistry(rmi_port);
			} catch (RemoteException e) {
				clientReg = LocateRegistry.getRegistry(mcConfig.getString("proxy.host"), rmi_port);
			}
			rs = new Subscribe();
			try {
				stub = (ISubscribe) UnicastRemoteObject.exportObject(rs, 0);
			} catch (RemoteException e) {
				System.out.println(e.getMessage());
			}
			bindingName = "clientSubscribe";
			clientReg.rebind(bindingName, stub);
			///////////

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

		} catch (NotBoundException e) {
			System.out.println("Error: RMI Not bound.");
			exit();
		}

	}

	@Command
	@Override
	public LoginResponse login(String username, String password) throws IOException {


		try {
			if (proxyChannel == null) {
				connectToProxy(username, password); //First time
			} else {
				if (!loggedIn) { //Connection lost or cancelled
					connectToProxy(username, password); //
				} else { //Already connected
					System.out.println("Already connected, logout first.");
					return loginfailed();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return loginfailed();
		}

		SecureRandom secureRandom = new SecureRandom(); 
		final byte[] number = new byte[32]; 
		secureRandom.nextBytes(number);
		String challenge = Base64.encode(number);

		Response r = contactProxy(new CryptedLoginRequest(username,challenge)); //Init Login-Request

		if (!(r instanceof CryptedLoginResponse)) { //Answer was not according to protocol
			System.out.println("Proxy did not respond with CryptedLoginResponse when expected.");
			return loginfailed(); 
		}
		CryptedLoginResponse clr = (CryptedLoginResponse)r;

		//Compare recevied challenge
		byte[] chResponse;
		try {
			chResponse = Base64.decode(clr.getClChallenge());
		} catch (Base64DecodingException e2) {
			System.out.println("Error decoding received clChallenge!");
			return loginfailed();
		}
		if (!Arrays.equals(chResponse, number)) { //Challenge was not returned correctly
			System.out.println("Challenge was not returned correctly!");
			return loginfailed(); 
		}

		byte[] encodedKey = null; //Get AES secret key
		try {
			encodedKey = Base64.decode(clr.getKey());
		} catch (Base64DecodingException e) {
			System.out.println("AES-key decoding error!");
			return loginfailed(); 
		}
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

		byte[] iv = null; //Get AES IV
		try {
			iv = Base64.decode(clr.getIv());
		} catch (Base64DecodingException e) {
			System.out.println("AES-Iv decoding error!");
			return loginfailed(); 
		}

		try { //Create AES channel, RSA challenge is replaced
			proxyChannel.getOperators().set(0, new AESOperator(key,iv));
		} catch (IOException e1) {
			System.out.println("AES-Channel creation failed: " + e1.getMessage());
			return loginfailed();
		}

		//Confirm by returning Proxy challenge using AES channel
		proxyChannel.transmit(new CryptedLoginConfirmationResponse(clr.getProxyChallenge()));

		LoginResponse lr;
		try {
			lr = (LoginResponse) proxyChannel.receive();
		} catch (ClassCastException e) {
			System.out.println("Last conformation was not as expected!");
			return loginfailed();
		}
		loggedIn = true; //Login Successfull
		this.username = username;
		return lr;
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
		if (proxyChannel != null) {
			proxyChannel.close();
		}
		loggedIn = false;
		return r;
	}

	@Command
	@Override
	public MessageResponse exit() throws IOException {

		if (clientReg != null) {
			try {
				System.out.print("Unbinding RMI ... ");
				//clientReg.unbind(bindingName);
				UnicastRemoteObject.unexportObject(rs, true);
				System.out.print("done\n");
			} catch (Exception e) {
				System.out.println("could not unbind rmi");
			}
		}

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
			loggedIn = false;
			return new MessageResponse("Couldn\'t connect to proxy. Is proxy online?") ;
		}

		try {
			return (Response)proxyChannel.contact(request);
		} catch (IOException e) { //Connection error
			loggedIn = false;
			return new MessageResponse("Connection to Proxy lost.");

		}

	}

	private Response contactFileServer(InetAddress address, int port, Request request) {
		Channel fileServerChannel;
		try {
			fileServerChannel = new TcpChannel(address,port);
			Response resp = (Response)fileServerChannel.contact(request);
			fileServerChannel.close();
			return resp;
		} catch (IOException e) {
			return new MessageResponse("Lost connection to proxy. Maybe proxy went offline?") ;
		} catch (ClassCastException e) { 
			return new MessageResponse("Received strange packet, was not a Response!");
		}

	}

	private void connectToProxy(String username, String password) throws IOException {
		try {
			proxyChannel = new TcpChannel(InetAddress.getByName(proxyHost),proxyPort);
		} catch (IOException e) {
			loggedIn = false;
			throw new IOException("Connection attempt failed. Is proxy online?");
		}
		proxyChannel.getOperators().add(new BiDirectionalRsaOperator(proxyKeyPath,keyPath + username + ".pem",password));
		proxyChannel.getOperators().add(new Base64Operator());
	}

	private LoginResponse loginfailed() {
		loggedIn = false;
		if (proxyChannel != null) {
			proxyChannel.close();
		}
		return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
	}

	@Override
	@Command
	public MessageResponse readQuorum() throws IOException {
		return new MessageResponse("Read-Quorum is set to " + mc.readQuorum());
	}

	@Override
	@Command
	public MessageResponse writeQuorum() throws IOException {
		return new MessageResponse("Write-Quorum is set to " + mc.writeQuorum());
	}

	@Override
	@Command
	public MessageResponse topThreeDownloads() throws IOException {
		ArrayList<FileInfo> list = mc.topThreeDownloads();

		if (list.size() == 0) {
			return new MessageResponse("No files have been downloaded yet");
		} else {
			// check if we have top three files, if not display top, top two and so on
			String nr1 = "", nr2 = "", nr3 = "", msg = "";
			if (list.size() >= 1) {
				FileInfo tmp = list.get(0);
				nr1 = "1. "+tmp.getFilename()+" "+tmp.getDownloads()+"\n";
			}
			if (list.size() >= 2) {
				FileInfo tmp = list.get(1);
				nr2 = "2. "+tmp.getFilename()+" "+tmp.getDownloads()+"\n";
			}
			if (list.size() >= 3) {
				FileInfo tmp = list.get(2);
				nr3 = "3. "+tmp.getFilename()+" "+tmp.getDownloads()+"\n";
			}

			if (list.size() != 3) {
				msg = "3 different files have not been downloaded yet!\n";
			}

			return new MessageResponse("Top Three Downloads: \n"+nr1+nr2+nr3+msg);
		}
	}

	@Override
	@Command
	public MessageResponse subscribe(String filename, int noOfDls) throws IOException {
		if (this.loggedIn == true) {
			Subscription s = new Subscription();
			s.setCount(noOfDls);
			s.setFilename(filename);
			s.setUser(this.username);
			mc.subscribe(s);
			return new MessageResponse("Successfully subscribed for file: "+s.getFilename());
		} else {
			return new MessageResponse("Please login first!");
		}
	}

	@Override
	@Command
	public MessageResponse getProxyPublicKey() throws IOException {
		byte [] publicKey = mc.getProxyPublicKey();

		try {
			String filePath = keyPath + "/proxy.pub.pem";
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(publicKey);
			fos.close();
		} catch(FileNotFoundException ex)
		{
			System.out.println("FileNotFoundException : " + ex);
		}
		catch(IOException ioe)
		{
			System.out.println("IOException : " + ioe);
		}

		return new MessageResponse("Successfully received public key of Proxy.");
	}

	@Override
	@Command
	public MessageResponse setUserPublicKey(String user) throws IOException {
		String filePath = keyPath+"/"+user+".pub.pem";
		byte[] data = new byte[800];
		try {
			FileInputStream fis = new FileInputStream(filePath);
			fis.read(data);
			fis.close();
		} catch (IOException e) {
			System.out.println("could not read/find file "+user+".pub.pem");
			return new MessageResponse("Failed to trasnmit public key of user: "+user);
		}

		if (mc.setUserPublicKey(user, data)== true) {
			return new MessageResponse("Successfully transmitted public key of user: "+user);
		} else {
			return new MessageResponse("Failed to trasnmit public key of user: "+user);
		}
	}
}


