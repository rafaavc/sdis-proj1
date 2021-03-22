package configuration;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    public void hi() throws RemoteException;
    public void testMulticast() throws RemoteException;
    public void backup(String filePath, int replicationDegree) throws RemoteException;
}
