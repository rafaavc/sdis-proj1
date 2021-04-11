import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import channels.MulticastChannel;
import channels.MulticastChannel.ChannelType;
import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
import exceptions.ArgsException;

public class Main {
    public static void main(String[] args) throws Exception {
        PeerConfiguration configuration = parseArgs(args);
        Peer peer = new Peer(configuration);

        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(configuration.getServiceAccessPoint(), (Remote) peer);

        Runtime.getRuntime().addShutdownHook(new Thread() { 
            public void run() {
                System.out.println("Closing multicast sockets and ubinding from registry..."); 

                for (MulticastChannel channel : configuration.getChannels()) channel.close();
                
                configuration.getThreadScheduler().shutdown();

                try {
                    peer.writeState();
                } catch (IOException e1) {
                    System.out.println(e1.getMessage());
                    e1.printStackTrace();
                }

                try {
                    registry.unbind(configuration.getServiceAccessPoint());
                    System.out.println("Unbound successfully."); 
                } catch (RemoteException | NotBoundException e) {
                    System.err.println("Error unbinding."); 
                }
            } 
        });
    }

    public static PeerConfiguration parseArgs(String args[]) throws Exception {
        if (args.length != 9) throw new ArgsException(ArgsException.Type.ARGS_LENGTH);

        // Need to verify better
        String protocolVersion = args[0];
        String peerId = args[1];
        String serviceAccessPoint = args[2];
        MulticastChannel mc = new MulticastChannel(ChannelType.CONTROL, args[3], Integer.parseInt(args[4])); // Multicast control
        MulticastChannel mdb = new MulticastChannel(ChannelType.BACKUP, args[5], Integer.parseInt(args[6])); // Multicast data backup
        MulticastChannel mdr = new MulticastChannel(ChannelType.RESTORE, args[7], Integer.parseInt(args[8])); // Multicast data restore

        PeerConfiguration configuration = new PeerConfiguration(new ProtocolVersion(protocolVersion), peerId, serviceAccessPoint, mc, mdb, mdr);

        return configuration;
    } 
}
