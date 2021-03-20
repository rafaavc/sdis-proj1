package channels.actions;

import java.net.DatagramPacket;

import configuration.PeerConfiguration;

public class RestoreChannelAction extends Action {
    public RestoreChannelAction(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(DatagramPacket packet) {
        System.out.println("Received a RestorePacketAction: " + new String(packet.getData()));
    }
}
