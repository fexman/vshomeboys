package solution.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import solution.AbstractTcpListener;
import solution.AbstractTcpServer;
import solution.communication.Base64TCPChannel;
import solution.communication.TCPChannel;
import solution.model.MyFileServerInfo;
import solution.model.MyUserInfo;


public class ProxyTcpListener extends AbstractTcpListener {

	private ConcurrentHashMap<MyFileServerInfo,Long> fileservers;
	private ConcurrentHashMap<String,MyUserInfo> users;
	
	public ProxyTcpListener(int port) throws SocketException, IOException {
		super(port);
		throw new IOException("Sorry, can't construct ProxyClientListener that way! :(");
	}
	
	public ProxyTcpListener(int port, ConcurrentHashMap<String,MyUserInfo> users, ConcurrentHashMap<MyFileServerInfo,Long> fileservers) throws SocketException, IOException {
		super(port);
		this.users = users;
		this.fileservers = fileservers;

	}
	@Override
	public AbstractTcpServer createTcpServer(ServerSocket socket,
			Set<AbstractTcpServer> connections) throws IOException {
		return new Proxy(new Base64TCPChannel(socket.accept()), users, fileservers, connections);
	}
	
}
