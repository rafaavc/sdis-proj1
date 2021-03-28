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
            .addFileId(fileId)
            .addChunkNo(String.valueOf(chunkNo))
            .addReplicationDeg((short) replicationDeg)
            .addBody(body);
        return builder.getMessage();
    }

    public byte[] getStoredMessage(String senderId, String fileId, int chunkNo) throws ArgsException {
        MessageBuilder builder = new MessageBuilder();
        builder
            .addVersion(versionN, versionM)
            .addMessageType(MessageType.STORED)
            .addSenderId(senderId)
            .addFileId(fileId)
            .addChunkNo(String.valueOf(chunkNo));
        return builder.getMessage();
    }

    public byte[] getDeleteMessage(String senderId, String fileId) throws ArgsException {
        MessageBuilder builder = new MessageBuilder();
        builder
            .addVersion(versionN, versionM)
            .addMessageType(MessageType.DELETE)
            .addSenderId(senderId)
            .addFileId(fileId);
        return builder.getMessage();
    }

    public byte[] getGetchunkMessage(String senderId, String fileId, int chunkNo) throws ArgsException {
        MessageBuilder builder = new MessageBuilder();
        builder
            .addVersion(versionN, versionM)
            .addMessageType(MessageType.GETCHUNK)
            .addSenderId(senderId)
            .addFileId(fileId)
            .addChunkNo(String.valueOf(chunkNo));
        return builder.getMessage();
    }

    public byte[] getChunkMessage(String senderId, String fileId, int chunkNo, byte[] body) throws ArgsException {
        MessageBuilder builder = new MessageBuilder();
        builder
            .addVersion(versionN, versionM)
            .addMessageType(MessageType.GETCHUNK)
            .addSenderId(senderId)
            .addFileId(fileId)
            .addChunkNo(String.valueOf(chunkNo))
            .addBody(body);
        return builder.getMessage();
    }
}
