import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import actions.BackupPacketAction;
import actions.ControlPacketAction;
import actions.RestorePacketAction;
import exceptions.CLArgsException;
import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;

public class Peer {
    private final PeerConfiguration configuration;
    private final Client client;
    private final List<ChannelListener> channels = new ArrayList<>();

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
        Client client = new Client();        

        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(configuration.getServiceAccessPoint(), (Remote) client);

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

        Peer peer = new Peer(configuration, client);
        peer.start();
    }

    public Peer(PeerConfiguration configuration, Client client) {
        this.configuration = configuration;
        this.client = client;

        for (MulticastChannel channel : configuration.getChannels()) {
            this.channels.add(new ChannelListener(channel));
        }
    }

    public void start() {
        this.channels.forEach((ChannelListener channel) -> channel.start());
    }
}
