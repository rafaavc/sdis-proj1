package actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import configuration.PeerConfiguration;
import files.FileManager;
import messages.MessageFactory;
import messages.trackers.ChunkTracker;
import state.ChunkPair;
import state.FileInfo;

public class Restore extends Thread {
    private final PeerConfiguration configuration;
    private final String fileId;

    public Restore(PeerConfiguration configuration, String fileId) {
        this.configuration = configuration;
        this.fileId = fileId;
    }

    @Override
    public void run() {
        try {
            FileInfo file = configuration.getPeerState().getFile(fileId);
            ChunkTracker chunkTracker = configuration.getChunkTracker();

            Map<ChunkPair, byte[]> chunksToGet = new HashMap<>();

            for (ChunkPair chunk : file.getChunks()) chunksToGet.put(chunk, null);

            for (ChunkPair chunk : chunksToGet.keySet()) {
                chunkTracker.startWaitingForChunk(file.getFileId(), chunk.getChunkNo());
            }

            int count = 0, sleepAmount = 1000;
            while(count < 5)
            {
                for (ChunkPair chunk : chunksToGet.keySet()) 
                {
                    byte[] msg = chunksToGet.get(chunk);
                    if (msg == null) {
                        msg = new MessageFactory(configuration.getProtocolVersion()).getGetchunkMessage(this.configuration.getPeerId(), file.getFileId(), chunk.getChunkNo());
                        chunksToGet.put(chunk, msg);
                    }

                    this.configuration.getMC().send(msg);
                }

                Thread.sleep(sleepAmount);
                
                Map<ChunkPair, byte[]> chunksToGetCopy = new HashMap<>(chunksToGet);

                for (ChunkPair chunk : chunksToGetCopy.keySet())
                {
                    if (chunkTracker.hasReceivedChunkData(file.getFileId(), chunk.getChunkNo())) {
                        chunksToGet.remove(chunk);
                    }
                }

                if (chunksToGet.size() == 0) break;

                sleepAmount *= 2;
                count++;
            }

            if (chunksToGet.size() != 0) {
                System.out.println("Couldn't restore file, chunks missing:");
                for (ChunkPair chunk : chunksToGet.keySet()) {
                    System.out.println("- " + chunk.getChunkNo());
                }
                System.out.println();
                return;
            }
        
            List<byte[]> chunks = chunkTracker.getFileChunks(file.getFileId());
            System.out.println("Received " + chunks.size() + "/" + file.getChunks().size() + " chunks.");

            FileManager fileManager = new FileManager(this.configuration.getRootDir());
            fileManager.writeFile(file.getFileName(), chunks);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
