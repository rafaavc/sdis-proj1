package channels.actions;

import messages.Message;

import configuration.PeerConfiguration;

public class RestoreChannelAction extends Action {
    public RestoreChannelAction(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        System.out.println("Received a RestorePacketAction: " + msg);
    }
}
