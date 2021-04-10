package messages;

import java.util.Arrays;

import messages.Message.MessageType;

public class MessageParser {

    public static Message parse(byte[] data, int length) {
        int bodyStart = -1, headerEnd = -1;
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            if (b == 0xD) {
                if (data[i+1] == 0xA && data[i+2] == 0xD && data[i+3] == 0xA) {
                    if (data.length > i + 4 && data[i + 3] != ' ') bodyStart = i + 4;
                    headerEnd = i - 1;
                    break;
                }
            }
        }

        String header = new String(Arrays.copyOf(data, headerEnd + 1));

        String[] headerPieces = header.split(" +"); // regex for spaces (works with multiple spaces)

        String version = headerPieces[0], messageType = headerPieces[1], senderId = headerPieces[2], fileId = headerPieces[3];

        Message message = new Message(version, senderId, fileId);

        if (messageType.equals(Message.messageTypeStrings.get(MessageType.PUTCHUNK))) {
            message.setMessageType(MessageType.PUTCHUNK);
            message.setChunkNo(Integer.parseInt(headerPieces[4]));
            message.setReplicationDeg((short) Integer.parseInt(headerPieces[5]));
            byte[] body = Arrays.copyOfRange(data, bodyStart, length);
            message.setBody(body);

        } else if(messageType.equals(Message.messageTypeStrings.get(MessageType.STORED))) { // TODO

            message.setMessageType(MessageType.STORED);  
            message.setChunkNo(Integer.parseInt(headerPieces[4]));      

        } else if(messageType.equals(Message.messageTypeStrings.get(MessageType.GETCHUNK))) { // TODO

            message.setMessageType(MessageType.GETCHUNK);     
            message.setChunkNo(Integer.parseInt(headerPieces[4]));

        } else if (messageType.equals(Message.messageTypeStrings.get(MessageType.CHUNK))) { // TODO
            
            message.setMessageType(MessageType.CHUNK);
            message.setChunkNo(Integer.parseInt(headerPieces[4]));
            byte[] body = Arrays.copyOfRange(data, bodyStart, length);
            message.setBody(body);

        } else if(messageType.equals(Message.messageTypeStrings.get(MessageType.DELETE))) { // TODO

            message.setMessageType(MessageType.DELETE);

        } else if(messageType.equals(Message.messageTypeStrings.get(MessageType.REMOVED))) { // TODO
            
            message.setMessageType(MessageType.REMOVED);     
            message.setChunkNo(Integer.parseInt(headerPieces[4]));     

        } else if(messageType.equals(Message.messageTypeStrings.get(MessageType.FILECHECK))) { // TODO
            
            message.setMessageType(MessageType.FILECHECK);
        } 

        return message;
    }
}
