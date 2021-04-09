package channels.handlers.strategies;

import java.io.IOException;

import configuration.PeerConfiguration;
import exceptions.ArgsException;
import messages.Message;

public abstract class BackupStrategy {
    protected final PeerConfiguration configuration;
    public BackupStrategy(PeerConfiguration configuration) {
        this.configuration = configuration;
    }
    public abstract void backup(Message message) throws IOException, ArgsException, Exception;
    public abstract void sendAlreadyHadStored(Message message) throws IOException, ArgsException, Exception;
}
