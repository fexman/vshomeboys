package solution.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import solution.model.*;

public interface ISubscribe extends Remote {
	/**
	 * adds a subscription
	 * @param s
	 * @throws RemoteException
	 */
	public void notificate(Subscription s) throws RemoteException;
}
