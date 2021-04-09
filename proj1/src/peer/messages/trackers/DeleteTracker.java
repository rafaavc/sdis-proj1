package messages.trackers;

import java.util.ArrayList;
import java.util.List;

public class DeleteTracker {
    private final List<String> deletesReceived = new ArrayList<>();

    public synchronized void resetHasReceivedDelete(String fileId) {
        this.deletesReceived.remove(fileId);
    }

    public synchronized boolean hasReceivedDelete(String fileId) {
        return this.deletesReceived.contains(fileId);
    }

    public synchronized void addDeleteReceived(String fileId) {
        if (!this.deletesReceived.contains(fileId)) this.deletesReceived.add(fileId);
    }
}
