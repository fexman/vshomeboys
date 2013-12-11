package solution.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.MissingResourceException;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadFileRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.DownloadFileResponse;
import message.response.DownloadTicketResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import model.DownloadTicket;
import solution.util.FileUtils;
import util.Config;
import cli.Command;
import cli.Shell;
import client.IClientCli;

public class ClientCli implements IClientCli {

	private Thread shellThread;
	private String path;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private boolean connected;
	private boolean loggedIn;
	private String proxyHost;
	private int proxyPort;
	
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
		
		connected = false;
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
	public LoginResponse login(String username, String password)
			throws IOException {
		Response r = contactProxy(new LoginRequest(username,password));
		if (!(r instanceof LoginResponse)) { //Connection problems
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS); //We have to return some kind of LoginResponse!
		}
		LoginResponse lr = (LoginResponse)r;
		if (lr.getType().equals(LoginResponse.Type.SUCCESS)) { //Login successful? (For different logout behavior)
			loggedIn = true;
		}
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
		if (loggedIn) { //assumption: logout-behaviour differs when not logged in (stays connected)
			connected = false;
			loggedIn = false;
		}
		return r;
	}

	@Command
	@Override
	public MessageResponse exit() throws IOException {
		
		if (loggedIn) {
			logout();
		}
		
		if (socket != null) //Have we been connected already?
		{		
			
			try {
				socket.close();
			} catch (IOException e) {
				//Nothing
			}
		}
		
		//this.shell.close();
		System.in.close();
		
		System.out.println("Goodbye.");
		return new MessageResponse("");
	}
	
	private Response contactProxy(Request request) throws IOException {
		try {
			if (!connected) { //If not connected open socket first
				try {
				socket = new Socket(proxyHost,proxyPort);
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
				connected = true;
				} catch (IOException ex) { }
			}
			
			if (connected) {
				out.writeObject(request);
				return (Response)in.readObject();
			}
			
			//Connection attempt went wrong
			return new MessageResponse("Couldn\'t connect to proxy. Is proxy online?") ;
			
		} catch (ClassNotFoundException e) { //??
			e.printStackTrace();
			return new MessageResponse("Well, that should NOT happen! ClassNotFoundException!") ;
		} catch (IOException e) { //Abruptly lost connection
			connected = false;
			return new MessageResponse("Lost connection to proxy. Maybe proxy went offline?") ;
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

	
}
