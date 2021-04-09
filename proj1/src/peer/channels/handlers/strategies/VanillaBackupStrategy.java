package channels.handlers.strategies;

import java.io.IOException;
import java.util.Random;

import configuration.PeerConfiguration;
import exceptions.ArgsException;
import files.FileManager;
import messages.Message;
import state.ChunkInfo;

public class VanillaBackupStrategy extends BackupStrategy {
    public VanillaBackupStrategy(PeerConfiguration configuration) {
        super(configuration);
    }

    public void backup(Message msg) throws IOException, ArgsException, Exception {
        FileManager files = new FileManager(configuration.getRootDir());
        System.out.println("Storing chunk.");

        files.writeChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
        configuration.getPeerState().addChunk(new ChunkInfo(msg.getFileId(), (float)(msg.getBody().length / 1000.), msg.getChunkNo(), configuration.getStoredTracker().getStoredCount(msg.getFileId(), msg.getChunkNo()), msg.getReplicationDeg()));

        configuration.getStoredTracker().addStoredCount(configuration.getPeerState(), msg.getFileId(), msg.getChunkNo(), Integer.parseInt(this.configuration.getPeerId()));

        sendStored(msg);
    }

    public void sendAlreadyHadStored(Message msg) throws IOException, ArgsException, Exception {
        sendStored(msg);
    }

    private void sendStored(Message msg) throws IOException, ArgsException, Exception{
        Thread.sleep(new Random().nextInt(400));
        this.configuration.getMC().send(this.configuration.getMessageFactory().getStoredMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo()));
    }
}
