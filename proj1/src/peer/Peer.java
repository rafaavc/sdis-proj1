import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import channels.ChannelListener;
import channels.MulticastChannel;
import channels.handlers.Handler;
import configuration.ClientInterface;
import configuration.PeerConfiguration;
import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;
import state.PeerState;
import actions.Backup;

public class Peer extends UnicastRemoteObject implements ClientInterface {
    private static final long serialVersionUID = 5157944159616018684L;
    private final PeerConfiguration configuration;

    public Peer(PeerConfiguration configuration) throws IOException, ChunkSizeExceeded, InvalidChunkNo, ClassNotFoundException {
        this.configuration = configuration;

        System.out.println(this.getState());

        for (MulticastChannel channel : this.configuration.getChannels()) {
            new ChannelListener(channel, Handler.get(this.configuration, channel.getType())).start();
        }

        System.out.println("Ready!");
    }

    public void writeState() throws IOException {
        this.getState().write();
    }

    /* RMI interface */

    public void backup(String filePath, int desiredReplicationDegree) throws RemoteException {
        new Backup(this.configuration, filePath, desiredReplicationDegree).start();
    }

    public PeerState getState() {
        return this.configuration.getState();
    }

    public void hi() throws RemoteException {
        System.out.println("Hi");
    }

    public void testMulticast() throws RemoteException {
        System.out.println("This functionality is temporarily shut down.");
        // try {
        //     this.configuration.getMC().send("Hi in MC");
        //     this.configuration.getMDB().send("Hi in MDB");
        //     this.configuration.getMDR().send("Hi in MDR");
        // } catch (IOException e) {
        //     System.err.println(e.getMessage());
        // }

    }
}

