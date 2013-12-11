package solution.fileserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Set;

import solution.AbstractTcpListener;
import solution.AbstractTcpServer;

public class FileServerTcpListener extends AbstractTcpListener {

	private final String path;
	
	public FileServerTcpListener(int port, String path) throws SocketException, IOException {
		super(port);
		this.path = path;
	}

	@Override
	public AbstractTcpServer createTcpServer(ServerSocket socket, Set<AbstractTcpServer> connections) throws IOException {
		return new FileServer(socket.accept(), connections, path);
	}


}
