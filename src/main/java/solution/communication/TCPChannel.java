package solution.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

public class TCPChannel implements Channel {

	private final Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private boolean connected;
	
	public TCPChannel (Socket socket) throws IOException {
		this.socket = socket;
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		connected = true;
	}
	
	public TCPChannel (InetAddress address, int port) throws IOException {
		this(new Socket(address,port));
	}
	
	@Override
	public void transmit(Serializable s) throws IOException {
			out.writeObject(s);
	}

	@Override
	public Serializable receive() throws IOException {

		while (true) {
			try {
				return (Serializable)in.readObject();
			} catch (ClassNotFoundException e) {
				//Continue listening
			}
		}
		
	}

	@Override
	public Serializable contact(Serializable s) throws IOException {
		transmit(s);
		return receive();
	}
	

	@Override
	public void close() {
		connected = false;
		try {
			socket.close();
		} catch (IOException e) {
			//Nothing TODO here
		}
	}

	@Override
	public boolean connected() {
		return connected;
	}

	@Override
	public String getConnectionInfo() {
		return "TCP/" +socket.getInetAddress().getHostAddress() + ":" +  socket.getPort();
	}


	
	
}
