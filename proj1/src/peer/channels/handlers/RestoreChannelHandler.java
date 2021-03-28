package channels.handlers;

import messages.Message;

import configuration.PeerConfiguration;

public class RestoreChannelHandler extends Handler {
    public RestoreChannelHandler(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        System.out.println("Received a RestorePacketAction: " + msg);
    }
}
