package state;

import java.io.Serializable;

public class ChunkInfo extends ChunkPair implements Serializable {
    private final String fileId;
    private final int desiredReplicationDegree;
    public ChunkInfo(String fileId, int chunkNo, int perceivedReplicationDegree, int desiredReplicationDegree) {
        super(chunkNo, perceivedReplicationDegree);
        this.fileId = fileId;
        this.desiredReplicationDegree = desiredReplicationDegree;
    }

    public String getFileId() {
        return fileId;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    @Override
    public String toString() {
        return fileId + ":" + chunkNo;
    }
}
