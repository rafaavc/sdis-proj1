package channels.handlers.strategies;

import configuration.PeerConfiguration;
import messages.Message;
import messages.MessageFactory;

public abstract class BackupStrategy {
    protected final PeerConfiguration configuration;
    protected final MessageFactory messageFactory;

    public BackupStrategy(PeerConfiguration configuration, MessageFactory messageFactory) {
        this.configuration = configuration;
        this.messageFactory = messageFactory;
    }
    
    public abstract void backup(Message message) throws Exception;
    public abstract void sendAlreadyHadStored(Message message) throws Exception;
}
