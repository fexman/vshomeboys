package solution.proxy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

import solution.model.FileInfo;


public class ManagementComponent implements IManagementComponent {
	private ProxyCli proxyInstance;

	@Override
	public int readQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return this.proxyInstance.getReadQuorum();
	}

	@Override
	public int writeQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return this.proxyInstance.getWriteQuorum();
	}

	@Override
	public ArrayList<FileInfo> topThreeDownloads() throws RemoteException {
		if (this.proxyInstance.getFiles().size() != 0) {
			// put all downloaded files into ArrayList
			ArrayList<FileInfo> list = new ArrayList<FileInfo>();
			
			for (String filename : this.proxyInstance.getFiles().keySet()) {
				FileInfo fi = new FileInfo();
				fi.setFilename(filename);
				fi.setVersion(this.proxyInstance.getFiles().get(filename).getVersion());
				fi.setDownloads(this.proxyInstance.getFiles().get(filename).getDownloads());
				list.add(fi);
			}
			
			// Sort the list
			Collections.sort(list);
			
			return list;
		} else {
			// no files downloaded, return empty list
			return new ArrayList<FileInfo>();
		}
	}

	@Override
	public String subscribe(String filename, int numberOfDownloads) throws RemoteException {
		// TODO Auto-generated method stub
		return "subscribed";
	}

	@Override
	public String getProxyPublicKey() throws RemoteException {
		// TODO Auto-generated method stub
		return "proxy public key";
	}

	@Override
	public boolean setUserPublicKey() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setProxyInstance(ProxyCli i) throws RemoteException {
		this.proxyInstance = i;
	}
}