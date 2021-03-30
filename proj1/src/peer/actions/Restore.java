package actions;



import java.util.List;

import configuration.PeerConfiguration;
import files.FileManager;
import state.ChunkPair;
import state.FileInfo;

public class Restore extends Thread {
    private final PeerConfiguration configuration;
    private final FileInfo file;

    public Restore(PeerConfiguration configuration, FileInfo file) {
        this.configuration = configuration;
        this.file = file;
    }

    @Override
    public void run() {
        try {
            for (ChunkPair chunk : file.getChunks()) {
                byte[] msg = this.configuration.getMessageFactory().getGetchunkMessage(this.configuration.getPeerId(), file.getFileId(), chunk.getChunkNo());

                this.configuration.startWaitingForChunk(file.getFileId(), chunk.getChunkNo());

                int count = 0, sleepAmount = 1000;
                while (count < 5 && !this.configuration.hasReceivedChunkData(file.getFileId(), chunk.getChunkNo())) {
                    this.configuration.getMC().send(msg);
                    Thread.sleep(sleepAmount);
                    sleepAmount *= 2;
                    count++;
                }
            }

            List<byte[]> chunks = this.configuration.getFileChunks(file.getFileId());
            System.out.println("Received " + chunks.size() + "/" + file.getChunks().size() + " chunks.");

            FileManager fileManager = new FileManager(this.configuration.getRootDir());
            fileManager.writeFile(file.getFileName(), chunks);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
