import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    final String directory;

    public FileManager(String directory) {
        this.directory = directory;
    }

    public void write(String file, List<Byte> data) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
  
        try {
            for (Byte b : data) {
                out.write(b);
            }
        } finally {
            out.close();
        }
    }

    public List<Byte> read(String file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        List<Byte> data = new ArrayList<Byte>();

        try {
            int b;
            while ((b = in.read()) != -1) {
                data.add((byte) b);
            }
        } finally {
            in.close();
        }

        return data;
    }
}
