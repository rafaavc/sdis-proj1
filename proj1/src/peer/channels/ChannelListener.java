package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import channels.handlers.Handler;
import exceptions.ArgsException;
import messages.Message;
import messages.MessageParser;

public class ChannelListener extends Thread {
    private final MulticastChannel channel;
    private final Handler action;
    private final ScheduledThreadPoolExecutor threadScheduler;

    public ChannelListener(MulticastChannel channel, Handler action, ScheduledThreadPoolExecutor threadScheduler) {
        this.channel = channel;
        this.action = action;
        this.threadScheduler = threadScheduler;
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
                threadScheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        byte[] data = packet.getData();

                        try 
                        {
                            Message msg = MessageParser.parse(data, packet.getLength());
        
                            if (msg.getSenderId().equals(action.getConfiguration().getPeerId())) return;
                            action.execute(msg, packet.getAddress());
                        } 
                        catch(ArgsException e) 
                        {
                            System.err.println(e.getMessage());
                        }
                    }
                }, 0, TimeUnit.MILLISECONDS);
            }
        } catch (IOException e) {
            if (!(e instanceof SocketException)) {
                System.err.println("IOException in ChannelListener of channel " + this.channel + ": " + e.getMessage());
            }
        }
    }
}
