package actions;

import java.util.List;

import configuration.PeerConfiguration;
import files.Chunk;
import files.File;
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
            this.configuration.getPeerState().addFile(info);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
}
