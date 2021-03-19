import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class Client implements ClientInterface, Serializable {
    private static final long serialVersionUID = 1L;

    public void hi() throws RemoteException {
        System.out.println("Hi from peer");
    }
}
