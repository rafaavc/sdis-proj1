import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import channels.ChannelListener;
import channels.MulticastChannel;
import channels.actions.Action;
import configuration.ClientInterface;
import configuration.PeerConfiguration;
import exceptions.ArgsException;
import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;
import files.Chunk;
import files.File;
import messages.MessageFactory;

public class Peer extends UnicastRemoteObject implements ClientInterface {
    private static final long serialVersionUID = 5157944159616018684L;
    private final PeerConfiguration configuration;

    public Peer(PeerConfiguration configuration) throws IOException, ChunkSizeExceeded, InvalidChunkNo {
        this.configuration = configuration;

        for (MulticastChannel channel : this.configuration.getChannels()) {
            new ChannelListener(channel, Action.get(this.configuration, channel.getType())).start();
        }

        System.out.println("Ready!");
    }

    public void backup(String filePath, int replicationDegree) throws RemoteException {
        try {
            File file = new File(filePath);
            MessageFactory factory = new MessageFactory(1, 0);
            for (Chunk chunk : file.getChunks()) {
                byte[] msg = factory.getPutchunkMessage(this.configuration.getPeerId(), file.getFileId(), replicationDegree, chunk.getChunkNo(), chunk.getData());
                this.configuration.getMDB().send(msg);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void hi() throws RemoteException {
        System.out.println("Hi");
    }

    public void testMulticast() throws RemoteException {
        try {
            this.configuration.getMC().send("Hi in MC");
            this.configuration.getMDB().send("Hi in MDB");
            this.configuration.getMDR().send("Hi in MDR");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
