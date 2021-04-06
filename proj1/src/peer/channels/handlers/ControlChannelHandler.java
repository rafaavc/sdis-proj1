package channels.handlers;

import messages.Message;
import state.ChunkInfo;

import java.util.Random;

import configuration.PeerConfiguration;
import files.FileManager;

public class ControlChannelHandler extends Handler {
    public ControlChannelHandler(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        FileManager fileManager = new FileManager(this.configuration.getPeerId());
        try {
            switch(msg.getMessageType()) { 
                case STORED:
                    this.configuration.addStoredCount(msg.getFileId(), msg.getChunkNo(), Integer.parseInt(msg.getSenderId())); // TODO change peer id type to int
                    break;
                case DELETE:
                    this.configuration.getPeerState().deleteFileChunks(msg.getFileId());
                    fileManager.deleteFileChunks(msg.getFileId());
                    break;
                case GETCHUNK:
                    if (this.configuration.getPeerState().hasChunk(msg.getFileId(), msg.getChunkNo())) {
                        Thread.sleep(new Random().nextInt(400));

                        if (this.configuration.hasReceivedChunk(msg.getFileId(), msg.getChunkNo())) break;
                        // else: send chunk

                        byte[] chunkData = fileManager.readChunk(msg.getFileId(), msg.getChunkNo());
                        byte[] chunkMsg = this.configuration.getMessageFactory().getChunkMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo(), chunkData);
                        
                        this.configuration.getMDR().send(chunkMsg);
                    }
                    break;
                case REMOVED:
                    if (this.configuration.getPeerState().hasChunk(msg.getFileId(), msg.getChunkNo())) {
                        ChunkInfo chunk = this.configuration.getPeerState().getChunk(msg.getFileId(), msg.getChunkNo());
                        chunk.setPerceivedReplicationDegree(chunk.getPerceivedReplicationDegree() - 1);

                        if (chunk.getPerceivedReplicationDegree() >= chunk.getDesiredReplicationDegree()) break;

                        byte[] chunkData = fileManager.readChunk(chunk.getFileId(), chunk.getChunkNo());

                        this.configuration.resetHasReceivedPutchunk(chunk.getFileId(), chunk.getChunkNo());

                        // esperar entre 0 e 400 ms e se receber um putchunk deste chunk abortar
                        Thread.sleep(new Random().nextInt(400));

                        if (this.configuration.hasReceivedPutchunk(chunk.getFileId(), chunk.getChunkNo())) break;

                        // reset stored count
                        this.configuration.resetStoredCount(chunk.getFileId(), chunk.getChunkNo());
                        byte[] putchunkMsg = this.configuration.getMessageFactory().getPutchunkMessage(this.configuration.getPeerId(), chunk.getFileId(), chunk.getDesiredReplicationDegree() - 1 /* This peer already has the chunk */, chunk.getChunkNo(), chunkData);
                
                        int count = 0, sleepAmount = 1000, replicationDegree = 0;
                        while(count < 5) {
                            this.configuration.getMDB().send(putchunkMsg);
                            Thread.sleep(sleepAmount);
                            System.out.println("Checking stored count = " + this.configuration.getStoredCount(chunk.getFileId(), chunk.getChunkNo()));
                            replicationDegree = Math.max(this.configuration.getStoredCount(chunk.getFileId(), chunk.getChunkNo()), replicationDegree);
                            if (replicationDegree >= chunk.getDesiredReplicationDegree() - 1) break;
                            sleepAmount *= 2;
                            count++;
                        }

                        chunk.setPerceivedReplicationDegree(replicationDegree + 1);
                    }
                    
                    break;
                default:
                    System.err.println("Received wrong message in ControlChannelHandler! " + msg);
                    break;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
