package channels.actions;

import java.net.DatagramPacket;

import configuration.PeerConfiguration;

public class ControlChannelAction extends Action {
    public ControlChannelAction(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(DatagramPacket packet) {
        System.out.println("Received a ControlPacketAction: " + new String(packet.getData()));
    }
}
