package configuration;

import java.io.IOException;

import channels.MulticastChannel;
import channels.handlers.strategies.BackupStrategy;
import channels.handlers.strategies.EnhancedBackupStrategy;
import channels.handlers.strategies.EnhancedRestoreStrategy;
import channels.handlers.strategies.RestoreStrategy;
import channels.handlers.strategies.VanillaBackupStrategy;
import channels.handlers.strategies.VanillaRestoreStrategy;
import exceptions.ArgsException;
import messages.trackers.ChunkTracker;
import messages.trackers.PutchunkTracker;
import messages.trackers.StoredTracker;
import messages.trackers.DeleteTracker;
import state.PeerState;

public class PeerConfiguration {
    private final String protocolVersion, peerId, serviceAccessPoint;
    private final MulticastChannel mc, mdb, mdr;
    private final PeerState state;
    private final ChunkTracker chunkTracker;
    private final PutchunkTracker putchunkTracker;
    private final StoredTracker storedTracker;
    private final DeleteTracker deleteTracker;

    public PeerConfiguration(String protocolVersion, String peerId, String serviceAccessPoint, MulticastChannel mc, MulticastChannel mdb, MulticastChannel mdr) throws ClassNotFoundException, IOException, ArgsException {
        this.protocolVersion = protocolVersion;
        this.peerId = peerId;
        this.serviceAccessPoint = serviceAccessPoint;
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
        this.state = PeerState.read(this.getRootDir());
        this.chunkTracker = new ChunkTracker();
        this.putchunkTracker = new PutchunkTracker();
        this.storedTracker = new StoredTracker();
        this.deleteTracker = new DeleteTracker();
    }

    public PeerState getPeerState() {
        return state;
    }

    public String getRootDir() {
        return this.peerId;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public BackupStrategy getBackupStrategy() throws ArgsException {
        if (this.protocolVersion.equals("1.0")) return new VanillaBackupStrategy(this);
        if (this.protocolVersion.equals("1.1")) return new EnhancedBackupStrategy(this);
        return null;
    }

    public RestoreStrategy getRestoreStrategy() throws ArgsException {
        if (this.protocolVersion.equals("1.0")) return new VanillaRestoreStrategy(this);
        if (this.protocolVersion.equals("1.1")) return new EnhancedRestoreStrategy(this);
        return null;
    }

    public String getPeerId() {
        return peerId;
    }

    public String getServiceAccessPoint() {
        return serviceAccessPoint;
    }

    public MulticastChannel[] getChannels() {
        return new MulticastChannel[] { this.mc, this.mdb, this.mdr };
    }

    public MulticastChannel getMC() {
        return mc;
    }
    
    public MulticastChannel getMDB() {
        return mdb;
    }

    public MulticastChannel getMDR() {
        return mdr;
    }

    public ChunkTracker getChunkTracker() {
        return chunkTracker;
    }

    public PutchunkTracker getPutchunkTracker() {
        return putchunkTracker;
    }

    public StoredTracker getStoredTracker() {
        return storedTracker;
    }

    public DeleteTracker getDeleteTracker() {
        return deleteTracker;
    }
}
