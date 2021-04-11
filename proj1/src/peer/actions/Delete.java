package actions;

import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
import messages.MessageFactory;

public class Delete {
    private final PeerConfiguration configuration;
    private final String fileId;

    public Delete(PeerConfiguration configuration, String fileId) {
        this.configuration = configuration;
        this.fileId = fileId;
    }

    public void execute() {
        configuration.getPeerState().addDeletedFile(fileId);
        this.configuration.getPeerState().deleteFile(fileId);
        
        try {
            byte[] msg = new MessageFactory(new ProtocolVersion(1, 0)).getDeleteMessage(this.configuration.getPeerId(), fileId);
                    
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
