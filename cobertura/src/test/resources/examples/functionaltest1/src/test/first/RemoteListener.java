package test.first;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public final class RemoteListener extends UnicastRemoteObject implements RemoteInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RemoteListener() throws RemoteException
	{
		
	}
}
