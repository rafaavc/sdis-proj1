package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;

import channels.handlers.Handler;
import messages.Message;
import messages.MessageParser;

public class ChannelListener extends Thread {
    private final MulticastChannel channel;
    private final Handler action;

    public ChannelListener(MulticastChannel channel, Handler action) {
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

                        Message msg = MessageParser.parse(data, packet.getLength());
        
                        if (msg.getSenderId().equals(action.getConfiguration().getPeerId())) return;
                        action.execute(msg);
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
