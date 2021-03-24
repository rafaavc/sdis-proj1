import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import configuration.ClientInterface;

public class BackupServiceInterface {
    public static void main(String[] args) throws AccessException, RemoteException, NotBoundException {
        if (args.length < 2) {
            System.err.println("I need the peer's rmi registry name and the method to invoke.");
            System.exit(1);
        }

        Registry registry = LocateRegistry.getRegistry();
        ClientInterface stub = (ClientInterface) registry.lookup(args[0]);

        switch(args[1]) {
            case "hi":
                stub.hi();
                break;
            case "testMulticast":
                stub.testMulticast();
                break;
            case "backup": 
                if (args.length < 4) {
                    System.err.println("To backup I need the file path and the desired replication degree (from 0 to 9).");
                    System.exit(1);
                }
                stub.backup(args[2], Integer.parseInt(args[3]));
                break;
            default:
                break;
        }
    }
}