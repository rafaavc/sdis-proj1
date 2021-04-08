package actions;

import java.util.List;

import configuration.PeerConfiguration;
import files.Chunk;
import files.ChunkedFile;
import messages.trackers.StoredTracker;
import state.ChunkPair;
import state.FileInfo;

public class Backup extends Thread {
    private final PeerConfiguration configuration;
    private final String filePath;
    private final int desiredReplicationDegree;

    public Backup(PeerConfiguration configuration, String filePath, int desiredReplicationDegree) {
        this.configuration = configuration;
        this.filePath = filePath;
        this.desiredReplicationDegree = desiredReplicationDegree;
    }

    @Override
    public void run() {
        try {

            ChunkedFile file = new ChunkedFile(filePath);
            FileInfo info = new FileInfo(filePath, file.getFileId(), desiredReplicationDegree);
            StoredTracker storedTracker = configuration.getStoredTracker();

            List<Chunk> chunks = file.getChunks();
            System.out.println("I split the file into these chunks: " + chunks);

            this.configuration.getPeerState().addFile(info);

            for (Chunk chunk : chunks) {
                storedTracker.resetStoredCount(chunk.getFileId(), chunk.getChunkNo());
                
                byte[] msg = this.configuration.getMessageFactory().getPutchunkMessage(this.configuration.getPeerId(), file.getFileId(), desiredReplicationDegree, chunk.getChunkNo(), chunk.getData());
                
                int count = 0, sleepAmount = 1000, replicationDegree = 0;
                while(count < 5) {
                    this.configuration.getMDB().send(msg);
                    Thread.sleep(sleepAmount);
                    System.out.println("Checking stored count = " + storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo()));
                    replicationDegree = Math.max(storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo()), replicationDegree);
                    if (replicationDegree >= desiredReplicationDegree) break;
                    sleepAmount *= 2;
                    count++;
                }

                if (replicationDegree == 0) System.err.println("Could not backup chunk (replication degree == 0).");

                info.addChunk(new ChunkPair(chunk.getChunkNo(), replicationDegree));
            }
            
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
}
