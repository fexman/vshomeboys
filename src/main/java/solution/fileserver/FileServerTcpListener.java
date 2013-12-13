package solution.fileserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import solution.AbstractTcpListener;
import solution.AbstractTcpServer;

public class FileServerTcpListener extends AbstractTcpListener {

	private String path;
	private File dir;
	private ConcurrentHashMap<String, Integer> files;
	
	public FileServerTcpListener(int port, String path) throws SocketException, IOException {
		super(port);
		this.path = path;
		this.dir = new File(path);
		files = createFileMap(path);
	}

	@Override
	public AbstractTcpServer createTcpServer(ServerSocket socket, Set<AbstractTcpServer> connections) throws IOException {
		return new FileServer(socket.accept(), connections, path, files);
	}
	
	/**
	 * Creates a map of all existing files in the directory
	 * and initializes the version number with 0.
	 * @return
	 */
	private ConcurrentHashMap<String, Integer> createFileMap(String path) {

		ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();

		for (File f : dir.listFiles()) {

			if (!f.isDirectory()) {
				map.put(f.getName(), 0);
			}
		}

		return map;
	}
}