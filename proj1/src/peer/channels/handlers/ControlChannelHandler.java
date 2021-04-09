package channels.handlers;

import messages.trackers.ChunkTracker;
import messages.trackers.DeleteTracker;
import messages.Message;
import messages.MessageFactory;
import messages.trackers.StoredTracker;
import messages.trackers.PutchunkTracker;
import state.ChunkInfo;
import state.ChunkPair;
import state.FileInfo;

import java.net.InetAddress;
import java.util.Random;

import channels.handlers.strategies.RestoreStrategy;
import configuration.PeerConfiguration;
import files.FileManager;

public class ControlChannelHandler extends Handler {
    private final RestoreStrategy restoreStrategy;

    public ControlChannelHandler(PeerConfiguration configuration, RestoreStrategy restoreStrategy) {
        super(configuration);
        this.restoreStrategy = restoreStrategy;
    }

    public void execute(Message msg, InetAddress senderAddress) {
        FileManager fileManager = new FileManager(this.configuration.getPeerId());
        StoredTracker storedTracker = configuration.getStoredTracker();
        PutchunkTracker putchunkTracker = configuration.getPutchunkTracker();
        ChunkTracker chunkTracker = configuration.getChunkTracker();
        DeleteTracker deleteTracker = configuration.getDeleteTracker();

        try {
            MessageFactory msgFactoryVanilla = new MessageFactory(1, 0);
            switch(msg.getMessageType()) { 
                case STORED:
                    //System.out.println("Received stored from peer " + msg.getSenderId() + " of file " + msg.getFileId() + ", chunk " + msg.getChunkNo());
                    // this works also in reclaim (because the peers send all the stored even if they have the chunk)
                    storedTracker.addStoredCount(peerState, msg.getFileId(), msg.getChunkNo(), Integer.parseInt(msg.getSenderId())); // TODO change peer id type to int
                    break;
                case DELETE:
                    if (peerState.hasFileChunks(msg.getFileId())) {
                        peerState.deleteFileChunks(msg.getFileId());
                        fileManager.deleteFileChunks(msg.getFileId());
                    }
                    peerState.addDeletedFile(msg.getFileId());
                    deleteTracker.addDeleteReceived(msg.getFileId());
                    break;
                case FILECHECK:
                    if (configuration.getProtocolVersion().equals("1.1") && peerState.isDeleted(msg.getFileId())) {
                        deleteTracker.resetHasReceivedDelete(msg.getFileId());
                        Thread.sleep(new Random().nextInt(400));

                        if (deleteTracker.hasReceivedDelete(msg.getFileId())) break;
                        
                        byte[] deleteMsg = msgFactoryVanilla.getDeleteMessage(configuration.getPeerId(), msg.getFileId());
                        configuration.getMC().send(deleteMsg);
                    }
                    break;
                case GETCHUNK:
                    if (peerState.hasChunk(msg.getFileId(), msg.getChunkNo())) {
                        Thread.sleep(new Random().nextInt(400));

                        if (chunkTracker.hasReceivedChunk(msg.getFileId(), msg.getChunkNo())) break;

                        restoreStrategy.sendChunk(msg);
                    }
                    break;
                case REMOVED:
                    if (peerState.ownsFileWithId(msg.getFileId()))
                    {
                        FileInfo file = peerState.getFile(msg.getFileId());
                        ChunkPair chunk = file.getChunk(msg.getChunkNo());
                        storedTracker.resetStoredCount(msg.getFileId(), msg.getChunkNo());
                        chunk.setPerceivedReplicationDegree(chunk.getPerceivedReplicationDegree() - 1);

                        Thread.sleep(2000);
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

                        byte[] putchunkMsg = msgFactoryVanilla.getPutchunkMessage(this.configuration.getPeerId(), chunk.getFileId(), chunk.getDesiredReplicationDegree(), chunk.getChunkNo(), chunkData);
                        byte[] storedMsg = msgFactoryVanilla.getStoredMessage(this.configuration.getPeerId(), chunk.getFileId(), chunk.getChunkNo());

                        int count = 0, sleepAmount = 1000, replicationDegree = 0;
                        while(count < 5) {
                            this.configuration.getMDB().send(putchunkMsg);

                            int randVal = new Random().nextInt(400);
                            Thread.sleep(randVal);
                            this.configuration.getMC().send(storedMsg);

                            Thread.sleep(sleepAmount - randVal);

                            replicationDegree = Math.max(storedTracker.getStoredCount(chunk.getFileId(), chunk.getChunkNo()), replicationDegree);
                            if (replicationDegree >= chunk.getDesiredReplicationDegree()) break;
                            
                            sleepAmount *= 2;
                            count++;
                        }

                        // this is for the other peers to take this one into account
                        

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
