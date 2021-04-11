package actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
import files.FileManager;
import messages.MessageFactory;
import state.ChunkInfo;

public class Reclaim {
    private final PeerConfiguration configuration;
    private final int availableSpaceDesired;

    public Reclaim(PeerConfiguration configuration, int availableSpaceDesired) {
        this.configuration = configuration;
        this.availableSpaceDesired = availableSpaceDesired;
    }

    public void execute() {
        try {
            // space desired < 0 means no space limit
            this.configuration.getPeerState().setMaximumStorageAvailable(availableSpaceDesired < 0 ? -1 : availableSpaceDesired);

            // calcular espaço ocupado
            float occupiedSpace = configuration.getPeerState().getOccupiedStorage();

            System.out.println("Occupied space: " + occupiedSpace);

            if (availableSpaceDesired < 0 || availableSpaceDesired >= occupiedSpace) return;

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
                // if replication degree == 1 fails (shortcoming of the protocol)
                chunksToRemove.add(chunk);
                occupiedSpace -= chunk.getSize();
                peerChunks.remove(0);
            }

            System.out.println("Chunks to remove: " + chunksToRemove);

            FileManager fileManager = new FileManager(this.configuration.getRootDir());

            for (ChunkInfo chunk : chunksToRemove) {
                byte[] msg = new MessageFactory(new ProtocolVersion(1, 0)).getRemovedMessage(this.configuration.getPeerId(), chunk.getFileId(), chunk.getChunkNo());
                
                this.configuration.getMC().send(msg);
                
                fileManager.deleteChunk(chunk.getFileId(), chunk.getChunkNo());
                this.configuration.getPeerState().deleteChunk(chunk);
            }

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
