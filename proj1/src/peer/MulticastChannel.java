import actions.Action;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MulticastChannel {
    private final InetAddress host;
    private final int port;
    private final String name, hostName;
    private final Action action;

    public MulticastChannel(String name, Action action, String host, int port) throws UnknownHostException {
        this.host = InetAddress.getByName(host);
        this.hostName = host;
        this.port = port;
        this.name = name;
        this.action = action;
    }

    public InetAddress getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return name + " (" + this.hostName + ":" + this.port + ")";
    }
}
