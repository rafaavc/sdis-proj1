package actions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import configuration.PeerConfiguration;
import files.Chunk;
import messages.trackers.StoredTracker;
import state.ChunkPair;
import state.FileInfo;

public class ChunksBackup implements Runnable {
    private final Map<Chunk, byte[]> chunksToSend;
    private int count, sleepAmount;
    private final PeerConfiguration configuration;
    private final FileInfo info;

    public ChunksBackup(PeerConfiguration configuration, FileInfo info, Map<Chunk, byte[]> chunksToSend) {
        this.count = 1;
        this.sleepAmount = 1000;
        this.configuration = configuration;
        this.info = info;
        this.chunksToSend = chunksToSend;
    }

    private ChunksBackup(PeerConfiguration configuration, FileInfo info, Map<Chunk, byte[]> chunksToSend, int count, int sleepAmount) {
        this(configuration, info, chunksToSend);
        this.count = count;
        this.sleepAmount = sleepAmount;
    }


    @Override
    public void run() {
        StoredTracker storedTracker = configuration.getStoredTracker();
        int desiredReplicationDegree = info.getDesiredReplicationDegree();

        try {
            for (Chunk chunk : chunksToSend.keySet()) 
            {
                byte[] msg = chunksToSend.get(chunk);        
                this.configuration.getMDB().send(msg);
            } 
        }
        catch(Exception e) 
        {
            System.err.println(e.getMessage());
            return;
        }

        configuration.getThreadScheduler().schedule(new Runnable() {
            @Override
            public void run() {
                Map<Chunk, byte[]> chunksToSendCopy = new HashMap<>(chunksToSend);

                for (Chunk chunk : chunksToSendCopy.keySet()) {
                    System.out.println("Checking stored count = " + storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo()));
                    int replicationDegree = storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo());
                    
                    if (replicationDegree >= desiredReplicationDegree) {
                        info.addChunk(new ChunkPair(chunk.getChunkNo(), replicationDegree));
                        chunksToSend.remove(chunk);
                    }
                }


                if (count < 5 && chunksToSend.size() != 0)
                {
                    configuration.getThreadScheduler().schedule(new ChunksBackup(configuration, info, chunksToSend, count+1, sleepAmount*2), 0, TimeUnit.MILLISECONDS);
                    return;
                }

                if (chunksToSend.size() != 0)
                {
                    for (Chunk chunk : chunksToSend.keySet()) {
                        int replicationDegree = storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo());
            
                        if (replicationDegree == 0) {
                            new Delete(configuration, info.getFileId()).execute();
                            System.err.println("Wasn't able to backup file: chunk " + chunk.getChunkNo() + " was not backed up by any peers");
                            return;
                        }
                        info.addChunk(new ChunkPair(chunk.getChunkNo(), replicationDegree));
                        System.out.println("Couldn't backup chunk " + chunk.getChunkNo() + " with the desired replication degree. Perceived = " + replicationDegree);
                    }
                }

                System.out.println("Backed up successfully!");
            }
        }, sleepAmount, TimeUnit.MILLISECONDS);
    }
}
