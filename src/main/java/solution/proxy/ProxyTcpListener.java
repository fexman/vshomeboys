package solution.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import solution.AbstractListener;
import solution.AbstractServer;
import solution.communication.Base64Operator;
import solution.communication.Channel;
import solution.communication.ChannelOperator;
import solution.communication.BiDirectionalRsaOperator;
import solution.communication.OneDirectionalRsaOperator;
import solution.communication.TcpChannel;
import solution.model.MyFileServerInfo;
import solution.model.MyUserInfo;


public class ProxyTcpListener extends AbstractListener {

	private ConcurrentHashMap<MyFileServerInfo,Long> fileservers;
	private ConcurrentHashMap<String,MyUserInfo> users;
	private String pathToPrivateKey;
	private String pathToKeys;
	private String pathToHMAC;
	
	public ProxyTcpListener(int port) throws SocketException, IOException {
		super(port);
		throw new IOException("Sorry, can't construct ProxyClientListener that way! :(");
	}
	
	public ProxyTcpListener(int port, ConcurrentHashMap<String,MyUserInfo> users, ConcurrentHashMap<MyFileServerInfo,Long> fileservers, String pathToPrivateKey, String pathToKeys, String pathToHMAC) throws SocketException, IOException {
		super(port);
		this.users = users;
		this.fileservers = fileservers;
		this.pathToPrivateKey = pathToPrivateKey;
		this.pathToKeys = pathToKeys;
		this.pathToHMAC = pathToHMAC;

	}
	@Override
	public AbstractServer createServer(ServerSocket socket,
			Set<AbstractServer> connections) throws IOException {
		Channel tcpChannel = new TcpChannel(socket.accept());
		tcpChannel.getOperators().add(new OneDirectionalRsaOperator(pathToPrivateKey,"12345"));
		tcpChannel.getOperators().add(new Base64Operator());
		return new Proxy(tcpChannel, connections, solution.util.CryptoUtil.getHMACKeyFromPath(pathToHMAC), users, fileservers, pathToPrivateKey, pathToKeys);
	}
	
}
