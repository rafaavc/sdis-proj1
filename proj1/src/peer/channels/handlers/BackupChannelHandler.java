package channels.handlers;

import messages.Message;
import messages.PutchunkTracker;
import messages.StoredTracker;
import state.ChunkInfo;
import state.PeerState;

import java.util.Random;

import configuration.PeerConfiguration;
import files.FileManager;

public class BackupChannelHandler extends Handler {
    public BackupChannelHandler(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        PutchunkTracker putchunkTracker = configuration.getPutchunkTracker();
        StoredTracker storedTracker = configuration.getStoredTracker();
        PeerState state = configuration.getPeerState();

        switch(msg.getMessageType()) {
            case PUTCHUNK:
                try {
                    putchunkTracker.addPutchunkReceived(msg.getFileId(), msg.getChunkNo());
                    if (state.hasChunk(msg.getFileId(), msg.getChunkNo())) {
                        System.out.println("Already had chunk!");
                        Thread.sleep(new Random().nextInt(400));
                        this.configuration.getMC().send(this.configuration.getMessageFactory().getStoredMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo()));
                        break;
                    } else if (state.ownsFile(msg.getFileId())) {
                        System.out.println("I am the file owner!");
                        break;
                    } else if (state.getMaximumStorage() != -1 && state.getStorageAvailable() < (msg.getBody().length / 1000.)) {
                        System.out.println("Not enough space available for backup.");
                        break;
                    }

                    System.out.println("Storing chunk.");
                    FileManager files = new FileManager(this.configuration.getRootDir());

                    files.writeChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
                    state.addChunk(new ChunkInfo(msg.getFileId(), (float)(msg.getBody().length / 1000.), msg.getChunkNo(), storedTracker.getStoredCount(msg.getFileId(), msg.getChunkNo()), msg.getReplicationDeg()));  // TODO: PERCEIVED

                    storedTracker.addStoredCount(state, msg.getFileId(), msg.getChunkNo(), Integer.parseInt(this.configuration.getPeerId()));

                    Thread.sleep(new Random().nextInt(400));
                    this.configuration.getMC().send(this.configuration.getMessageFactory().getStoredMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo()));
                    
                } catch (Exception e) {

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
