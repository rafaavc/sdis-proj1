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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import actions.ReclaimChunkBackup;
import channels.handlers.strategies.RestoreStrategy;
import configuration.PeerConfiguration;
import configuration.ProtocolVersion;
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
        ScheduledThreadPoolExecutor threadScheduler = configuration.getThreadScheduler();

        try {
            MessageFactory msgFactoryVanilla = new MessageFactory(new ProtocolVersion(1, 0));
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

                        threadScheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                if (deleteTracker.hasReceivedDelete(msg.getFileId())) return;
                                
                                try 
                                {
                                    byte[] deleteMsg = msgFactoryVanilla.getDeleteMessage(configuration.getPeerId(), msg.getFileId());
                                    configuration.getMC().send(deleteMsg);
                                } 
                                catch(Exception e) 
                                {
                                    System.err.println(e.getMessage());
                                }
                            }
                        }, configuration.getRandomDelay(400), TimeUnit.MILLISECONDS);

                    }
                    break;
                case GETCHUNK:
                    if (peerState.hasChunk(msg.getFileId(), msg.getChunkNo())) {

                        threadScheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                try 
                                {
                                    if (chunkTracker.hasReceivedChunk(msg.getFileId(), msg.getChunkNo())) return;
                                    restoreStrategy.sendChunk(msg);
                                } 
                                catch(Exception e) 
                                {
                                    System.err.println(e.getMessage());
                                }
                            }
                        }, configuration.getRandomDelay(400), TimeUnit.MILLISECONDS);
                    }
                    break;
                case REMOVED:
                    if (peerState.ownsFileWithId(msg.getFileId()))
                    {
                        FileInfo file = peerState.getFile(msg.getFileId());
                        ChunkPair chunk = file.getChunk(msg.getChunkNo());
                        storedTracker.resetStoredCount(msg.getFileId(), msg.getChunkNo());
                        chunk.setPerceivedReplicationDegree(chunk.getPerceivedReplicationDegree() - 1);

                        threadScheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                try 
                                {
                                    int count = storedTracker.getStoredCount(msg.getFileId(), msg.getChunkNo());
                                    if (count != 0) chunk.setPerceivedReplicationDegree(count);
                                } 
                                catch(Exception e) 
                                {
                                    System.err.println(e.getMessage());
                                }
                            }
                        }, 5000, TimeUnit.MILLISECONDS);
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

                        threadScheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                try
                                {
                                    // if received putchunk abort (another peer already initiated backup)
                                    if (putchunkTracker.hasReceivedPutchunk(chunk.getFileId(), chunk.getChunkNo())) return;

                                    System.out.println("Restarting backup of (" + chunk + ") after receiving REMOVED.");

                                    // because this peer already has the chunk
                                    storedTracker.addStoredCount(peerState, msg.getFileId(), msg.getChunkNo(), Integer.parseInt(configuration.getPeerId()));

                                    byte[] putchunkMsg = msgFactoryVanilla.getPutchunkMessage(configuration.getPeerId(), chunk.getFileId(), chunk.getDesiredReplicationDegree(), chunk.getChunkNo(), chunkData);
                                    byte[] storedMsg = msgFactoryVanilla.getStoredMessage(configuration.getPeerId(), chunk.getFileId(), chunk.getChunkNo());

                                    threadScheduler.schedule(new ReclaimChunkBackup(configuration, chunk, putchunkMsg, storedMsg), 0, TimeUnit.MILLISECONDS);
                                } 
                                catch(Exception e) 
                                {
                                    System.err.println(e.getMessage());
                                }
                            }
                        }, configuration.getRandomDelay(400), TimeUnit.MILLISECONDS);
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
