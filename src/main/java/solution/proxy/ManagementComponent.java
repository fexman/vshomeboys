package solution.proxy;

import java.rmi.RemoteException;
import java.util.ArrayList;


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
    public String topThreeDownloads() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
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