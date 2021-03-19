import java.io.Serializable;
import java.rmi.Remote;

public class MulticastChannelName implements Serializable, Remote {
    private static final long serialVersionUID = -617834473823463967L;
    private final String host;
    private final int port;

    public MulticastChannelName(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
