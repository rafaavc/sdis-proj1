package configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import channels.MulticastChannel;
import messages.MessageFactory;
import state.PeerState;

public class PeerConfiguration {
    private final String protocolVersion, peerId, serviceAccessPoint;
    private final MulticastChannel mc, mdb, mdr;
    private final MessageFactory factory;
    private final PeerState state;
    private final Map<String, List<Integer>> storedCount = new HashMap<>();   // need to either store the ids of the peers who have alread sent STORED or reset the counter in each turn
    private final Map<String, List<Integer>> chunksReceived = new HashMap<>(); 
    private final Map<String, Map<Integer, byte[]>> chunksDataReceived = new HashMap<>(); 

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

    public List<byte[]> getFileChunks(String fileId) {
        Collection<Integer> keys = this.chunksDataReceived.get(fileId).keySet();;
        
        List<Integer> keysSorted = keys.stream().collect(Collectors.toList());
        List<byte[]> res = new ArrayList<>();

        for (int key : keysSorted) res.add(this.chunksDataReceived.get(fileId).get(key));

        return res;
    }

    public void startWaitingForChunk(String fileId, int chunkNo) {
        if (!chunksDataReceived.containsKey(fileId)) chunksDataReceived.put(fileId, new HashMap<Integer, byte[]>());
        if (!chunksDataReceived.get(fileId).containsKey(chunkNo)) chunksDataReceived.get(fileId).put(chunkNo, null);
    }

    public void addChunkReceived(String fileId, int chunkNo, byte[] data) {
        if (isWaitingForChunk(fileId, chunkNo)) chunksDataReceived.get(fileId).put(chunkNo, data);

        if (!chunksReceived.containsKey(fileId)) chunksReceived.put(fileId, new ArrayList<Integer>());
        if (!chunksReceived.get(fileId).contains(chunkNo)) chunksReceived.get(fileId).add(chunkNo);
    }

    public boolean hasReceivedAllChunksData(String fileId) {
        if (!chunksDataReceived.containsKey(fileId)) {
            System.err.println("I have no entry for file with id '" + fileId + "' in the chunk reception map (not waiting for it)");
            return true;
        }
        return !chunksDataReceived.get(fileId).values().contains(null);  // has received all chunks if no chunk entry has the null value
    }

    public boolean hasReceivedChunk(String fileId, int chunkNo) {
        return chunksReceived.containsKey(fileId) && chunksReceived.get(fileId).contains(chunkNo);
    }

    public boolean hasReceivedChunkData(String fileId, int chunkNo) {
        if (chunksDataReceived.containsKey(fileId) && 
            chunksDataReceived.get(fileId).containsKey(chunkNo)  &&
            chunksDataReceived.get(fileId).get(chunkNo) != null /* if null it hasn't been received yet */) return true;
        return false;
    }

    public byte[] getReceivedChunkData(String fileId, int chunkNo) {
        if (!hasReceivedChunkData(fileId, chunkNo)) return null;
        return chunksDataReceived.get(fileId).get(chunkNo);
    }

    public boolean isWaitingForChunk(String fileId, int chunkNo) {
        if (chunksDataReceived.containsKey(fileId) && 
            chunksDataReceived.get(fileId).containsKey(chunkNo)  &&
            chunksDataReceived.get(fileId).get(chunkNo) == null /* if null it hasn't been received yet */) return true;
        return false;
    }

    public void addStoredCount(String fileId, int chunkNo, int peerId) {
        String key = fileId + chunkNo;
        if (this.storedCount.containsKey(key)) {
            List<Integer> peerList = this.storedCount.get(key);
            if (!peerList.contains(peerId)) peerList.add(peerId);
        } else {
            this.storedCount.put(key, new ArrayList<Integer>(peerId));
        }
        // should the storedCount be kept in non-volatile memory? :thinking:
        this.state.updateChunkPerceivedRepDegree(fileId, chunkNo, this.storedCount.get(key).size()); // updates if already existing in the peer's state
    }

    public int getStoredCount(String fileId, int chunkNo) {
        return this.storedCount.containsKey(fileId + chunkNo) ? this.storedCount.get(fileId + chunkNo).size() : 0;
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
}
