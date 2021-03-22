package files;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {
    public static void write(String file, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.close();
    }

    public static byte[] read(String file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] data = in.readAllBytes();
        in.close();
        return data;
    }
}
