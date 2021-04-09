package channels.handlers;

import messages.Message;
import state.PeerState;
import channels.MulticastChannel.ChannelType;
import configuration.PeerConfiguration;

public abstract class Handler {
    protected final PeerConfiguration configuration;
    protected final PeerState peerState;

    public Handler(PeerConfiguration configuration) {
        this.configuration = configuration;
        this.peerState = configuration.getPeerState();
    }

    public static Handler get(PeerConfiguration configuration, ChannelType type) {
        switch (type) {
            case CONTROL:
                return new ControlChannelHandler(configuration);
            case BACKUP:
                return new BackupChannelHandler(configuration, configuration.getBackupStrategy());
            case RESTORE:
                return new RestoreChannelHandler(configuration);
        }
        return null;
    }

    public PeerConfiguration getConfiguration() {
        return configuration;
    }

    public abstract void execute(Message msg);
}
