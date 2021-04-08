package state;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileInfo implements Serializable {
    private static final long serialVersionUID = 8712295865807115205L;
    
    private final String fileName, fileId;
    private final int desiredReplicationDegree;
    private final List<ChunkPair> chunks = new ArrayList<>();

    public FileInfo(String pathName, String fileId, int desiredReplicationDegree) {
        Path path = Paths.get(pathName);
        this.fileName = path.getFileName().toString();
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

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return fileName + ": " + fileId;
    }
}
