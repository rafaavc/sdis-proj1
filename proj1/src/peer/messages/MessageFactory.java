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

    public byte[] getPutchunkMessage(String senderId, String fileId, int replicationDeg, byte[] body) throws ArgsException {
        MessageBuilder builder = new MessageBuilder();
        builder.addVersion(versionN, versionM);
        builder.addMessageType(MessageType.PUTCHUNK);
        builder.addSenderId(senderId);
        builder.addChunkNo(fileId);
        builder.addReplicationDeg((short) replicationDeg);
        builder.addBody(body);
        return builder.getMessage();
    }
}
