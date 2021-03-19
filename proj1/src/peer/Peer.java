import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import exceptions.CLArgsException;
import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;

public class Peer implements ClientInterface, Serializable {
    private static final long serialVersionUID = -2366152158020082928L;
    private PeerConfiguration configuration;

    public static PeerConfiguration parseArgs(String args[]) throws CLArgsException {
        if (args.length != 9) throw new CLArgsException(CLArgsException.Type.ARGS_LENGTH);

        String protocolVersion = args[0];
        String peerId = args[1];
        String serviceAccessPoint = args[2];
        MulticastChannelName mc = new MulticastChannelName(args[3], Integer.parseInt(args[4]));
        MulticastChannelName mdb = new MulticastChannelName(args[5], Integer.parseInt(args[6]));
        MulticastChannelName mdr = new MulticastChannelName(args[7], Integer.parseInt(args[8]));

        return new PeerConfiguration(protocolVersion, peerId, serviceAccessPoint, mc, mdb, mdr);
    }
    public static void main(String[] args) throws RemoteException, NotBoundException, IOException, ChunkSizeExceeded, InvalidChunkNo, InterruptedException, AlreadyBoundException, CLArgsException {
        PeerConfiguration configuration = parseArgs(args);

        Peer peer = new Peer(configuration);

        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(configuration.getServiceAccessPoint(), (Remote) peer);

        Runtime.getRuntime().addShutdownHook(new Thread() { 
            public void run() { 
                System.out.println("Unbinding from registry..."); 
                try {
                    registry.unbind(configuration.getServiceAccessPoint());
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

    public Peer(PeerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void hi() throws RemoteException {
        System.out.println("Hi from peer" + this.configuration.getPeerId());
    }
}
