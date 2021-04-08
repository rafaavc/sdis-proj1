package channels.handlers;

import messages.trackers.ChunkTracker;
import messages.Message;
import configuration.PeerConfiguration;

public class RestoreChannelHandler extends Handler {
    public RestoreChannelHandler(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        ChunkTracker chunkTracker = configuration.getChunkTracker();
        
        try {
            switch(msg.getMessageType()) {
                case CHUNK:
                    chunkTracker.addChunkReceived(msg.getFileId(), msg.getChunkNo(), msg.getBody());
                    break;
                default:
                    System.err.println("Received wrong message in RestoreChannelHandler! " + msg);
                    break;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
