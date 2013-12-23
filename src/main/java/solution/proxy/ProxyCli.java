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

public class ProxyCli implements IProxyCli  {
	
	private ConcurrentHashMap<String,MyUserInfo> users;
	private ConcurrentHashMap<MyFileServerInfo,Long> fileservers;
	private final Shell shell;
	private Thread shellThread;
	private ProxyTcpListener pcl;
	private ProxyUdpListener pfl;
	
	public static void main(String[] args) {
		new ProxyCli(new Config("proxy"),new Shell("Proxy",System.out, System.in));
	}
	
	public ProxyCli(Config conf, Shell shell) {
		this.shell = shell;
		
		users = new ConcurrentHashMap<String,MyUserInfo>(UserConfigParser.getUserMap());
		fileservers = new ConcurrentHashMap<MyFileServerInfo,Long>();
		
		this.shell.register(this);
		
		try {
			pfl = new ProxyUdpListener(conf.getInt("udp.port"),conf.getInt("fileserver.timeout"), conf.getInt("fileserver.checkPeriod"),fileservers);
			pcl = new ProxyTcpListener(conf.getInt("tcp.port"), users, fileservers, conf.getString("key"), conf.getString("keys.dir"));
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
