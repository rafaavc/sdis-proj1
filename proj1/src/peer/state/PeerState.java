package state;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class PeerState implements Serializable {
    private static final long serialVersionUID = 3474820596488159542L;

    private static String stateFileName = "metadata";
    private final String dir;

    private Map<String, FileInfo> files = new HashMap<>();
    private Map<String, ChunkInfo> chunks = new HashMap<>();

    public PeerState(String dir) {
        this.dir = dir;
    }

    public void addFile(FileInfo f) throws IOException {
        files.put(f.getFileId(), f);
        //this.write(); // TO REMOVE
    }

    public void addChunk(ChunkInfo c) throws IOException {
        chunks.put(c.getFileId() + c.getChunkNo(), c);  // chunk is identified by (fileId, chunkNo) pair
        //this.write(); // TO REMOVE
    }

    public Map<String, FileInfo> getFiles() {
        return files;
    }

    public Map<String, ChunkInfo> getChunks() {
        return chunks;
    }

    public FileInfo getFile(String fileId) {
        return files.get(fileId);
    }

    public ChunkInfo getChunk(String fileId, int chunkNo) {
        return chunks.get(fileId + chunkNo);
    }

    public static PeerState read(String dir) throws IOException, ClassNotFoundException {
        java.io.File f = new java.io.File(dir + "/" + stateFileName);
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

        java.io.File f = new java.io.File(this.dir);
        if (!f.exists()) f.mkdirs();

        FileOutputStream fout = new FileOutputStream(filePath);
        ObjectOutputStream out = new ObjectOutputStream(fout);
  
        out.writeObject(this);
  
        out.close();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

        if (!chunks.isEmpty()) {
            res.append("I've stored these chunks:\n");
            for (ChunkInfo chunk : chunks.values()) {
                res.append(chunk);
                res.append("\n");
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

        return res.toString();
    }
}
