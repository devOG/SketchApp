import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SendMessage class
 * Uppdaterar anslutna klienter med ritobjekt
 */
public class SendMessage extends Thread {
        protected List<ClientHandler> clients;
        protected String userInput;
        protected BufferedReader console;
		protected Object obj;

        public SendMessage(Collection<ClientHandler> clients, Object obj) {
            this.clients = new ArrayList<ClientHandler>();
            this.clients.addAll(clients);
            this.userInput = null;
            this.obj = obj;
            this.start();
        }

        public void run() {
            try {
                if (clients.size() > 0) {
                    for (ClientHandler client : clients) {
                        client.out.writeObject(obj);
                        client.out.flush();
                    Thread.currentThread();
                    Thread.sleep(1 * 1000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
