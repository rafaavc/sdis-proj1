package actions;

import java.net.DatagramPacket;

public interface Action {
    public void execute(DatagramPacket packet);
}
