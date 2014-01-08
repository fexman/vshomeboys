package solution.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;

import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import model.FileServerInfo;
import model.UserInfo;
import proxy.IProxyCli;
import solution.model.MyFileServerInfo;
import solution.model.MyUserInfo;
import solution.util.UserConfigParser;
import util.Config;
import cli.Command;
import cli.Shell;
import java.rmi.registry.*;
import java.rmi.RemoteException;

public class ProxyCli implements IProxyCli  {
	
	private ConcurrentHashMap<String,MyUserInfo> users;
	private ConcurrentHashMap<MyFileServerInfo,Long> fileservers;
	private final Shell shell;
	private Thread shellThread;
	private ProxyTcpListener pcl;
	private ProxyUdpListener pfl;

	// mc variables
	private String mc_proxy_host;
	private String mc_binding_name;
	private int mc_rmi_port;
	private String mc_keys_dir;
	
	public static void main(String[] args) {
		new ProxyCli(new Config("proxy"),new Shell("Proxy",System.out, System.in));
	}
	
	public ProxyCli(Config conf, Shell shell) {
		// read mc.properties
		Config mc = new Config("mc");
		this.mc_proxy_host = mc.getString("proxy.host");
		this.mc_binding_name = mc.getString("binding.name");
		this.mc_rmi_port = mc.getInt("proxy.rmi.port");
		this.mc_keys_dir = mc.getString("keys.dir");
		
		// set up mc
		try {
			LocateRegistry.createRegistry(this.mc_rmi_port);
		} catch (RemoteException e2) {
			System.out.println("failed to create mc registry");
			e2.printStackTrace();
		}
		
		this.shell = shell;
		
		users = new ConcurrentHashMap<String,MyUserInfo>(UserConfigParser.getUserMap());
		fileservers = new ConcurrentHashMap<MyFileServerInfo,Long>();
		
		this.shell.register(this);
		
		try {
			pfl = new ProxyUdpListener(conf.getInt("udp.port"),conf.getInt("fileserver.timeout"), conf.getInt("fileserver.checkPeriod"),fileservers);
			pcl = new ProxyTcpListener(conf.getInt("tcp.port"), users, fileservers, conf.getString("key"), conf.getString("keys.dir"),conf.getString("hmac.key"));
			pfl.start();
			pcl.start();
			shellThread = new Thread(shell);
			shellThread.start();
		} catch (MissingResourceException e) {
			
			System.out.println("Invalid usage! Missing resource: " + e.getKey());
			
			try {
				exit();
			} catch (IOException e1) { //Should not happen!
				System.out.println("Boy, that escalated quickly!");
			}
			
		} catch (Exception e) { //No matter what happens, we have to shutdown
			
			System.out.println("Error, could not set up listeners: "+e.getClass().getSimpleName()+" - "+e.getMessage()+"\nShutting down ...");
			
			try {
				exit();
			} catch (IOException e1) { //Should not happen!
				System.out.println("Boy, that escalated quickly!");
			}
		}
		

	}

	@Override
	@Command
	public Response fileservers() throws IOException {
		ArrayList<FileServerInfo> fileserverList = new ArrayList<FileServerInfo>();
		for (MyFileServerInfo m : fileservers.keySet()) {
			fileserverList.add(m.toFileServerInfo());
		}
		return new FileServerInfoResponse(fileserverList);
	}

	@Override
	@Command
	public Response users() throws IOException {
		ArrayList<UserInfo> userList = new ArrayList<UserInfo>();
		for (MyUserInfo u : users.values()) {
			userList.add(u.toUserInfo());
		}
		return new UserInfoResponse(userList);
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {

		if (pfl != null) {
			pfl.shutDown();
		}
		
		if (pcl != null) {
			pcl.shutDown();
		}
		
		//shell.close();
		System.in.close();
		
		return new MessageResponse("");
	}
	

}
