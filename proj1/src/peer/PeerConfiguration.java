import java.io.Serializable;
import java.rmi.Remote;

public class PeerConfiguration implements Serializable, Remote {
    private static final long serialVersionUID = 7686282043870935656L;
    private final String protocolVersion, peerId, serviceAccessPoint;
    private final MulticastChannelName mc, mdb, mdr;

    public PeerConfiguration(String protocolVersion, String peerId, String serviceAccessPoint, MulticastChannelName mc, MulticastChannelName mdb, MulticastChannelName mdr) {
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

    public MulticastChannelName getMC() {
        return mc;
    }

    public MulticastChannelName getMDB() {
        return mdb;
    }

    public MulticastChannelName getMDR() {
        return mdr;
    }
}
