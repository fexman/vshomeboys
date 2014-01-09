package solution.client;

import java.rmi.RemoteException;

import solution.model.Subscription;

public class Subscribe implements ISubscribe {

	@Override
	public void notificate(Subscription s) throws RemoteException {
		System.out.println("Notification: "+s.getFilename()+" got downloaded "+s.getCount()+" times!");
	}

}
