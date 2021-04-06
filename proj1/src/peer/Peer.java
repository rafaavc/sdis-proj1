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
import actions.Delete;
import actions.Restore;
import actions.Reclaim;

public class Peer extends UnicastRemoteObject implements ClientInterface {
    private static final long serialVersionUID = 5157944159616018684L;
    private final PeerConfiguration configuration;

    public Peer(PeerConfiguration configuration) throws IOException, ChunkSizeExceeded, InvalidChunkNo, ClassNotFoundException {
        this.configuration = configuration;

        System.out.println(this.getPeerState());

        for (MulticastChannel channel : this.configuration.getChannels()) {
            new ChannelListener(channel, Handler.get(this.configuration, channel.getType())).start();
        }

        System.out.println("Ready!");
    }

    public void writeState() throws IOException {
        this.getPeerState().write();
    }

    /* RMI interface */

    public void backup(String filePath, int desiredReplicationDegree) throws RemoteException {
        new Backup(this.configuration, filePath, desiredReplicationDegree).start();
    }

    public void restore(String fileName, String fileId) throws RemoteException {
        if (!getPeerState().ownsFile(fileId)) {
            System.err.println("The file '" + fileName + "' doesn't exist in my history.");
            return;
        }
        new Restore(configuration, fileId).start();
    }

    public void delete(String fileName, String fileId) throws RemoteException {
        if (!getPeerState().ownsFile(fileId)) {
            System.err.println("The file '" + fileName + "' doesn't exist in my history.");
            return;
        }
        new Delete(configuration, fileId).start();
    }

    public void reclaim(int kb) throws RemoteException {
        new Reclaim(configuration, kb).start();
    }

    public PeerState getPeerState() {
        return this.configuration.getPeerState();
    }

    public void hi() throws RemoteException {
        System.out.println("Hi");
    }
}

