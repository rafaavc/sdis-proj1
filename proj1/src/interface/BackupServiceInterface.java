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
        try {
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
                        System.err.println("To backup I need the file path and the desired replication degree (from 1 to 9).");
                        System.exit(1);
                    }
                    try {
                        int desiredReplicationDegree = Integer.parseInt(args[3]);
                        if (desiredReplicationDegree < 1 || desiredReplicationDegree > 9) throw new NumberFormatException();
    
                        stub.backup(args[2], desiredReplicationDegree);
                    } catch(NumberFormatException e) {
                        System.err.println("The desired replication degree is not valid. It must be an integer in the inclusive range of 1 to 9.");
                        System.exit(1);
                    }
                    break;
                default:
                    break;
            }
        } catch(NotBoundException e) {
            System.out.println("Could not find peer with access point '" + args[0] + "'.");
            System.exit(1);
        }
    }
}
