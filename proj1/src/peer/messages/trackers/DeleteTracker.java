package messages.trackers;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeleteTracker {
    private static final Queue<DeleteTracker> deleteTrackers = new ConcurrentLinkedQueue<>();
    private final Queue<String> deletesReceived = new ConcurrentLinkedQueue<>();
    private boolean active = true;

    public static DeleteTracker getNewTracker() {
        DeleteTracker tracker = new DeleteTracker();
        deleteTrackers.add(tracker);
        return tracker;
    }

    public static void addDeleteReceived(String fileId) {
        for (DeleteTracker tracker : deleteTrackers) {
            tracker.addDelete(fileId);
        }
    }

    public static void removeTracker(DeleteTracker tracker) {
        deleteTrackers.remove(tracker);
        tracker.active = false;
    }

    public synchronized boolean hasReceivedDelete(String fileId) {
        if (!active) System.err.println("called hasReceivedDelete on inactive DeleteTracker");
        return this.deletesReceived.contains(fileId);
    }

    private synchronized void addDelete(String fileId) {
        if (!active) System.err.println("called addDelete on inactive DeleteTracker");
        synchronized (deletesReceived) {
            if (!this.deletesReceived.contains(fileId)) this.deletesReceived.add(fileId);
        }
    }
}
