package files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import exceptions.ArgsException;
import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;
import exceptions.ArgsException.Type;

public class File {
    private final String fileId;
    private final byte[] data;
    private final List<Chunk> chunks;

    public static String getFileId(java.io.File file) throws IOException, NoSuchAlgorithmException {
        BasicFileAttributes attr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);

        String original = file.getPath() + attr.lastModifiedTime() + attr.creationTime() + attr.size();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(original.getBytes());

        StringBuilder idBuilder = new StringBuilder();
        for (byte b : hash) {
            idBuilder.append(String.format("%02x", b));
        }

        return idBuilder.toString();
    }

    public File(String path) throws IOException, ChunkSizeExceeded, InvalidChunkNo, ArgsException, NoSuchAlgorithmException {
        java.io.File f = new java.io.File(path);

        if (!f.exists()) throw new ArgsException(Type.FILE_DOESNT_EXIST, path);

        this.data = FileManager.read(path);

        this.fileId = File.getFileId(f);

        this.chunks = Chunk.getChunks(fileId, data);
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public String getFileId() {
        return fileId;
    }    
}
