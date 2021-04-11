package actions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
import files.Chunk;
import files.ChunkedFile;
import messages.MessageFactory;
import messages.trackers.StoredTracker;
import state.FileInfo;

public class Backup {
    private final PeerConfiguration configuration;
    private final String filePath;
    private final int desiredReplicationDegree;

    public Backup(PeerConfiguration configuration, String filePath, int desiredReplicationDegree) {
        this.configuration = configuration;
        this.filePath = filePath;
        this.desiredReplicationDegree = desiredReplicationDegree;
    }

    public void execute() {
        try {

            ChunkedFile file = new ChunkedFile(filePath);
            FileInfo info = new FileInfo(filePath, file.getFileId(), desiredReplicationDegree);
            StoredTracker storedTracker = configuration.getStoredTracker();

            Map<Chunk, byte[]> chunksToSend = new HashMap<>();

            for (Chunk chunk : file.getChunks())
            {   
                byte[] msg = new MessageFactory(new ProtocolVersion(1, 0)).getPutchunkMessage(this.configuration.getPeerId(), file.getFileId(), desiredReplicationDegree, chunk.getChunkNo(), chunk.getData());
                chunksToSend.put(chunk, msg);
            }

            System.out.println("I split the file into these chunks: " + chunksToSend);

            this.configuration.getPeerState().addFile(info);

            for (Chunk chunk : chunksToSend.keySet()) {
                storedTracker.resetStoredCount(chunk.getFileId(), chunk.getChunkNo());
            }

            configuration.getThreadScheduler().schedule(new ChunksBackup(configuration, info, chunksToSend), 0, TimeUnit.MILLISECONDS);
            
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
}
