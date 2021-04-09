package actions;

import configuration.PeerConfiguration;
import messages.MessageFactory;

public class Delete extends Thread {
    private final PeerConfiguration configuration;
    private final String fileId;
    private boolean deleteFromState = true;

    public Delete(PeerConfiguration configuration, String fileId) {
        this.configuration = configuration;
        this.fileId = fileId;
    }

    public Delete(PeerConfiguration configuration, String fileId, boolean deleteFromState) {
        this(configuration, fileId);
        this.deleteFromState = deleteFromState;
    }

    @Override
    public void run() {
        configuration.getPeerState().addDeletedFile(fileId);
        if (deleteFromState) this.configuration.getPeerState().deleteFile(fileId);
        try {
            byte[] msg = new MessageFactory(1, 0).getDeleteMessage(this.configuration.getPeerId(), fileId);
                    
            int count = 0;
            while(count < 5) {
                this.configuration.getMC().send(msg);
                Thread.sleep(500);
                count++;
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
