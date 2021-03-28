package channels.handlers;

import messages.Message;

import channels.MulticastChannel.ChannelType;
import configuration.PeerConfiguration;

public abstract class Handler {
    protected final PeerConfiguration configuration;

    public Handler(PeerConfiguration configuration) {
        this.configuration = configuration;
    }

    public static Handler get(PeerConfiguration configuration, ChannelType type) {
        switch (type) {
            case CONTROL:
                return new ControlChannelHandler(configuration);
            case BACKUP:
                return new BackupChannelHandler(configuration);
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
