package configuration;

import channels.MulticastChannel;

public class PeerConfiguration {
    private final String protocolVersion, peerId, serviceAccessPoint;
    private final MulticastChannel mc, mdb, mdr;

    public PeerConfiguration(String protocolVersion, String peerId, String serviceAccessPoint, MulticastChannel mc, MulticastChannel mdb, MulticastChannel mdr) {
        this.protocolVersion = protocolVersion;
        this.peerId = peerId;
        this.serviceAccessPoint = serviceAccessPoint;
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
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
}
