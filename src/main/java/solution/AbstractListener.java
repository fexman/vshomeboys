package solution;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract TcpListener implementation. Listens on given port and creates new tcp-connections
 * handled by implementations of AbstractTcpServer. (createTcpServer-method) The AstractTcpServer
 * implementations are managed  in a newCachedThreadPool.
 * @author Felix
 *
 */

public abstract class AbstractListener extends Thread {
	private boolean listening;
	private ExecutorService threadpool;
	private ServerSocket socket;

	private Set<AbstractServer> connections;
	
	public AbstractListener(int port) throws SocketException, IOException {
		this.listening = true;
		this.threadpool = Executors.newCachedThreadPool();
		this.socket = new ServerSocket(port);
		this.connections = Collections.newSetFromMap(new ConcurrentHashMap<AbstractServer,Boolean>());
	}
	
	public void run() {
		
		while (listening) {

			try {
				AbstractServer ts = createServer(socket, connections);
				connections.add(ts);
				threadpool.execute(ts);
			} catch (IOException e) {
				//PROBABLY: SOCKET HAS BEEN CLOSED -  DO NOTHING
			}
		}
		System.out.println(this.getClass().getSimpleName()+":TcpListener going down ... !");

	}
	
	/**
	 * Closes ServerSocket and stops the listening-loop. All still running AbstractTcpServer-Implementations
	 * (stored in connections) will be closed and the cachedThreadPool is shut down.
	 * @throws IOException
	 */
	public void shutDown() throws IOException {
		listening = false;
		for (AbstractServer ts : connections) {
			ts.shutDown();
		}
		threadpool.shutdownNow();
		socket.close();

	}
	
	/**
	 * Creates a new AbstractTcpServer-Implemenation to handle incoming tcp connections. Each created AbstracTcpServer
	 * is saved in the connections-set. And will remove itself when shut down. 
	 * @param socket Socket for TCP connection
	 * @param connections connections set to save AbstracTcpServer in
	 * @return
	 * @throws IOException
	 */
	public abstract AbstractServer createServer(ServerSocket socket, Set<AbstractServer> connections) throws IOException;
	
}
