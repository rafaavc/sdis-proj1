package configuration;

import java.rmi.Remote;
import java.rmi.RemoteException;

import state.PeerState;

public interface ClientInterface extends Remote {
    public void hi() throws RemoteException;
    public void testMulticast() throws RemoteException;
    public PeerState getState() throws RemoteException;
    public void backup(String filePath, int replicationDegree) throws RemoteException;
    public void delete(String fileName) throws RemoteException;
}
