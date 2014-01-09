package solution.proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import solution.model.FileInfo;


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
    public ArrayList<FileInfo> topThreeDownloads() throws RemoteException;
    
    //TODO: to be implemented
    public String subscribe(String filename, int numberOfDownloads) throws RemoteException;

    /**
     * Gets the public key from the proxy
     * @return proxy public key in bytes
     * @throws RemoteException
     */
    public byte[] getProxyPublicKey() throws RemoteException;
    
    /**
     * Sends public key of a user to proxy
     * @param user, username
     * @param file, public key file
     * @return true if success, false if failure
     * @throws RemoteException
     */
    public boolean setUserPublicKey(String user, byte [] file) throws RemoteException;

    public void setProxyInstance(ProxyCli i) throws RemoteException;
}