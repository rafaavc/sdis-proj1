package actions;

import java.net.DatagramPacket;

public class BackupPacketAction implements Action {
    public void execute(DatagramPacket packet) {
        System.out.println("Received a BackupPacketAction: " + new String(packet.getData()));
    }
}
