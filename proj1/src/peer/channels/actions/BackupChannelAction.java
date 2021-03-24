package channels.actions;

import messages.Message;
import state.ChunkInfo;

import java.util.Random;

import configuration.PeerConfiguration;
import files.FileManager;

public class BackupChannelAction extends Action {
    public BackupChannelAction(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        switch(msg.getMessageType()) { 
            case PUTCHUNK:
                try {

                    System.out.println("Storing chunk.");
                    FileManager files = new FileManager(this.configuration.getRootDir());

                    files.write(msg.getFileId() + msg.getChunkNo(), msg.getBody());
                    this.configuration.getState().addChunk(new ChunkInfo(msg.getFileId(), msg.getChunkNo(), 0, msg.getReplicationDeg()));  // TODO: PERCEIVED

                    Thread.sleep(new Random().nextInt(400));
                    this.configuration.getMC().send(this.configuration.getMessageFactory().getStoredMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo()));
                    
                } catch (Exception e) {

                    System.err.println(e.getMessage());
                    e.printStackTrace();

                }
                break;
            default:
                System.err.println("Received wrong message in BackupChannelAction! " + msg);
                break;
        }
    }
}
