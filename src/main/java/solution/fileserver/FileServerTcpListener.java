package solution.fileserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import solution.AbstractListener;
import solution.AbstractServer;
import solution.communication.TcpChannel;
import solution.model.FileInfo;

public class FileServerTcpListener extends AbstractListener {

	private String path;
	private File dir;
	private ConcurrentHashMap<String, FileInfo> files;
	private String pathToHMAC;
	
	public FileServerTcpListener(int port, String path, String pathToHMAC) throws SocketException, IOException {
		super(port);
		this.path = path;
		this.dir = new File(path);
		files = createFileMap(path);
		this.pathToHMAC = pathToHMAC;
	}

	@Override
	public AbstractServer createServer(ServerSocket socket, Set<AbstractServer> connections) throws IOException {
		return new FileServer(new TcpChannel(socket.accept()), connections, solution.util.CryptoUtil.getHMACKeyFromPath(pathToHMAC), path, files);
	}
	
	/**
	 * Creates a map of all existing files in the directory
	 * and initializes the version number with 0.
	 * @return
	 */
	private ConcurrentHashMap<String, FileInfo> createFileMap(String path) {

		ConcurrentHashMap<String, FileInfo> map = new ConcurrentHashMap<String, FileInfo>();

		for (File f : dir.listFiles()) {

			if (!f.isDirectory()) {
				FileInfo fi = new FileInfo();
				map.put(f.getName(), fi);
			}
		}

		return map;
	}
}