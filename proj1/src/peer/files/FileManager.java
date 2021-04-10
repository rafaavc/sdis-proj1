package files;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import exceptions.ArgsException;
import exceptions.ArgsException.Type;

public class FileManager {
    private final String rootDir;

    public FileManager(String rootDir) {
        this.rootDir = rootDir;
        this.createDir(rootDir);
    }
    
    public FileManager() {
        this.rootDir = ".";
    }

    public void createDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();
    }

    public void writeFile(String fileName, List<byte[]> chunks) throws IOException {
        FileOutputStream out = new FileOutputStream(this.rootDir + "/" + fileName);
        for (byte[] chunk : chunks) out.write(chunk);
        out.close();
    }

    public void write(String file, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(this.rootDir + "/" + file);
        out.write(data);
        out.close();
    }

    public void writeChunk(String fileId, int chunkNo, byte[] data) throws IOException {
        String dir = this.rootDir + "/" + fileId;
        this.createDir(dir);

        write(fileId + "/" + chunkNo, data);
    }

    public void deleteChunk(String fileId, int chunkNo) throws IOException {
        File f = new File(this.rootDir + "/" + fileId + "/" + chunkNo);
        f.delete();
        
        File dir = new File(this.rootDir + "/" + fileId);
        if (dir.listFiles().length == 0) dir.delete();
    }

    public byte[] read(String file) throws IOException, ArgsException {
        String path = this.rootDir + "/" + file;
        
        File f = new File(path);
        if (!f.exists()) throw new ArgsException(Type.FILE_DOESNT_EXIST, path);

        FileInputStream in = new FileInputStream(path);
        byte[] data = in.readAllBytes();
        in.close();
        return data;
    }

    public byte[] readChunk(String fileId, int chunkNo) throws IOException, ArgsException {
        String dir = this.rootDir + "/" + fileId;
        this.createDir(dir);

        return read(fileId + "/" + chunkNo);
    }

    public void deleteFileChunks(String fileId) {
        File fileFolder = new File(this.rootDir + "/" + fileId);
        if (!fileFolder.exists()) return;

        for (File chunk : fileFolder.listFiles()) chunk.delete();

        fileFolder.delete();
    }
}
