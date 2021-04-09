package channels.handlers;

import messages.Message;
import messages.trackers.PutchunkTracker;

import channels.handlers.strategies.BackupStrategy;
import configuration.PeerConfiguration;

public class BackupChannelHandler extends Handler {
    private final PutchunkTracker putchunkTracker;
    private final BackupStrategy backupStrategy;

    public BackupChannelHandler(PeerConfiguration configuration, BackupStrategy backupStrategy) {
        super(configuration);
        this.putchunkTracker = configuration.getPutchunkTracker();
        this.backupStrategy = backupStrategy;
    }

    public void execute(Message msg) {
        switch(msg.getMessageType()) {
            case PUTCHUNK:
                try 
                {
                    putchunkTracker.addPutchunkReceived(msg.getFileId(), msg.getChunkNo());
                    if (peerState.hasChunk(msg.getFileId(), msg.getChunkNo())) {
                        System.out.println("Already had chunk!");
                        backupStrategy.sendAlreadyHadStored(msg);
                        break;
                    } else if (peerState.ownsFileWithId(msg.getFileId())) {
                        System.out.println("I am the file owner!");
                        break;
                    } else if (peerState.getMaximumStorage() != -1 && peerState.getStorageAvailable() < (msg.getBody().length / 1000.)) {
                        System.out.println("Not enough space available for backup.");
                        break;
                    }

                    backupStrategy.backup(msg);
                } 
                catch (Exception e) 
                {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
                break;
            default:
                System.err.println("Received wrong message in BackupChannelHandler! " + msg);
                break;
        }
    }
}
