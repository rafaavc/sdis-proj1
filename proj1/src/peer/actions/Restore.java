package actions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import configuration.PeerConfiguration;
import messages.MessageFactory;
import messages.trackers.ChunkTracker;
import state.ChunkPair;
import state.FileInfo;

public class Restore {
    private final PeerConfiguration configuration;
    private final String fileId;

    public Restore(PeerConfiguration configuration, String fileId) {
        this.configuration = configuration;
        this.fileId = fileId;
    }

    public void execute() {
        try {
            FileInfo file = configuration.getPeerState().getFile(fileId);
            ChunkTracker chunkTracker = configuration.getChunkTracker();

            Map<ChunkPair, byte[]> chunksToGet = new HashMap<>();

            for (ChunkPair chunk : file.getChunks())
            {
                byte[] msg = new MessageFactory(configuration.getProtocolVersion()).getGetchunkMessage(this.configuration.getPeerId(), file.getFileId(), chunk.getChunkNo());
                chunksToGet.put(chunk, msg);
            }

            for (ChunkPair chunk : chunksToGet.keySet()) {
                chunkTracker.startWaitingForChunk(file.getFileId(), chunk.getChunkNo());
            }

            configuration.getThreadScheduler().schedule(new ChunksRestore(configuration, file, chunksToGet), 0, TimeUnit.MILLISECONDS);
        } 
        catch(Exception e) 
        {
            System.err.println(e.getMessage());
        }
    }
}
