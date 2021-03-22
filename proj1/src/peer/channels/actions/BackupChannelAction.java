package channels.actions;

import java.net.DatagramPacket;
import messages.MessageParser;
import messages.Message;

import configuration.PeerConfiguration;

public class BackupChannelAction extends Action {
    public BackupChannelAction(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(DatagramPacket packet) {
        byte[] data = packet.getData();
        Message msg = MessageParser.parse(data);

        System.out.println("Received msg: " + new String(msg.getBody()));
    }
}
