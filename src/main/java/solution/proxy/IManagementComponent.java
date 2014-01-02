package solution.proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import solution.model.MyDownloadInfo;

/**
 * RMI-Interface for the Management-Component of the Proxy.
 */
public interface IManagementComponent extends Remote {

    /**
     * Returns the Read-Quorum from Gifford's scheme. 
     * @return int > 0
     * @throws RemoteException, if remote Proxy is not available.
     */
    public int readQuorum() throws RemoteException;
    
    /**
     * Returns the Write-Quorum from Gifford's scheme. 
     * @return int > 0
     * @throws RemoteException, if remote Proxy is not available.
     */
    public int writeQuorum() throws RemoteException;
    
    /**
     * Returns the top three downloads in a list of {@link solution.model.MyDownloadInfo}. 
     * @return ArrayList, sorted descendingly by the downloads of the given file.
     * @throws RemoteException, if remote Proxy is not available.
     */
    public ArrayList<MyDownloadInfo> topThreeDownloads() throws RemoteException;
    
    //TODO: to be implemented
    public String subscribe(String filename, int numberOfDownloads) throws RemoteException;

    //TODO: to be implemented
    public String getProxyPublicKey() throws RemoteException;
    
    //TODO: to be implemented
    public boolean setUserPublicKey() throws RemoteException;
}