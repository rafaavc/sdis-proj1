package channels.handlers;

import messages.Message;

import java.util.Random;

import configuration.PeerConfiguration;
import files.FileManager;

public class ControlChannelHandler extends Handler {
    public ControlChannelHandler(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        FileManager fileManager = new FileManager(this.configuration.getPeerId());
        try {
            switch(msg.getMessageType()) { 
                case STORED:
                    this.configuration.addStoredCount(msg.getFileId(), msg.getChunkNo(), Integer.parseInt(msg.getSenderId())); // TODO change peer id type to int
                    break;
                case DELETE:
                    this.configuration.getPeerState().deleteFileChunks(msg.getFileId());
                    fileManager.deleteFileChunks(msg.getFileId());
                    break;
                case GETCHUNK:
                    if (this.configuration.getPeerState().hasChunk(msg.getFileId(), msg.getChunkNo())) {
                        Thread.sleep(new Random().nextInt(400));
                        // TODO if chunk message received: break;
                        // else: send chunk
                        byte[] chunkData = fileManager.readChunk(msg.getFileId(), msg.getChunkNo());
                        byte[] chunkMsg = this.configuration.getMessageFactory().getChunkMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo(), chunkData);
                        
                        this.configuration.getMDR().send(chunkMsg);
                    }
                    break;
                default:
                    System.err.println("Received wrong message in BackupChannelHandler! " + msg);
                    break;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
