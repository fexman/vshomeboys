package solution.client;

import java.rmi.RemoteException;

import solution.model.Subscription;
import solution.proxy.Proxy;

public class Subscribe implements ISubscribe {

	@Override
	public void notificate(Subscription s) throws RemoteException {
		System.out.println("Notification: "+s.getFilename()+" got downloaded "+s.getCount()+" times!");
	}

}
