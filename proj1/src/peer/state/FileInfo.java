package state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileInfo implements Serializable {
    private final String pathName, fileId;
    private final int desiredReplicationDegree;
    private final List<ChunkPair> chunks = new ArrayList<>();

    public FileInfo(String pathName, String fileId, int desiredReplicationDegree) {
        this.pathName = pathName;
        this.fileId = fileId;
        this.desiredReplicationDegree = desiredReplicationDegree;
    }

    public void addChunk(ChunkPair chunk) {
        this.chunks.add(chunk);
    }

    public List<ChunkPair> getChunks() {
        return chunks;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    public String getFileId() {
        return fileId;
    }

    public String getPathName() {
        return pathName;
    }

    public String toString() {
        return pathName + " " + fileId;
    }
}
