package solution.proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import solution.model.MyFileServerInfo;
import convert.ConversionService;

public class ProxyUdpListener extends Thread {
	
	private boolean listening;
	private DatagramSocket socket;
	private long timeout;
	private long checkPeriod;
	private ConcurrentHashMap<MyFileServerInfo,Long> fileservers;
	private Timer t;
	
	public ProxyUdpListener(int port, int timeout, int checkperiod, ConcurrentHashMap<MyFileServerInfo,Long> fileservers) throws SocketException {
		this.listening = true;
		this.fileservers = fileservers;
		this.socket = new DatagramSocket(port);
		this.timeout = (long)timeout;
		this.checkPeriod = (long)checkperiod;
		this.t = new Timer();
	}
	
	public void run() {
		
		ConversionService cs = new ConversionService();
		t.schedule(new CheckOnlineStatus(), 0l, checkPeriod);
		
		while (listening) {
			
			try {
		
				DatagramPacket p = new DatagramPacket(new byte[256],256);
				socket.receive(p);
				
				String data = cs.convert(p.getData(),String.class).trim();
				
				if (Pattern.matches("!alive [0-9]{1,5}",data)) {
					//System.out.println("Received good packet: " + data);
					int port = Integer.parseInt(data.split(" ")[1]);
					MyFileServerInfo mfs = new MyFileServerInfo(p.getAddress(),port);
					fileservers.put(mfs, System.currentTimeMillis() + timeout);
				} else {
					System.out.println("Received strange packet: " + data);
				}
			
			} catch (IOException e) {
				t.cancel();
				listening = false;
				//PROBABLY: SOCKET HAS BEEN CLOSED -  DO NOTHING
			}
		}
		System.out.println("FileListener going down ... !");

	}
	
	public void shutDown() {
		listening = false;
		socket.close();
		t.cancel();
	}

	private class CheckOnlineStatus extends TimerTask {

		/**
		 * Checks if fileserver are still sending isAlive packets in specified interval.
		 * If a fileserver is not sending anymore packets  in time it will be flagged as offline.
		 */
		
		@Override
		public void run() {
			for (MyFileServerInfo m : ProxyUdpListener.this.fileservers.keySet()) {
				if (ProxyUdpListener.this.fileservers.get(m) < System.currentTimeMillis()) {
					m.setOnline(false);
				} else {
					m.setOnline(true);
				}
			}
			
		}
	}
	
}