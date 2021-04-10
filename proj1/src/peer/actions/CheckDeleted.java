package actions;

import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
import messages.MessageFactory;

public class CheckDeleted extends Thread {
    private final PeerConfiguration configuration;

    public CheckDeleted(PeerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void run() {
        try {
            for (String fileId : configuration.getPeerState().getBackedUpFileIds())
            {
                byte[] msg = new MessageFactory(new ProtocolVersion(1, 1)).getFilecheckMessage(configuration.getPeerId(), fileId);
                configuration.getMC().send(msg);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
