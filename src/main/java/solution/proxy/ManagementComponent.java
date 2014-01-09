package solution.proxy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import solution.model.FileInfo;
import solution.model.Subscription;
import util.Config;


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
	public byte[] getProxyPublicKey() throws RemoteException {
		Config conf = new Config("proxy");
		String keysDir = conf.getString("keys.dir");
		
		Path path = Paths.get(keysDir+"/proxy.pub.pem");
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			System.out.println("could not read file proxy.pub.pem");
		}
		
		return data;
	}

	@Override
	public boolean setUserPublicKey(String user, byte [] publicKey) throws RemoteException {
		Config conf = new Config("proxy");
		String keysDir = conf.getString("keys.dir");
		
		Path path = Paths.get(keysDir+"/"+user+".pub.pem");

		try {
			FileOutputStream fos = new FileOutputStream(path.toString());
			fos.write(publicKey);
			fos.close();
		} catch(FileNotFoundException ex)
		{
			System.out.println("FileNotFoundException : " + ex);
			return false;
		}
		catch(IOException ioe)
		{
			System.out.println("IOException : " + ioe);
			return false;
		}
		
		return true;
	}

	public void setProxyInstance(ProxyCli i) throws RemoteException {
		this.proxyInstance = i;
	}

	@Override
	public void subscribe(Subscription s) throws RemoteException {
		ArrayList<Subscription> slist = this.proxyInstance.getS_list();
		
		slist.add(s);
	}
}






