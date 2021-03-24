package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import messages.Message;
import messages.MessageParser;
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

                new Thread() {
                    @Override
                    public void run() {
                        byte[] data = packet.getData();
                        // int i = data.length - 1;
                        // for (; i >= 0; i--) {
                        //     if (data[i] != '\0') {
                        //         System.out.println("Found data, i = " + i);
                        //         break;
                        //     }
                        // }

                        System.out.println("Packet length: " + packet.getLength());
                        Message msg = MessageParser.parse(data, packet.getLength());
        
                        if (msg.getSenderId().equals(action.getConfiguration().getPeerId())) System.out.println("Received own message");
                        else {
                            action.execute(msg);
                        }
                    }
                }.start();
            }
        } catch (IOException e) {
            if (!(e instanceof SocketException)) {
                System.err.println("IOException in ChannelListener of channel " + this.channel + ": " + e.getMessage());
            }
        }
    }
}
