package actions;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import configuration.PeerConfiguration;
import state.ChunkInfo;

public class ReclaimChunkBackup implements Runnable {
    private final ChunkInfo chunk;
    private int count, sleepAmount;
    private final PeerConfiguration configuration;
    private final byte[] putchunkMsg, storedMsg;

    public ReclaimChunkBackup(PeerConfiguration configuration, ChunkInfo chunk, byte[] putchunkMsg, byte[] storedMsg) {
        this.count = 0;
        this.sleepAmount = 1000;
        this.configuration = configuration;
        this.chunk = chunk;
        this.putchunkMsg = putchunkMsg;
        this.storedMsg = storedMsg;
    }

    private ReclaimChunkBackup(PeerConfiguration configuration, ChunkInfo chunk, byte[] putchunkMsg, byte[] storedMsg, int count, int sleepAmount) {
        this(configuration, chunk, putchunkMsg, storedMsg);
        this.count = count;
        this.sleepAmount = sleepAmount;
    }

    @Override
    public void run() {
        ScheduledThreadPoolExecutor threadScheduler = configuration.getThreadScheduler();

        try
        {
            configuration.getMDB().send(putchunkMsg);
        }
        catch(Exception e)
        {
            System.err.println(e.getMessage());
        }

        int randVal = configuration.getRandomDelay(400);
        
        // sends the stored corresponding to this peer after the rand(400) ms delay
        threadScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try
                {
                    configuration.getMC().send(storedMsg);
                }
                catch(Exception e)
                {
                    System.err.println(e.getMessage());
                }

                // checks the replication degree after sleepAmount ms
                threadScheduler.schedule(new Runnable() {
                    @Override
                    public void run() {                
                        int replicationDegree = configuration.getStoredTracker().getStoredCount(chunk.getFileId(), chunk.getChunkNo());
                        
                        System.out.println("Checking stored count = " + replicationDegree);

                        if (count < 5 && replicationDegree < chunk.getDesiredReplicationDegree())
                        {
                            threadScheduler.schedule(new ReclaimChunkBackup(configuration, chunk, putchunkMsg, storedMsg, count + 1, sleepAmount * 2), 0, TimeUnit.MILLISECONDS);
                            return;
                        }

                        if (replicationDegree == 0)
                            System.out.println("Couldn't backup chunk " + chunk.getChunkNo() + ". Perceived = " + replicationDegree);
                        
                        else if (replicationDegree < chunk.getDesiredReplicationDegree())
                            System.out.println("Couldn't backup chunk " + chunk.getChunkNo() + " with the desired replication degree. Perceived = " + replicationDegree);

                        else
                            System.out.println("Backed up chunk successfully.");

                        chunk.setPerceivedReplicationDegree(replicationDegree);
                    }
                }, sleepAmount - randVal, TimeUnit.MILLISECONDS);
            }
        }, randVal, TimeUnit.MILLISECONDS);
    }
}
