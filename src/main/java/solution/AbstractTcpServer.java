package solution;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

import message.Request;
import message.Response;
import solution.communication.TCPChannel;
import solution.util.requestProcessor.RequestHandlerUtil;

/**
 * Manages a tcp-connection. (Socket) While running incoming requests will be
 * processed using RequestProcessorUtil
 * 
 * @author Felix
 * 
 */
public abstract class AbstractTcpServer extends Thread {

	private final TCPChannel tcpChannel;
	private boolean listening;

	private final Set<AbstractTcpServer> connections;
	private final String identString;

	private final String threadStr;

	public AbstractTcpServer(final TCPChannel tcpChannel, final Set<AbstractTcpServer> connections) throws IOException {
		this.tcpChannel = tcpChannel;
		this.listening = true;
		this.connections = connections;

		threadStr = "[TH" + this.getId() + "] ";

		int saltVal = (int) (Math.random() * (10000 - 1000) + 1000);
		identString = this.getId() + tcpChannel.getConnectionInfo() + System.currentTimeMillis() + saltVal;

	}

	public void run() {
		println("Spawned, serving: " + tcpChannel.getConnectionInfo());

		do {

			try {

				Request received = (Request)tcpChannel.receive();

				if (received != null) {
					Request r = (Request) received;
					Response resp = RequestHandlerUtil.handle(received, this);

					if (resp != null) {
						tcpChannel.transmit(resp);
					} else {
						println("Received strange object via TCP: " + received.getClass());
					}

				} else { // connection errors
					System.err.println("Lost connection to client " + tcpChannel.getConnectionInfo());
					listening = false;
				}
			} catch (IOException e) {
				println("Error: Lost connection.");
				listening = false;
			}

		} while (listening);
		println("Closing connection to " + tcpChannel.getConnectionInfo()); // listening = false,
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

		tcpChannel.close();

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
