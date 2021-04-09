package channels.handlers;

import messages.trackers.ChunkTracker;
import messages.Message;
import messages.trackers.StoredTracker;
import messages.trackers.PutchunkTracker;
import state.ChunkInfo;
import state.ChunkPair;
import state.FileInfo;

import java.util.Random;

import configuration.PeerConfiguration;
import files.FileManager;

public class ControlChannelHandler extends Handler {
    public ControlChannelHandler(PeerConfiguration configuration) {
        super(configuration);
    }

    public void execute(Message msg) {
        FileManager fileManager = new FileManager(this.configuration.getPeerId());
        StoredTracker storedTracker = configuration.getStoredTracker();
        PutchunkTracker putchunkTracker = configuration.getPutchunkTracker();
        ChunkTracker chunkTracker = configuration.getChunkTracker();
        
        try {
            switch(msg.getMessageType()) { 
                case STORED:
                    //System.out.println("Received stored from peer " + msg.getSenderId() + " of file " + msg.getFileId() + ", chunk " + msg.getChunkNo());
                    // this works also in reclaim (because the peers send all the stored even if they have the chunk)
                    storedTracker.addStoredCount(peerState, msg.getFileId(), msg.getChunkNo(), Integer.parseInt(msg.getSenderId())); // TODO change peer id type to int
                    break;
                case DELETE:
                    peerState.deleteFileChunks(msg.getFileId());
                    fileManager.deleteFileChunks(msg.getFileId());
                    break;
                case GETCHUNK:
                    if (peerState.hasChunk(msg.getFileId(), msg.getChunkNo())) {
                        Thread.sleep(new Random().nextInt(400));

                        if (chunkTracker.hasReceivedChunk(msg.getFileId(), msg.getChunkNo())) break;
                        // else: send chunk

                        byte[] chunkData = fileManager.readChunk(msg.getFileId(), msg.getChunkNo());
                        byte[] chunkMsg = this.configuration.getMessageFactory().getChunkMessage(this.configuration.getPeerId(), msg.getFileId(), msg.getChunkNo(), chunkData);
                        
                        this.configuration.getMDR().send(chunkMsg);
                    }
                    break;
                case REMOVED:
                    // TODO the case where the peer is the file owner (update chunks replication degrees)
                    if (peerState.ownsFileWithId(msg.getFileId()))
                    {
                        FileInfo file = peerState.getFile(msg.getFileId());
                        ChunkPair chunk = file.getChunk(msg.getChunkNo());
                        chunk.setPerceivedReplicationDegree(chunk.getPerceivedReplicationDegree() - 1);

                        Thread.sleep(2000);
                        storedTracker.resetStoredCount(msg.getFileId(), msg.getChunkNo());
                        int count = storedTracker.getStoredCount(msg.getFileId(), msg.getChunkNo());
                        if (count != 0) chunk.setPerceivedReplicationDegree(count);
                    }
                    else if (peerState.hasChunk(msg.getFileId(), msg.getChunkNo())) 
                    {
                        // So that previously received stored don't influence the outcome
                        storedTracker.resetStoredCount(msg.getFileId(), msg.getChunkNo());

                        // so that previously received putchunks don't matter
                        putchunkTracker.resetHasReceivedPutchunk(msg.getFileId(), msg.getChunkNo());

                        ChunkInfo chunk = peerState.getChunk(msg.getFileId(), msg.getChunkNo());

                        chunk.setPerceivedReplicationDegree(chunk.getPerceivedReplicationDegree() - 1);

                        // if there is no need to backup the chunk
                        if (chunk.getPerceivedReplicationDegree() >= chunk.getDesiredReplicationDegree()) break;

                        byte[] chunkData = fileManager.readChunk(chunk.getFileId(), chunk.getChunkNo());
                        Thread.sleep(new Random().nextInt(400)); 

                        // if received putchunk abort (another peer already initiated backup)
                        if (putchunkTracker.hasReceivedPutchunk(chunk.getFileId(), chunk.getChunkNo())) break;

                        System.out.println("Restarting backup of (" + chunk + ") after receiving REMOVED.");

                        // because this peer already has the chunk
                        storedTracker.addStoredCount(peerState, msg.getFileId(), msg.getChunkNo(), Integer.parseInt(this.configuration.getPeerId()));

                        byte[] putchunkMsg = this.configuration.getMessageFactory().getPutchunkMessage(this.configuration.getPeerId(), chunk.getFileId(), chunk.getDesiredReplicationDegree(), chunk.getChunkNo(), chunkData);
                
                        int count = 0, sleepAmount = 1000, replicationDegree = 0;
                        while(count < 5) {
                            this.configuration.getMDB().send(putchunkMsg);

                            Thread.sleep(sleepAmount);

                            replicationDegree = Math.max(storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo()), replicationDegree);
                            if (replicationDegree >= chunk.getDesiredReplicationDegree()) break;
                            
                            sleepAmount *= 2;
                            count++;
                        }

                        // this is for the other peers to take this one into account
                        byte[] storedMsg = this.configuration.getMessageFactory().getStoredMessage(this.configuration.getPeerId(), chunk.getFileId(), chunk.getChunkNo());
                        this.configuration.getMC().send(storedMsg);

                        chunk.setPerceivedReplicationDegree(replicationDegree);
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
