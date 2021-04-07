package configuration;

import java.io.IOException;

import channels.MulticastChannel;
import exceptions.ArgsException;
import messages.ChunkTracker;
import messages.MessageFactory;
import messages.PutchunkTracker;
import messages.StoredTracker;
import state.PeerState;

public class PeerConfiguration {
    private final String protocolVersion, peerId, serviceAccessPoint;
    private final MulticastChannel mc, mdb, mdr;
    private final MessageFactory factory;
    private final PeerState state;
    private final ChunkTracker chunkTracker;
    private final PutchunkTracker putchunkTracker;
    private final StoredTracker storedTracker;

    public PeerConfiguration(String protocolVersion, String peerId, String serviceAccessPoint, MulticastChannel mc, MulticastChannel mdb, MulticastChannel mdr) throws ClassNotFoundException, IOException, ArgsException {
        this.protocolVersion = protocolVersion;
        this.peerId = peerId;
        this.serviceAccessPoint = serviceAccessPoint;
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
        this.factory = new MessageFactory(1, 0);
        this.state = PeerState.read(this.getRootDir());
        this.chunkTracker = new ChunkTracker();
        this.putchunkTracker = new PutchunkTracker();
        this.storedTracker = new StoredTracker();
    }

    public PeerState getPeerState() {
        return state;
    }

    public String getRootDir() {
        return this.peerId;
    }

    public MessageFactory getMessageFactory() {
        return factory;
    }

    public String getProtocolVersion() {
        return protocolVersion;
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
}
