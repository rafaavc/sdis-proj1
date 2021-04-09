package messages;

import exceptions.ArgsException;
import exceptions.ArgsException.Type;
import messages.Message.MessageType;

public class MessageFactory {
    private final String version;
    
    public MessageFactory(short versionN, short versionM) throws ArgsException {
        if (versionN < 0 || versionN > 9 || versionM < 0 || versionM > 9 || (versionN == 0 && versionM == 0)) throw new ArgsException(Type.VERSION_NO);
        StringBuilder builder = new StringBuilder();
        builder.append(versionN);
        builder.append('.');
        builder.append(versionM);
        this.version = builder.toString();
    }
    
    public MessageFactory(int versionN, int versionM) throws ArgsException {
        this((short) versionN, (short) versionM);
    }
    
    public MessageFactory(String version) {
        // TODO validate
        this.version = version;
    }

    public byte[] getPutchunkMessage(String senderId, String fileId, int replicationDeg, int chunkNo, byte[] body) throws ArgsException {
        Message msg = new Message(version, 
                                    MessageType.PUTCHUNK,
                                    senderId,
                                    fileId,
                                    chunkNo,
                                    replicationDeg,
                                    body);
        return msg.getBytes();
    }

    public byte[] getStoredMessage(String senderId, String fileId, int chunkNo) throws ArgsException {
        Message msg = new Message(version, 
                                    MessageType.STORED,
                                    senderId,
                                    fileId,
                                    chunkNo);
        return msg.getBytes();
    }

    public byte[] getDeleteMessage(String senderId, String fileId) throws ArgsException {
        Message msg = new Message(version, 
                                    MessageType.DELETE,
                                    senderId,
                                    fileId);
        return msg.getBytes();
    }

    public byte[] getGetchunkMessage(String senderId, String fileId, int chunkNo) throws ArgsException {
        Message msg = new Message(version, 
                                    MessageType.GETCHUNK,
                                    senderId,
                                    fileId,
                                    chunkNo);
        return msg.getBytes();
    }

    public byte[] getChunkMessage(String senderId, String fileId, int chunkNo, byte[] body) throws ArgsException {
        Message msg = new Message(version, 
                                    MessageType.CHUNK,
                                    senderId,
                                    fileId,
                                    chunkNo,
                                    body);
        return msg.getBytes();
    }

    public byte[] getRemovedMessage(String senderId, String fileId, int chunkNo) throws ArgsException {
        Message msg = new Message(version, 
                                    MessageType.REMOVED,
                                    senderId,
                                    fileId,
                                    chunkNo);
        return msg.getBytes();
    }

    public byte[] getFilecheckMessage(String senderId, String fileId) throws ArgsException {
        Message msg = new Message(version, 
                                    MessageType.FILECHECK,
                                    senderId,
                                    fileId);
        return msg.getBytes();
    }
}
