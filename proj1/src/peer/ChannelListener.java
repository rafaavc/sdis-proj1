import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class ChannelListener extends Thread {
    private final MulticastChannel channel;

    public ChannelListener(MulticastChannel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        System.out.println("Listening on channel " + channel.toString());
        try {
            MulticastSocket socket = (MulticastSocket) new MulticastSocket(channel.getPort());
            socket.joinGroup(channel.getHost());

            while (true) {
                byte[] rbuf = new byte[100];
                DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

                socket.receive(packet);

                this.channel.getAction().execute(packet);
            } 

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
