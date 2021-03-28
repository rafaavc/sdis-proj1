package messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import exceptions.ArgsException;
import exceptions.ArgsException.Type;

public class MessageBuilder {
    private final String CRLF = new String(new byte[] { 0xD, 0xA });
    private final List<String> header = new ArrayList<>();
    private byte[] body = null;

    public static enum MessageType {
        PUTCHUNK,
        STORED,
        GETCHUNK,
        CHUNK,
        DELETE,
        REMOVED
    }

    public static final HashMap<MessageType, String> messages = new HashMap<>();

    static {
        messages.put(MessageType.PUTCHUNK, "PUTCHUNK");
        messages.put(MessageType.STORED, "STORED");
        messages.put(MessageType.GETCHUNK, "GETCHUNK");
        messages.put(MessageType.CHUNK, "CHUNK");
        messages.put(MessageType.DELETE, "DELETE");
        messages.put(MessageType.REMOVED, "REMOVED");
    }


    public MessageBuilder addVersion(short n, short m) throws ArgsException {
        if (n < 0 || n > 9 || m < 0 || m > 9 || (n == 0 && m == 0)) throw new ArgsException(Type.VERSION_NO);
        StringBuilder builder = new StringBuilder();
        builder.append(n);
        builder.append('.');
        builder.append(m);
        header.add(builder.toString());
        return this;
    }

    public MessageBuilder addMessageType(MessageType type) {
        header.add(messages.get(type));
        return this;
    }

    public MessageBuilder addSenderId(String senderId) {
        header.add(senderId);
        return this;
    }

    // TODO
    public MessageBuilder addFileId(String fileId) {
        header.add(fileId);
        return this;
    }

    public MessageBuilder addChunkNo(String chunkNo) throws ArgsException {
        if (chunkNo.length() > 6) throw new ArgsException(Type.CHUNK_NO);
        header.add(chunkNo);
        return this;
    }

    public MessageBuilder addReplicationDeg(short replicationDeg) throws ArgsException {
        if (replicationDeg > 9 || replicationDeg < 0) throw new ArgsException(Type.REPLICATION_DEG);
        header.add(String.valueOf(replicationDeg));
        return this;
    }

    public MessageBuilder addHeaderCRLF() {
        header.add(CRLF);
        return this;
    }

    public MessageBuilder addBody(byte[] data) {
        body = data;
        return this;
    }

    public byte[] getMessage() {
        StringBuilder builder = new StringBuilder();
        header.forEach((String el) -> {
            if (el != header.get(0)) builder.append(' ');  // comparing reference intentionally
            builder.append(el);
        });

        builder.append(' ' + CRLF + CRLF);
        String str =  builder.toString();
        byte[] header = str.getBytes();

        if (body != null) {
            byte[] data = new byte[header.length + body.length];

            System.arraycopy(header, 0, data, 0, header.length);
            System.arraycopy(body, 0, data, header.length, body.length);

            return data;
        }
        return header;
    }
}
