package actions;

import java.net.DatagramPacket;

public class RestorePacketAction implements Action {
    public void execute(DatagramPacket packet) {
        System.out.println("Received a RestorePacketAction! " + packet.getData().toString());
    }
}
