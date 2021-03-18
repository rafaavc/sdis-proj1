import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;

public class Peer implements ClientInterface, Serializable {
    private static final long serialVersionUID = -2366152158020082928L;
    private String id;
    public static void main(String[] args) throws RemoteException, NotBoundException, IOException, ChunkSizeExceeded, InvalidChunkNo, InterruptedException, AlreadyBoundException {
        if (args.length != 1) System.err.println("I need a unique id!");

        Peer peer = new Peer(args[0]);

        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("peer" + args[0], (Remote) peer);

        Runtime.getRuntime().addShutdownHook(new Thread() { 
            public void run() { 
                System.out.println("Unbinding from registry..."); 
                try {
                    registry.unbind("peer" + args[0]);
                    System.out.println("Unbound successfully."); 
                } catch (RemoteException | NotBoundException e) {
                    System.err.println("Error unbinding.");
                }
            } 
        }); 

        //FileManager fileManager = new FileManager("test");
        //List<Byte> data = fileManager.read("testFile");
        //System.out.println(Chunk.getChunks("id", data));
        System.out.println("Ready");
        peer.hi();

        while (true);
    }

    public Peer(String id) {
        this.id = id;
    }

    public void hi() throws RemoteException {
        System.out.println("Hi from peer" + this.id);
    }
}
