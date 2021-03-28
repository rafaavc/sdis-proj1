package actions;

import configuration.PeerConfiguration;

public class Delete extends Thread {
    private final PeerConfiguration configuration;
    private final String fileId;

    public Delete(PeerConfiguration configuration, String fileId) {
        this.configuration = configuration;
        this.fileId = fileId;
    }

    @Override
    public void run() {
        this.configuration.getState().deleteFile(this.fileId);
        try {
            byte[] msg = this.configuration.getMessageFactory().getDeleteMessage(this.configuration.getPeerId(), fileId);
                    
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
