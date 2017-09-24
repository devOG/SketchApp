import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ClientHandler class
 */
public class ClientHandler {
    protected Socket client;
    protected ObjectOutputStream out;

    public ClientHandler(Socket client) {
        this.client = client;
        try {
            this.out = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}