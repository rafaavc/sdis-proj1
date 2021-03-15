import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Peer {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        if (System.getenv("BACKUP_SERVICE_ENV").equals("docker")) {
            Registry registry = LocateRegistry.getRegistry("rmi");
            System.out.println("I am a docker container!\n" + registry);
        }
        while(true);
    }
}

