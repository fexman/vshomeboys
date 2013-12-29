package solution.fileserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.MissingResourceException;
import java.util.Timer;
import java.util.TimerTask;

import message.response.MessageResponse;
import server.IFileServerCli;
import util.Config;
import cli.Command;
import cli.Shell;
import convert.ConversionService;

public abstract class FileServerCli implements IFileServerCli {

	private Thread shellThread;
	private final Timer t;
	private ReportOnlineStatus ros;
	private FileServerTcpListener tcpListener;

	
	public FileServerCli(Config conf, Shell shell) {
		
		t = new Timer();
		
		try {
			ros = new ReportOnlineStatus(conf.getString("proxy.host"),conf.getInt("proxy.udp.port"),conf.getInt("tcp.port"));
			t.schedule(ros, 0l, conf.getInt("fileserver.alive"));
			tcpListener = new FileServerTcpListener(conf.getInt("tcp.port"),conf.getString("fileserver.dir"),conf.getString("hmac.key"));
			tcpListener.start();
			shell.register(this);
			shellThread = new Thread(shell);
			shellThread.start();
		} catch (MissingResourceException e) {
			
			System.out.println("Invalid usage! Missing resource: " + e.getKey());
			
			try {
				exit();
			} catch (IOException e1) { //Should not happen!
				System.out.println("Boy, that escalated quickly!");
			}
			
		} catch (Exception e) { //No matter what happens, we have to shutdown
			
			System.out.println("Error, could not set up listeners: "+e.getClass().getSimpleName()+" - "+e.getMessage()+"\nShutting down ...");
			
			try {
				exit();
			} catch (IOException e1) { //Should not happen!
				System.out.println("Boy, that escalated quickly!");
			}
		}
	}

	@Command
	public MessageResponse exit() throws IOException {
		
		t.cancel();
		
		if (ros != null) {
			ros.exit();
		}
		
		if (tcpListener != null) {
			tcpListener.shutDown();
		}
		
		//shell.close();
		System.in.close();
		
		System.out.println("Fileserver going down ... !");
		return new MessageResponse("");
	}
	
	private class ReportOnlineStatus extends TimerTask {

		/**
		 * Sends isAlive packets to proxy in specified intervals
		 */

		private DatagramSocket socket;
		private DatagramPacket packet;
		byte[] buf;
		
		private ReportOnlineStatus(String addressString, int udpPort, int tcpPort) throws UnknownHostException, SocketException {
			InetAddress address = InetAddress.getByName(addressString);
			String statusMsg = "!alive " + tcpPort;
			
			ConversionService cs = new ConversionService();
			buf = cs.convert(statusMsg,byte[].class);
			
			this.socket = new DatagramSocket();
			this.packet = new DatagramPacket(buf, buf.length, address, udpPort);
		}

		@Override
		public void run() {
			try {
				socket.send(packet);
				//System.out.println("Sending packet ... " + System.currentTimeMillis());
			} catch (IOException e) {
				System.out.println("Could not send Onlinestatus report!");
				e.printStackTrace();
			}
		}
		
		private void exit() {
			System.out.println("FileServer.ReportOnlineStatus exit() called!");
			this.socket.close();	
		}
	}

}
