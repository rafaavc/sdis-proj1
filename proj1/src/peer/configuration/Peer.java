package configuration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import channels.ChannelListener;
import channels.MulticastChannel;
import channels.actions.Action;
import exceptions.ChunkSizeExceeded;
import exceptions.InvalidChunkNo;

public class Peer extends UnicastRemoteObject implements ClientInterface {
    private static final long serialVersionUID = 5157944159616018684L;
    private final PeerConfiguration configuration;

    public Peer(PeerConfiguration configuration) throws IOException, ChunkSizeExceeded, InvalidChunkNo {
        this.configuration = configuration;

        for (MulticastChannel channel : this.configuration.getChannels()) {
            new ChannelListener(channel, Action.get(this.configuration, channel.getType())).start();
        }

        System.out.println("Ready!");
    }

    public void hi() throws RemoteException {
        System.out.println("Hi");
    }

    public void testMulticast() throws RemoteException {
        try {
            this.configuration.getMC().send("Hi");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
