package actions;

import java.util.HashMap;
import java.util.Map;

import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
import files.Chunk;
import files.ChunkedFile;
import messages.MessageFactory;
import messages.trackers.StoredTracker;
import state.ChunkPair;
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

            for (Chunk chunk : file.getChunks()) chunksToSend.put(chunk, null);

            System.out.println("I split the file into these chunks: " + chunksToSend);

            this.configuration.getPeerState().addFile(info);

            for (Chunk chunk : chunksToSend.keySet()) {
                storedTracker.resetStoredCount(chunk.getFileId(), chunk.getChunkNo());
            }

            int count = 0, sleepAmount = 1000;
            while(count < 5)
            {
                for (Chunk chunk : chunksToSend.keySet()) 
                {
                    byte[] msg = chunksToSend.get(chunk);
                    if (msg == null) {
                        msg = new MessageFactory(new ProtocolVersion(1, 0)).getPutchunkMessage(this.configuration.getPeerId(), file.getFileId(), desiredReplicationDegree, chunk.getChunkNo(), chunk.getData());
                        chunksToSend.put(chunk, msg);
                    }
                    
                    this.configuration.getMDB().send(msg);
                }

                Thread.sleep(sleepAmount);

                Map<Chunk, byte[]> chunksToSendCopy = new HashMap<>(chunksToSend);

                for (Chunk chunk : chunksToSendCopy.keySet()) {
                    System.out.println("Checking stored count = " + storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo()));
                    int replicationDegree = storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo());
                   
                    if (replicationDegree >= desiredReplicationDegree) {
                        info.addChunk(new ChunkPair(chunk.getChunkNo(), replicationDegree));
                        chunksToSend.remove(chunk);
                    }
                }

                if (chunksToSend.size() == 0) break;

                sleepAmount *= 2;
                count++;
            }
            
            for (Chunk chunk : chunksToSend.keySet()) {
                int replicationDegree = storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo());

                if (replicationDegree == 0) {
                    new Delete(this.configuration, info.getFileId()).execute();
                    System.err.println("Wasn't able to backup file: chunk " + chunk.getChunkNo() + " was not backed up by any peers");
                    return;
                }
                info.addChunk(new ChunkPair(chunk.getChunkNo(), replicationDegree));
                System.out.println("Couldn't backup chunk " + chunk.getChunkNo() + " with the desired replication degree. Perceived = " + replicationDegree);
            }

            System.out.println("Backed up successfully!");
            
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
}
