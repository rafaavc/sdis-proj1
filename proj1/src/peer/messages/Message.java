package messages;

import messages.MessageBuilder.MessageType;

public class Message {
    private final String version;
    private MessageType messageType;
    private final String senderId, fileId;
    private int chunkNo = -1;
    private short replicationDeg = -1;

    private byte[] body = null;

    public Message(String version, String senderId, String fileId) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
    }

    public Message(String version, MessageType messageType, String senderId, String fileId, int chunkNo) {
        this(version, senderId, fileId);
        this.messageType = messageType;
        this.chunkNo = chunkNo;
    }

    public Message(String version, MessageType messageType, String senderId, String fileId, int chunkNo, int replicationDeg) {
        this(version, messageType, senderId, fileId, chunkNo);
        this.replicationDeg = (short) replicationDeg;
    }

    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setReplicationDeg(short replicationDeg) {
        this.replicationDeg = replicationDeg;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    public String getFileId() {
        return fileId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getVersion() {
        return version;
    }

    public int getChunkNo() throws Exception {
        if (this.chunkNo < 0) throw new Exception("Trying to access chunkNo of message without this field.");
        return chunkNo;
    }

    public short getReplicationDeg() throws Exception {
        if (this.replicationDeg < 0) throw new Exception("Trying to access chunkNo of message without this field.");
        return replicationDeg;
    }

    @Override
    public String toString() {
        return "Message: " + version + " "+ messageType+ " "+ senderId + " "+ fileId + " "+ chunkNo;
    }
}
