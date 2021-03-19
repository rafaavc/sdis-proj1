package actions;

import java.net.DatagramPacket;

public class ControlPacketAction implements Action {
    public void execute(DatagramPacket packet) {
        System.out.println("Received a ControlPacketAction! " + new String(packet.getData()));
    }
}
