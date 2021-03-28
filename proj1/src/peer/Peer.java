import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

import channels.ChannelListener;
import channels.MulticastChannel;
import channels.handlers.Handler;
import configuration.ClientInterface;
import configuration.PeerConfiguration;
import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;
import files.Chunk;
import files.File;
import state.ChunkInfo;
import state.ChunkPair;
import state.FileInfo;
import state.PeerState;

public class Peer extends UnicastRemoteObject implements ClientInterface {
    private static final long serialVersionUID = 5157944159616018684L;
    private final PeerConfiguration configuration;

    public Peer(PeerConfiguration configuration) throws IOException, ChunkSizeExceeded, InvalidChunkNo, ClassNotFoundException {
        this.configuration = configuration;

        Map<String, FileInfo> files = this.getState().getFiles();

        String output = "";
        if (files.size() != 0) {
            output += "# I've sent these files:\n";
            for (String key : files.keySet()) {
                output += files.get(key) + "\n";
            }
        } else output += "# I haven't sent any files :(\n";

        Map<String, ChunkInfo> chunks = this.getState().getChunks();
        if (chunks.size() != 0) {
            output += "# I've backed up these chunks:\n";
            for (String key : chunks.keySet()) {
                output += chunks.get(key) + "\n";
            }
        } else output += "# I haven't backed up any chunks :(\n";

        System.out.println(output);

        for (MulticastChannel channel : this.configuration.getChannels()) {
            new ChannelListener(channel, Handler.get(this.configuration, channel.getType())).start();
        }

        System.out.println("Ready!");
    }

    public PeerState getState() {
        return this.configuration.getState();
    }

    public void writeState() throws IOException {
        this.getState().write();
    }

    public void backup(String filePath, int desiredReplicationDegree) throws RemoteException {
        try {
            File file = new File(filePath);
            FileInfo info = new FileInfo(filePath, file.getFileId(), desiredReplicationDegree);

            List<Chunk> chunks = file.getChunks();
            System.out.println("I split the file into these chunks: " + chunks);

            for (Chunk chunk : chunks) {
                byte[] msg = this.configuration.getMessageFactory().getPutchunkMessage(this.configuration.getPeerId(), file.getFileId(), desiredReplicationDegree, chunk.getChunkNo(), chunk.getData());
                
                int count = 0, sleepAmount = 1000, replicationDegree = 0;
                while(count < 5) {
                    this.configuration.getMDB().send(msg);
                    Thread.sleep(sleepAmount);
                    System.out.println("Checking stored count = " + this.configuration.getStoredCount(chunk.getFileId(), chunk.getChunkNo()));
                    replicationDegree = Math.max(this.configuration.getStoredCount(chunk.getFileId(), chunk.getChunkNo()), replicationDegree);
                    if (replicationDegree >= desiredReplicationDegree) break;
                    sleepAmount *= 2;
                    count++;
                }

                if (replicationDegree == 0) System.err.println("Could not backup chunk (replication degree == 0).");

                info.addChunk(new ChunkPair(chunk.getChunkNo(), replicationDegree));
            }
            this.getState().addFile(info);
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

