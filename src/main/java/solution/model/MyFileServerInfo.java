package solution.model;

import java.net.InetAddress;

import model.FileServerInfo;

/**
 * Contains information about a {@link server.IFileServer} and its state.
 */
public class MyFileServerInfo implements Comparable<MyFileServerInfo> {


	private InetAddress address;
	private int port;
	private volatile long usage;
	private volatile boolean online;

	public MyFileServerInfo(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		this.usage = 0;
		this.online = true;
	}

	@Override
	public String toString() {
		return String.format("%1$-15s %2$-5d %3$-7s %4$13d",
				getAddress().getHostAddress(), getPort(),
				isOnline() ? "online" : "offline", getUsage());
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
	
	public synchronized void use(long increment) {
		this.usage += increment;
	}

	public long getUsage() {
		return usage;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean isOnline() {
		return online;
	}
	
	public FileServerInfo toFileServerInfo() {
		return new FileServerInfo(address, port, usage, online);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + port;
		return result;
	}

	/* (non-Javadoc)
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
		MyFileServerInfo other = (MyFileServerInfo) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	@Override
	public int compareTo(MyFileServerInfo arg0) {
        if(usage < arg0.usage) {
            return -1;
        } if(usage > arg0.usage) {
            return 1;
        }
        return 0;
	}
	
	
	
	
}
