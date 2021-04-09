package actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import configuration.PeerConfiguration;
import files.FileManager;
import state.ChunkInfo;

public class Reclaim extends Thread {
    private final PeerConfiguration configuration;
    private final int availableSpaceDesired;

    public Reclaim(PeerConfiguration configuration, int availableSpaceDesired) {
        this.configuration = configuration;
        this.availableSpaceDesired = availableSpaceDesired;
    }

    @Override
    public void run() {
        try {
            this.configuration.getPeerState().setMaximumStorageAvailable(availableSpaceDesired);

            // calcular espaço ocupado
            float occupiedSpace = configuration.getPeerState().getOccupiedStorage();

            System.out.println("Occupied space: " + occupiedSpace);

            if (availableSpaceDesired >= occupiedSpace) return;

            List<ChunkInfo> peerChunks = configuration.getPeerState().getChunks();

            // remover e mandar msg para cada chunk que seja necessário remover (os que tiverem perceived degree maior que o desired primeiro)
            Collections.sort(peerChunks, new Comparator<ChunkInfo>() {
                @Override
                public int compare(ChunkInfo chunkInfo1, ChunkInfo chunkInfo2) {
                    int chunkInfo1Diff = chunkInfo1.getPerceivedReplicationDegree() - chunkInfo1.getDesiredReplicationDegree();
                    int chunkInfo2Diff = chunkInfo2.getPerceivedReplicationDegree() - chunkInfo2.getDesiredReplicationDegree();
                    return chunkInfo2Diff - chunkInfo1Diff; // order in descending
                }
            });

            System.out.println("Chunks ordered: " + peerChunks);

            List<ChunkInfo> chunksToRemove = new ArrayList<>();

            while(occupiedSpace > availableSpaceDesired && peerChunks.size() != 0) {
                ChunkInfo chunk = peerChunks.get(0);
                // TODO check if replication degree == 1
                chunksToRemove.add(chunk);
                occupiedSpace -= chunk.getSize();
                peerChunks.remove(0);
            }

            System.out.println("Chunks to remove: " + chunksToRemove);

            FileManager fileManager = new FileManager(this.configuration.getRootDir());

            for (ChunkInfo chunk : chunksToRemove) {
                byte[] msg = this.configuration.getMessageFactory().getRemovedMessage(this.configuration.getPeerId(), chunk.getFileId(), chunk.getChunkNo());
                
                // int count = 0;
                // while(count < 5) {
                    this.configuration.getMC().send(msg);
                //     Thread.sleep(500);
                //     count++;
                // }
                
                fileManager.deleteChunk(chunk.getFileId(), chunk.getChunkNo());
                this.configuration.getPeerState().deleteChunk(chunk);
            }

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
