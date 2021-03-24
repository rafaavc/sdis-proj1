package channels.actions;

import messages.Message;

import configuration.PeerConfiguration;

public class ControlChannelAction extends Action {
    public ControlChannelAction(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        try {
            switch(msg.getMessageType()) { 
                case STORED:
                    this.configuration.addStoredCount(msg.getFileId(), msg.getChunkNo(), Integer.parseInt(this.configuration.getPeerId())); // TODO change peer id type to int
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
