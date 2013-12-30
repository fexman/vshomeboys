package solution;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Arrays;
import java.util.Set;

import message.Request;
import message.Response;
import solution.communication.Channel;
import solution.communication.TcpChannel;
import solution.message.request.HMacRequest;
import solution.message.response.HMacErrorResponse;
import solution.message.response.HMacResponse;
import solution.util.requestProcessor.RequestHandlerUtil;

/**
 * Manages a tcp-connection. (Socket) While running incoming requests will be
 * processed using RequestProcessorUtil
 * 
 * @author Felix
 * 
 */
public abstract class AbstractServer extends Thread {

	private final Channel channel;
	private boolean listening;

	private final Set<AbstractServer> connections;
	private final String identString;

	private final String threadStr;
	protected final Key hMACKey;

	public AbstractServer(final Channel channel, final Set<AbstractServer> connections, final Key hMACKey) throws IOException {
		this.channel = channel;
		this.listening = true;
		this.connections = connections;
		
		//HMac
		this.hMACKey = hMACKey;

		threadStr = "[TH" + this.getId() + "] ";

		int saltVal = (int) (Math.random() * (10000 - 1000) + 1000);
		identString = this.getId() + channel.getConnectionInfo() + System.currentTimeMillis() + saltVal;

	}

	public void run() {
		println("Spawned, serving: " + channel.getConnectionInfo());
		boolean receivedHMAC;
		
		do {

			try {

				Object received = channel.receive();

				if (received != null) {
					Request r = (Request) received;
					
					if (r instanceof HMacRequest) {
						HMacRequest Hreq = (HMacRequest)received;
						r = Hreq.getRequest();
						HMacRequest verif = new HMacRequest(r,hMACKey);
						if (!Arrays.equals(Hreq.getHMac(),verif.getHMac())) {
							println("Invalid HMAC!\nRECEIVED: " + Hreq.toString() + "\nEXPECTED: " + verif.toString());
							channel.transmit(new HMacErrorResponse());
							continue;
						}
						println("Received HMAC-request: HMAC was ok.");
						receivedHMAC = true;
					} else {
						receivedHMAC = false;
					}
					
					Response resp = RequestHandlerUtil.handle(r, this);

					if (resp != null) {
						if (receivedHMAC) {
							channel.transmit(new HMacResponse(resp,hMACKey));
						} else {
							channel.transmit(resp);
						}
					} else {
						println("Received strange object via TCP: " + received.getClass());
					}

				} else { // connection errors
					System.err.println("Lost connection to client " + channel.getConnectionInfo());
					listening = false;
				}
			} catch (IOException e) {
				println("Error: Lost connection.");
				listening = false;
			} catch (InvalidKeyException e) {
				println("Invalid HMAC-Key!");
				listening = false;
			}

		} while (listening);
		println("Closing connection to " + channel.getConnectionInfo()); // listening = false,
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

		channel.close();

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
		AbstractServer other = (AbstractServer) obj;
		if (identString == null) {
			if (other.identString != null)
				return false;
		} else if (!identString.equals(other.identString))
			return false;
		return true;
	}

}
