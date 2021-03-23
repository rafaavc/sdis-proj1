package state;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import state.FileInfo;
import configuration.PeerConfiguration;


public class PeerState implements Serializable {

    private static String stateFileName = "metadata";
    private final String dir;

    private List<FileInfo> files = new ArrayList<>();
    private List<String> chunks = new ArrayList<>();

    public PeerState(String dir) {
        this.dir = dir;
    }

    public void addFile(FileInfo f) throws IOException {
        files.add(f);
        this.write(); // TO REMOVE
    }

    public List<FileInfo> getFiles() {
        return files;
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
}
