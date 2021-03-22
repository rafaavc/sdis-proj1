package messages;

import exceptions.ArgsException;
import messages.MessageBuilder.MessageType;

public class MessageFactory {
    private final short versionN, versionM;
    
    public MessageFactory(short versionN, short versionM) {
        this.versionN = versionN;
        this.versionM = versionM;
    }
    
    public MessageFactory(int versionN, int versionM) {
        this.versionN = (short) versionN;
        this.versionM = (short) versionM;
    }

    public byte[] getPutchunkMessage(String senderId, String fileId, int replicationDeg, int chunkNo, byte[] body) throws ArgsException {
        MessageBuilder builder = new MessageBuilder();
        builder
            .addVersion(versionN, versionM)
            .addMessageType(MessageType.PUTCHUNK)
            .addSenderId(senderId)
            .addSenderId(fileId)
            .addChunkNo(String.valueOf(chunkNo))
            .addReplicationDeg((short) replicationDeg)
            .addBody(body);
        return builder.getMessage();
    }
}
