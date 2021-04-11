package actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import configuration.PeerConfiguration;
import files.FileManager;
import messages.trackers.ChunkTracker;
import state.ChunkPair;
import state.FileInfo;

public class ChunksRestore implements Runnable {
    private final Map<ChunkPair, byte[]> chunksToGet;
    private int count, sleepAmount;
    private final PeerConfiguration configuration;
    private final FileInfo info;

    public ChunksRestore(PeerConfiguration configuration, FileInfo info, Map<ChunkPair, byte[]> chunksToGet) {
        this.count = 1;
        this.sleepAmount = 1000;
        this.configuration = configuration;
        this.info = info;
        this.chunksToGet = chunksToGet;
    }

    private ChunksRestore(PeerConfiguration configuration, FileInfo info, Map<ChunkPair, byte[]> chunksToGet, int count, int sleepAmount) {
        this(configuration, info, chunksToGet);
        this.count = count;
        this.sleepAmount = sleepAmount;
    }


    @Override
    public void run() {
        ChunkTracker chunkTracker = configuration.getChunkTracker();

        try {
            for (ChunkPair chunk : chunksToGet.keySet()) 
            {
                if (chunkTracker.hasReceivedChunk(info.getFileId(), chunk.getChunkNo())) continue;
                byte[] msg = chunksToGet.get(chunk);        
                this.configuration.getMC().send(msg);
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
                Map<ChunkPair, byte[]> chunksToGetCopy = new HashMap<>(chunksToGet);

                for (ChunkPair chunk : chunksToGetCopy.keySet()) {
                    if (chunkTracker.hasReceivedChunkData(info.getFileId(), chunk.getChunkNo())) {
                        chunksToGet.remove(chunk);
                    }
                }


                if (count < 5 && chunksToGet.size() != 0)
                {
                    configuration.getThreadScheduler().schedule(new ChunksRestore(configuration, info, chunksToGet, count+1, sleepAmount*2), 0, TimeUnit.MILLISECONDS);
                    return;
                }

                if (chunksToGet.size() != 0)
                {
                    System.out.println("Couldn't restore file, chunks missing:");
                    for (ChunkPair chunk : chunksToGet.keySet()) {
                        System.out.println("- " + chunk.getChunkNo());
                    }
                    System.out.println();
                    return;
                }

                List<byte[]> chunks = chunkTracker.getFileChunks(info.getFileId());
                System.out.println("Received " + chunks.size() + "/" + info.getChunks().size() + " chunks.");
    
                FileManager fileManager = new FileManager(configuration.getRootDir());

                try
                {
                    fileManager.writeFile(info.getFileName(), chunks);
                } 
                catch( Exception e)
                {
                    System.err.println(e.getMessage());
                }
            }
        }, sleepAmount, TimeUnit.MILLISECONDS);
    }
}
