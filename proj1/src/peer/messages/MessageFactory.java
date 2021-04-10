package messages;

import configuration.ProtocolVersion;
import exceptions.ArgsException;
import messages.Message.MessageType;

public class MessageFactory {
    private final ProtocolVersion version;
    
    public MessageFactory(ProtocolVersion version) throws ArgsException {
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
