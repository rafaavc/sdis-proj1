package channels.actions;

import messages.Message;

import channels.MulticastChannel.ChannelType;
import configuration.PeerConfiguration;

public abstract class Action {
    protected final PeerConfiguration configuration;

    public Action(PeerConfiguration configuration) {
        this.configuration = configuration;
    }

    public static Action get(PeerConfiguration configuration, ChannelType type) {
        switch (type) {
            case CONTROL:
                return new ControlChannelAction(configuration);
            case BACKUP:
                return new BackupChannelAction(configuration);
            case RESTORE:
                return new RestoreChannelAction(configuration);
        }
        return null;
    }

    public PeerConfiguration getConfiguration() {
        return configuration;
    }

    public abstract void execute(Message msg);
}