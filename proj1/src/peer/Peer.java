import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import actions.BackupPacketAction;
import actions.ControlPacketAction;
import actions.RestorePacketAction;
import exceptions.CLArgsException;
import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;

public class Peer extends UnicastRemoteObject implements ClientInterface {
    private static final long serialVersionUID = 5157944159616018684L;
    private final PeerConfiguration configuration;

    public static PeerConfiguration parseArgs(String args[]) throws CLArgsException, NumberFormatException, UnknownHostException {
        if (args.length != 9) throw new CLArgsException(CLArgsException.Type.ARGS_LENGTH);

        // Need to verify better
        String protocolVersion = args[0];
        String peerId = args[1];
        String serviceAccessPoint = args[2];
        MulticastChannel mc = new MulticastChannel("MC", new ControlPacketAction(), args[3], Integer.parseInt(args[4])); // Multicast control
        MulticastChannel mdb = new MulticastChannel("MDB", new BackupPacketAction(), args[5], Integer.parseInt(args[6])); // Multicast data backup
        MulticastChannel mdr = new MulticastChannel("MDR", new RestorePacketAction(), args[7], Integer.parseInt(args[8])); // Multicast data restore

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

    }

    public Peer(PeerConfiguration configuration) throws IOException, ChunkSizeExceeded, InvalidChunkNo {
        this.configuration = configuration;

        for (MulticastChannel channel : this.configuration.getChannels()) {
            new ChannelListener(channel).start();

            // FileManager fileManager = new FileManager(configuration.getServiceAccessPoint());
            // List<Byte> data = fileManager.read("testFile");
            // System.out.println(Chunk.getChunks("aaa", data));
        }
    }

    public void hi() throws RemoteException {
        System.out.println("Hi");
    }

    public void testMulticast() throws RemoteException {
        try {
            MulticastSocket socket = new MulticastSocket();
            
            byte[] rbuf = "Hey guys!".getBytes();
            DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length, this.configuration.getMC().getHost(), this.configuration.getMC().getPort());

            socket.send(packet);

            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
