import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import channels.ChannelListener;
import channels.MulticastChannel;
import channels.actions.Action;
import configuration.ClientInterface;
import configuration.PeerConfiguration;
import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;
import files.Chunk;
import files.File;
import state.ChunkPair;
import state.FileInfo;
import state.PeerState;

public class Peer extends UnicastRemoteObject implements ClientInterface {
    private static final long serialVersionUID = 5157944159616018684L;
    private final PeerConfiguration configuration;
    private final PeerState state;

    public Peer(PeerConfiguration configuration) throws IOException, ChunkSizeExceeded, InvalidChunkNo, ClassNotFoundException {
        this.configuration = configuration;
        this.state = PeerState.read(configuration.getRootDir());

        System.out.println(this.state.getFiles());

        for (MulticastChannel channel : this.configuration.getChannels()) {
            new ChannelListener(channel, Action.get(this.configuration, channel.getType())).start();
        }

        System.out.println("Ready!");
    }

    public void backup(String filePath, int replicationDegree) throws RemoteException {
        try {
            File file = new File(filePath);
            FileInfo info = new FileInfo(filePath, file.getFileId(), replicationDegree);
            for (Chunk chunk : file.getChunks()) {
                byte[] msg = this.configuration.getMessageFactory().getPutchunkMessage(this.configuration.getPeerId(), file.getFileId(), replicationDegree, chunk.getChunkNo(), chunk.getData());
                this.configuration.getMDB().send(msg);
                int effectiveReplicationDegree = replicationDegree; // TODO
                info.addChunk(new ChunkPair(chunk.getChunkNo(), effectiveReplicationDegree));
                //while(x<5){while(respostas<replicationDegree) {send;delay=delay*2
            }
            this.state.addFile(info);
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
