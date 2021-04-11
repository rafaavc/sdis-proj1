import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import channels.ChannelListener;
import channels.MulticastChannel;
import channels.handlers.Handler;
import configuration.ClientInterface;
import configuration.PeerConfiguration;
import state.PeerState;
import actions.Backup;
import actions.CheckDeleted;
import actions.Delete;
import actions.Restore;
import actions.Reclaim;

public class Peer extends UnicastRemoteObject implements ClientInterface {
    private static final long serialVersionUID = 5157944159616018684L;
    private final PeerConfiguration configuration;

    public Peer(PeerConfiguration configuration) throws Exception {
        this.configuration = configuration;

        System.out.println(this.getPeerState());

        for (MulticastChannel channel : this.configuration.getChannels())
        {
            new ChannelListener(channel, Handler.get(this.configuration, channel.getType()), configuration.getThreadScheduler()).start();
        }

        System.out.println("Running on protocol version " + configuration.getProtocolVersion() + ". Ready!");

        if (configuration.getProtocolVersion().equals("1.1")) {
            new CheckDeleted(configuration).execute();
        }
    }

    public void writeState() throws IOException {
        this.getPeerState().write();
    }

    /* RMI interface */

    public void backup(String filePath, int desiredReplicationDegree) throws RemoteException {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();

        if (getPeerState().ownsFileWithName(fileName)) 
        {
            String fileId = getPeerState().getFileId(fileName);
            System.out.println("The file " + fileName + " had an older version. Deleting it.");

            new Delete(configuration, fileId).execute();
        }

        new Backup(configuration, filePath, desiredReplicationDegree).execute();
    }

    public void restore(String fileName) throws RemoteException {
        if (!getPeerState().ownsFileWithName(fileName)) 
        {
            System.err.println("The file '" + fileName + "' doesn't exist in my history.");
            return;
        }
        new Restore(configuration, getPeerState().getFileId(fileName)).execute();
    }

    public void delete(String fileName) throws RemoteException {
        if (!getPeerState().ownsFileWithName(fileName))
        {
            System.err.println("The file '" + fileName + "' doesn't exist in my history.");
            return;
        }
        String fileId = getPeerState().getFileId(fileName);
        new Delete(configuration, fileId).execute();
    }

    public void reclaim(int kb) throws RemoteException {
        new Reclaim(configuration, kb).execute();
    }

    public PeerState getPeerState() {
        return this.configuration.getPeerState();
    }

    public void hi() throws RemoteException {
        System.out.println("Hi");
    }
}

