package solution;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

import message.Request;
import message.Response;
import solution.util.requestProcessor.RequestHandlerUtil;

/**
 * Manages a tcp-connection. (Socket) While running incoming requests will be
 * processed using RequestProcessorUtil
 * 
 * @author Felix
 * 
 */
public abstract class AbstractTcpServer extends Thread {

	private final ObjectInputStream in;
	private final ObjectOutputStream out;
	private final Socket socket;
	private boolean listening;

	private final Set<AbstractTcpServer> connections;
	private final String identString;

	private final String threadStr;
	private final String clientStr;

	public AbstractTcpServer(final Socket socket, final Set<AbstractTcpServer> connections) throws IOException {
		this.socket = socket;
		this.in = new ObjectInputStream(socket.getInputStream());
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.listening = true;
		this.connections = connections;

		clientStr = socket.getInetAddress().getHostAddress() + " ...";
		threadStr = "[TH" + this.getId() + "] ";

		int saltVal = (int) (Math.random() * (10000 - 1000) + 1000);
		identString = this.getId() + socket.getInetAddress().getHostAddress() + System.currentTimeMillis() + saltVal;

	}

	public void run() {
		println("Spawned, serving: " + clientStr);
		Object received;

		do {

			try {

				received = in.readObject();

				if (received instanceof Request) {
					Request r = (Request) received;

					Response resp = RequestHandlerUtil.handle(r, this);

					if (resp != null) {
						out.writeObject(resp);
					} else {
						println("Received strange object via TCP: " + received.getClass());
					}

				}
				
			} catch (IOException e) { // connection errors
				System.err.println("Lost connection to client " + clientStr);
				listening = false;
			} catch (ClassNotFoundException e) {
				System.err.println("Error: Received invalid object");
				listening = false;
			}

		} while (listening);
		println("Closing connection to " + clientStr); // listening = false,
														// normal logout

		shutDown();
		println("Running out ...");
	}

	/**
	 * Close socket, remove from connections-set
	 */
	public void shutDown() {

		customShutDown();

		/*
		 * try { out.close(); } catch (IOException e) { //Nothing } try {
		 * in.close(); } catch (IOException e) { //Nothing }
		 */

		try {
			socket.close();
		} catch (IOException e) {
			// Nothing
		}

		connections.remove(this); // Unregister this abstract-tcpserverinstance

	}

	/**
	 * Implementation specific shutdown behaviour
	 */
	protected abstract void customShutDown();

	/**
	 * Stop listening loop
	 */
	protected void stopListening() {
		listening = false;
	}

	/**
	 * Print output with Listener/Thread ident
	 * 
	 * @param msg
	 */
	protected void println(String msg) {
		System.out.println(this.getClass().getSimpleName() + threadStr + msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identString == null) ? 0 : identString.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractTcpServer other = (AbstractTcpServer) obj;
		if (identString == null) {
			if (other.identString != null)
				return false;
		} else if (!identString.equals(other.identString))
			return false;
		return true;
	}

}
