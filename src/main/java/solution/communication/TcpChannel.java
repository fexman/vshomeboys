package solution.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class TcpChannel implements Channel {

	private final Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private boolean connected;
	
	private ArrayList<ChannelOperator> operators;
	
	public TcpChannel (Socket socket) throws IOException {
		this.socket = socket;
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		connected = true;
		this.operators = new ArrayList<ChannelOperator>();
	}
	
	public TcpChannel (InetAddress address, int port) throws IOException {
		this(new Socket(address,port));
	}
	
	@Override
	public void transmit(Serializable s) throws IOException {
			for (ChannelOperator c : operators)
			{
				s = c.transmitOperation(s);
			}
			out.writeObject(s);
	}

	@Override
	public Serializable receive() throws IOException {

		while (true) {
			try {
				Serializable s = (Serializable)in.readObject();
				for (int i = operators.size()-1;i >= 0; i--) {
					s = operators.get(i).receiveOperation(s);
				}
				return s;
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
			//Nothing todo here
		}
	}

	@Override
	public boolean connected() {
		return connected;
	}

	@Override
	public String getConnectionInfo() {
		String str = "TCP://" +socket.getInetAddress().getHostAddress() + ":" +  socket.getPort();
		for (ChannelOperator c: operators) {
			str += " "+c.OPERATOR_IDENT();
		}
		return str;
	}

	@Override
	public ArrayList<ChannelOperator> getOperators() {
		return operators;
	}


	
	
}
