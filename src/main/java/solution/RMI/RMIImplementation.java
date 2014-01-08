package solution.RMI;

import java.rmi.RemoteException;
import java.util.SortedSet;
import java.rmi.registry.*;

public class RMIImplementation implements RMIInterface {

	@Override
	public String test() throws RemoteException {
		// TODO Auto-generated method stub
		return "Hello World";
	}
}
