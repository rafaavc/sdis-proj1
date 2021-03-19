import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BackupServiceInterface {
    public static void main(String[] args) throws AccessException, RemoteException, NotBoundException {
        if (args.length != 1) {
            System.err.println("I need the peer's rmi registry name.");
            System.exit(1);
        }

        Registry registry = LocateRegistry.getRegistry();
        ClientInterface stub = (ClientInterface) registry.lookup(args[0]);
        stub.hi();
    }
}
