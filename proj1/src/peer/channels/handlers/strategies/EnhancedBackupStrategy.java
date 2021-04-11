package channels.handlers.strategies;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
import exceptions.ArgsException;
import files.FileManager;
import messages.Message;
import messages.MessageFactory;
import messages.trackers.StoredTracker;
import state.ChunkInfo;

public class EnhancedBackupStrategy extends BackupStrategy {
    private final ScheduledThreadPoolExecutor threadScheduler;

    public EnhancedBackupStrategy(PeerConfiguration configuration) throws ArgsException {
        super(configuration, new MessageFactory(new ProtocolVersion(1, 0))); // the messages are exactly equal to 1.0
        this.threadScheduler = configuration.getThreadScheduler();
    }

    public void backup(Message msg) throws Exception {
        StoredTracker storedTracker = configuration.getStoredTracker();
        storedTracker.resetStoredCount(msg.getFileId(), msg.getChunkNo());

        threadScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try
                {
                    if (storedTracker.getStoredCount(msg.getFileId(), msg.getChunkNo()) >= msg.getReplicationDeg()) return;

                    // check if still has space because in the time interval that passed the peer may have received other backups
                    if (configuration.getPeerState().getMaximumStorage() != -1 && configuration.getPeerState().getStorageAvailable() < msg.getBodySizeKB()) {
                        System.out.println("Not enough space available for backup.");
                        return;
                    }

                    System.out.println("Storing chunk.");
                    configuration.getStoredTracker().addStoredCount(configuration.getPeerState(), msg.getFileId(), msg.getChunkNo(), Integer.parseInt(configuration.getPeerId()));
                    configuration.getPeerState().addChunk(new ChunkInfo(msg.getFileId(), msg.getBodySizeKB(), msg.getChunkNo(), configuration.getStoredTracker().getStoredCount(msg.getFileId(), msg.getChunkNo()), msg.getReplicationDeg()));

                    configuration.getMC().send(messageFactory.getStoredMessage(configuration.getPeerId(), msg.getFileId(), msg.getChunkNo()));

                    FileManager files = new FileManager(configuration.getRootDir());

                    files.writeChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
                
                } 
                catch(Exception e) 
                {
                    System.err.println(e.getMessage());
                }
            }
        }, configuration.getRandomDelay(400, 400), TimeUnit.MILLISECONDS); // this has will be executed after 400 + rand(400) ms, so that during the first 400 ms it received the STORED of the peers who already have the chunk backed up (which called the method below)
    }
    
    public void sendAlreadyHadStored(Message msg) {
        threadScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try
                {
                    configuration.getMC().send(messageFactory.getStoredMessage(configuration.getPeerId(), msg.getFileId(), msg.getChunkNo()));
                } 
                catch(Exception e) 
                {
                    System.err.println(e.getMessage());
                }
            }
        }, configuration.getRandomDelay(400), TimeUnit.MILLISECONDS);
    }
}
