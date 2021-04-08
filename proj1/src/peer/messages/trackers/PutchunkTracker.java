package messages.trackers;

import java.util.ArrayList;
import java.util.List;

public class PutchunkTracker {
    private final List<String> putchunksReceived = new ArrayList<>();

    public synchronized void resetHasReceivedPutchunk(String fileId, int chunkNo) {
        this.putchunksReceived.remove(fileId + chunkNo);
    }

    public synchronized boolean hasReceivedPutchunk(String fileId, int chunkNo) {
        return this.putchunksReceived.contains(fileId + chunkNo);
    }

    public synchronized void addPutchunkReceived(String fileId, int chunkNo) {
        this.putchunksReceived.add(fileId + chunkNo);
    }
}
