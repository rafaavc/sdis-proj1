import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;

public class Peer {
    public static void main(String[] args) throws RemoteException, NotBoundException, IOException, ChunkSizeExceeded, InvalidChunkNo {
        // if (System.getenv("BACKUP_SERVICE_ENV").equals("docker")) {
        //     Registry registry = LocateRegistry.getRegistry("rmi");
        //     System.out.println("I am a docker container!\n" + registry);
        // }
        
        FileManager fileManager = new FileManager("test");
        List<Byte> data = fileManager.read("testFile");
        System.out.println(Chunk.getChunks("id", data));
    }
}
