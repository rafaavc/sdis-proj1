package actions;

import configuration.PeerConfiguration;
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
                
                // TODO improve
                this.configuration.getMC().send(msg);
                Thread.sleep(500);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
