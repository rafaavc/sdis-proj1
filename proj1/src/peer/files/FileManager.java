package files;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {
    private final String rootDir;
    public FileManager(String rootDir) {
        this.rootDir = rootDir;
        this.createDir(rootDir);
    }

    public void createDir(String path) {
        java.io.File dir = new java.io.File(path);
        if (!dir.exists()) dir.mkdirs();
    }

    public void write(String file, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(this.rootDir + "/" + file);
        out.write(data);
        out.close();
    }

    public void writeChunk(String fileId, String fileName /* Probably the chunkNo */, byte[] data) throws IOException {
        String dir = this.rootDir + "/" + fileId;
        this.createDir(dir);

        FileOutputStream out = new FileOutputStream(dir + "/" + fileName);
        out.write(data);
        out.close();
    }

    public void deleteFileChunks(String fileId) {
        java.io.File fileFolder = new java.io.File(this.rootDir + "/" + fileId);
        if (!fileFolder.exists()) return;

        for (java.io.File chunk : fileFolder.listFiles()) chunk.delete();

        fileFolder.delete();
    }

    public byte[] read(String file) throws IOException {
        FileInputStream in = new FileInputStream(this.rootDir + "/" + file);
        byte[] data = in.readAllBytes();
        in.close();
        return data;
    }
}
