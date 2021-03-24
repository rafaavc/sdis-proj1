package files;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {
    private final String rootDir;
    public FileManager(String rootDir) {
        this.rootDir = rootDir;

        java.io.File dir = new java.io.File(rootDir);
        if (!dir.exists()) dir.mkdirs();
    }

    public void write(String file, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(this.rootDir + "/" + file);
        out.write(data);
        out.close();
    }

    public byte[] read(String file) throws IOException {
        FileInputStream in = new FileInputStream(this.rootDir + "/" + file);
        byte[] data = in.readAllBytes();
        in.close();
        return data;
    }
}
