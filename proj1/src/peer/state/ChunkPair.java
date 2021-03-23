package state;

import java.io.Serializable;

public class ChunkPair implements Serializable {
    private int chunkNo, perceivedReplicationDegree;
    public ChunkPair(int chunkNo, int perceivedReplicationDegree) {
        this.chunkNo = chunkNo;
        this.perceivedReplicationDegree = perceivedReplicationDegree;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getPerceivedReplicationDegree() {
        return perceivedReplicationDegree;
    }
    
}
