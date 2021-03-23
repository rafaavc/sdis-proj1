package channels.actions;

import messages.Message;

import configuration.PeerConfiguration;

public class ControlChannelAction extends Action {
    public ControlChannelAction(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        // Replies to putchunk
        System.out.println("Received a ControlPacketAction: " + msg);
    }
}
