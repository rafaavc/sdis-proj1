package channels.handlers;

import messages.Message;

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
                    this.configuration.getState().deleteFileChunks(msg.getFileId());
                    fileManager.deleteFileChunks(msg.getFileId());
                    break;
                default:
                    System.err.println("Received wrong message in BackupChannelAction! " + msg);
                    break;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
