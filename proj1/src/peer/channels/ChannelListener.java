package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;

import channels.actions.Action;

public class ChannelListener extends Thread {
    private final MulticastChannel channel;
    private final Action action;

    public ChannelListener(MulticastChannel channel, Action action) {
        this.channel = channel;
        this.action = action;
    }

    @Override
    public void run() {
        MulticastSocket socket = channel.getSocket();
        
        try {
            socket.joinGroup(channel.getHost());

            while (true) {
                byte[] rbuf = new byte[65000];
                DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

                socket.receive(packet);

                this.action.execute(packet);
            }
        } catch (IOException e) {
            if (!(e instanceof SocketException)) {
                System.err.println("IOException in ChannelListener of channel " + this.channel + ": " + e.getMessage());
            }
        }
    }
}
