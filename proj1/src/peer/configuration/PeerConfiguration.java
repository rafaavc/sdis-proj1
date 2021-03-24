package configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import channels.MulticastChannel;
import messages.MessageFactory;
import state.PeerState;

public class PeerConfiguration {
    private final String protocolVersion, peerId, serviceAccessPoint;
    private final MulticastChannel mc, mdb, mdr;
    private final MessageFactory factory;
    private final PeerState state;
    private final Map<String, List<Integer>> storedCount = new HashMap<>();   // need to either store the ids of the peers who have alread sent STORED or reset the counter in each turn

    public PeerConfiguration(String protocolVersion, String peerId, String serviceAccessPoint, MulticastChannel mc, MulticastChannel mdb, MulticastChannel mdr) throws ClassNotFoundException, IOException {
        this.protocolVersion = protocolVersion;
        this.peerId = peerId;
        this.serviceAccessPoint = serviceAccessPoint;
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
        this.factory = new MessageFactory(1, 0);
        this.state = PeerState.read(this.getRootDir());
    }

    public void addStoredCount(String fileId, int chunkNo, int peerId) {
        String key = fileId + chunkNo;
        if (this.storedCount.containsKey(key)) {
            List<Integer> peerList = this.storedCount.get(key);
            if (!peerList.contains(peerId)) peerList.add(peerId);
        } else {
            this.storedCount.put(key, new ArrayList<Integer>(peerId));
        }
    }

    public int getStoredCount(String fileId, int chunkNo) {
        return this.storedCount.get(fileId + chunkNo).size();
    }

    public PeerState getState() {
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
}
