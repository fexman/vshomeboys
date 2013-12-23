package solution.communication;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a communication channel, e.g. a TCP-connection.
 * @author Felix
 *
 */

public interface Channel {
	
	/**
	 * Transmits a Serializable through underlying communication channel
	 * @param s
	 * @throws IOException In case of connection errors
	 */
	void transmit(Serializable s) throws IOException;
	
	/**
	 * Waits for reception of a Serializable through the underlying communication channel,
	 * if anything else is received, it's simply dropped and the waiting will be continued
	 * @return
	 * @throws IOException
	 */
	Serializable receive() throws IOException;

	/**
	 * A combination of {@link Channel#transmit(Serializable)} and {@link Channel#receive()}. A Serializable
	 * is transmitted through the underlying communication channel, than the first Serializable reception will be
	 * awaited
	 * @param s
	 * @return
	 * @throws IOException
	 */
	Serializable contact(Serializable s) throws IOException;
	
	/**
	 * Returns true of false, whether this channel is connected or not.
	 * @return
	 */
	boolean connected();
	
	/**
	 * Closes this channel.
	 */
	void close();
	
	/**
	 * Readable information about the underlying type of connection
	 * @return
	 */
	String getConnectionInfo();
	
	/**
	 * Returns ArrayList of ChannelOperators to add/remove them to underlying channel.
	 * @return
	 */
	ArrayList<ChannelOperator> getOperators();
	
	

	
}
