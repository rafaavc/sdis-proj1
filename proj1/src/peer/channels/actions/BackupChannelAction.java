package channels.actions;

import java.net.DatagramPacket;

import configuration.PeerConfiguration;

public class BackupChannelAction extends Action {
    public BackupChannelAction(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(DatagramPacket packet) {
        System.out.println("Received a BackupPacketAction: " + new String(packet.getData()).trim());
    }
}
