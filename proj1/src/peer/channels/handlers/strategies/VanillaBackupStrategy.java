package channels.handlers.strategies;

import java.util.Random;

import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
import exceptions.ArgsException;
import files.FileManager;
import messages.Message;
import messages.MessageFactory;
import state.ChunkInfo;

public class VanillaBackupStrategy extends BackupStrategy {
    public VanillaBackupStrategy(PeerConfiguration configuration) throws ArgsException {
        super(configuration, new MessageFactory(new ProtocolVersion(1, 0)));
    }

    public void backup(Message msg) throws Exception {
        FileManager files = new FileManager(configuration.getRootDir());
        System.out.println("Storing chunk.");

        files.writeChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
        configuration.getPeerState().addChunk(new ChunkInfo(msg.getFileId(), (float)(msg.getBody().length / 1000.), msg.getChunkNo(), configuration.getStoredTracker().getStoredCount(msg.getFileId(), msg.getChunkNo()), msg.getReplicationDeg()));

        configuration.getStoredTracker().addStoredCount(configuration.getPeerState(), msg.getFileId(), msg.getChunkNo(), Integer.parseInt(this.configuration.getPeerId()));

        sendStored(msg);
    }

    public void sendAlreadyHadStored(Message msg) throws Exception {
        sendStored(msg);
    }

    private void sendStored(Message msg) throws Exception {
        Thread.sleep(new Random().nextInt(400));
        this.configuration.getMC().send(messageFactory.getStoredMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo()));
    }
}
