package channels.handlers.strategies;

import java.util.Random;

import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
import exceptions.ArgsException;
import files.FileManager;
import messages.Message;
import messages.MessageFactory;
import messages.trackers.StoredTracker;
import state.ChunkInfo;

public class EnhancedBackupStrategy extends BackupStrategy {
    public EnhancedBackupStrategy(PeerConfiguration configuration) throws ArgsException {
        super(configuration, new MessageFactory(new ProtocolVersion(1, 0))); // the messages are exactly equal to 1.0
    }

    public void backup(Message msg) throws Exception {
        StoredTracker storedTracker = configuration.getStoredTracker();
        storedTracker.resetStoredCount(msg.getFileId(), msg.getChunkNo());

        Thread.sleep(new Random().nextInt(400) + 400);  // this is so that it receives the STORED from the ones who already had the file first
        if (storedTracker.getStoredCount(msg.getFileId(), msg.getChunkNo()) >= msg.getReplicationDeg()) return;

        // check if still has space because in the time interval that passed the peer may have received other backups
        if (configuration.getPeerState().getMaximumStorage() != -1 && configuration.getPeerState().getStorageAvailable() < msg.getBodySizeKB()) {
            System.out.println("Not enough space available for backup.");
            return;
        }

        System.out.println("Storing chunk.");
        configuration.getStoredTracker().addStoredCount(configuration.getPeerState(), msg.getFileId(), msg.getChunkNo(), Integer.parseInt(this.configuration.getPeerId()));
        configuration.getPeerState().addChunk(new ChunkInfo(msg.getFileId(), msg.getBodySizeKB(), msg.getChunkNo(), configuration.getStoredTracker().getStoredCount(msg.getFileId(), msg.getChunkNo()), msg.getReplicationDeg()));

        this.configuration.getMC().send(messageFactory.getStoredMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo()));

        FileManager files = new FileManager(configuration.getRootDir());

        files.writeChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
    }
    
    public void sendAlreadyHadStored(Message msg) throws Exception {
        Thread.sleep(new Random().nextInt(400));
        this.configuration.getMC().send(messageFactory.getStoredMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo()));
    }
}
