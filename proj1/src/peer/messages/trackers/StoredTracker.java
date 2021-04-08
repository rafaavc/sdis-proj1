package messages.trackers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import state.PeerState;

public class StoredTracker {
    private final Map<String, List<Integer>> storedCount = new HashMap<>();

    public synchronized void resetStoredCount(String fileId, int chunkNo) {
        String key = fileId + chunkNo;
        if (this.storedCount.containsKey(key)) {
            this.storedCount.put(key, new ArrayList<>());  // resets the count
        }
    }

    public synchronized void addStoredCount(PeerState state, String fileId, int chunkNo, int peerId) {
        String key = fileId + chunkNo;
        if (this.storedCount.containsKey(key)) {
            List<Integer> peerList = this.storedCount.get(key);
            if (!peerList.contains(peerId)) peerList.add(peerId);
        } else {
            List<Integer> peerList = new ArrayList<Integer>();
            peerList.add(peerId);
            this.storedCount.put(key, peerList);
        }

        // should the storedCount be kept in non-volatile memory? :thinking:
        state.updateChunkPerceivedRepDegree(fileId, chunkNo, this.storedCount.get(key).size()); // updates if already existing in the peer's state
    }

    public synchronized int getStoredCount(String fileId, int chunkNo) {
        return this.storedCount.containsKey(fileId + chunkNo) ? this.storedCount.get(fileId + chunkNo).size() : 0;
    }
}
