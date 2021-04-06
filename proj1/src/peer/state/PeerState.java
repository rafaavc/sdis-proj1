package state;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class PeerState implements Serializable {
    private static final long serialVersionUID = 3474820596488159542L;

    private static String stateFileName = "metadata";
    private final String dir;

    private final Map<String, FileInfo> files = new HashMap<>();
    private final Map<String, Map<Integer, ChunkInfo>> chunks = new HashMap<>();

    private int maximumSpaceAvailable = -1;

    public PeerState(String dir) {
        this.dir = dir;
    }

    public void setMaximumStorageAvailable(int maximumSpaceAvailable) {
        this.maximumSpaceAvailable = maximumSpaceAvailable;
    }

    public int getMaximumStorageAvailable() {
        return this.maximumSpaceAvailable;
    }

    public float getOccupiedStorage() {
        float occupiedStorage = 0;
        for (ChunkInfo chunk : getChunks()) occupiedStorage += chunk.getSize();
        return occupiedStorage;
    }

    public void addFile(FileInfo f) throws IOException {
        files.put(f.getFileId(), f);
    }

    public void deleteFile(String fileId) {
        this.files.remove(fileId);
    }

    public boolean ownsFile(String fileId) {
        return files.containsKey(fileId);
    }

    public void addChunk(ChunkInfo c) throws IOException {

        if (chunks.containsKey(c.getFileId()) && !chunks.get(c.getFileId()).containsKey(c.getChunkNo())) {
            chunks.get(c.getFileId()).put(c.getChunkNo(), c);
        } else if (!chunks.containsKey(c.getFileId())) {
            Map<Integer, ChunkInfo> info = new HashMap<>();
            info.put(c.getChunkNo(), c);
            chunks.put(c.getFileId(), info);
        }
        //this.write(); // TO REMOVE
    }

    public void updateChunkPerceivedRepDegree(String fileId, int chunkNo, int perceivedReplicationDegree) {
        if (chunks.containsKey(fileId)) {
            Map<Integer, ChunkInfo> fileChunks = chunks.get(fileId);
            if (fileChunks.containsKey(chunkNo)) {
                fileChunks.get(chunkNo).setPerceivedReplicationDegree(perceivedReplicationDegree); // TODO this may need improvements
            }
        }
    }

    public void deleteChunk(ChunkInfo c) {
        chunks.get(c.getFileId()).remove(c.getChunkNo());
    }

    public void deleteFileChunks(String fileId) {
        this.chunks.remove(fileId);
    }

    public FileInfo getFile(String fileId) {
        return files.get(fileId);
    }

    public boolean hasChunk(String fileId, int chunkNo) {
        return chunks.containsKey(fileId) && chunks.get(fileId).containsKey(chunkNo);
    }

    public ChunkInfo getChunk(String fileId, int chunkNo) {
        return chunks.containsKey(fileId) ? chunks.get(fileId).get(chunkNo) : null;
    }

    public List<ChunkInfo> getChunks() {
        List<ChunkInfo> res = new ArrayList<>();

        for (Map<Integer, ChunkInfo> m : this.chunks.values()) {
            for (ChunkInfo c : m.values()) res.add(c);
        }

        return res;
    }

    public static PeerState read(String dir) throws IOException, ClassNotFoundException {
        File f = new File(dir + "/" + stateFileName);
        if (!f.exists()) {
            System.out.println("Didn't find a stored state, creating new one.");
            return new PeerState(dir);
        }

        FileInputStream fin = new FileInputStream(dir + "/" + stateFileName);
        ObjectInputStream in = new ObjectInputStream(fin);
        
        PeerState state = (PeerState) in.readObject();

        in.close();

        return state;
    }

    public void write() throws IOException {
        String filePath = this.dir + "/" + stateFileName;

        File f = new File(this.dir);
        if (!f.exists()) f.mkdirs();

        FileOutputStream fout = new FileOutputStream(filePath);
        ObjectOutputStream out = new ObjectOutputStream(fout);
  
        out.writeObject(this);
  
        out.close();
    }

    @Override
    public String toString() {
        if (chunks.isEmpty() && files.isEmpty()) return "I haven't sent any files nor backed up any chunks.\n";

        StringBuilder res = new StringBuilder();

        if (!chunks.isEmpty()) {
            res.append("I've stored these chunks:\n");
            for (Map<Integer, ChunkInfo> chunks : chunks.values()) {
                for (ChunkInfo chunk : chunks.values()) {
                    res.append(chunk);
                    res.append("\n");
                }
            }
            res.append("\n");
        }

        if (!files.isEmpty()) {
            res.append("I've sent these files for backup:\n");
            for (FileInfo file : files.values()) {
                res.append(file);
                res.append("\n");
            }
            res.append("\n");
        }

        res.append("Occupied storage: " + getOccupiedStorage() + "\n");

        return res.toString();
    }
}
