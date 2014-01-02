package solution.proxy;

import java.rmi.RemoteException;
import java.util.ArrayList;

import solution.model.MyDownloadInfo;

public class ManagementComponent implements IManagementComponent {

    @Override
    public int readQuorum() throws RemoteException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int writeQuorum() throws RemoteException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ArrayList<MyDownloadInfo> topThreeDownloads() throws RemoteException {
        // TODO Auto-generated method stub
        return new ArrayList<MyDownloadInfo>();
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
}