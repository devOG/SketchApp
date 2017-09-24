import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * Server class
 */
public class Server extends JFrame{

	// klassmedlemmar
	private static final long serialVersionUID = 1L;
	private String serverPort;
	private String serverIp;
	private int index = 1;
	private JList<String> text;
	private ServerSocket server;
	protected Map<Integer, ClientHandler> clientList;
	private DefaultListModel<String> model;
	
	// default konstruktor
	public Server(){
		setTitle("Drawing Server");
		setSize(600, 300);
		setLocationRelativeTo(null);
		
		createForm();
		startServer();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().add(createTopInfo(), BorderLayout.PAGE_START);
		getContentPane().add(createCenterInfo(), BorderLayout.CENTER);
		getContentPane().add(createBottomBar(), BorderLayout.PAGE_END);
		
	}
	
	// starta servern
	private void startServer() {
		try {
			server = new ServerSocket(Integer.parseInt(serverPort));
			serverIp = InetAddress.getLocalHost().getHostAddress();
			clientList = new HashMap<Integer, ClientHandler>();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	      System.out.println("Servern lyssnar på port "+ serverPort);
	      
	      new Thread(new Runnable() {

			public void run() {
				while (true) {
		            try {
		                Socket client = server.accept();
		                ClientHandler newClient = new ClientHandler(client);
		                clientList.put(index, newClient);
		                final ObjectInputStream input = new ObjectInputStream(client.getInputStream());
		                Object readObject = input.readObject();
		                System.out.println(readObject + " ansluten");
		                
		                if(readObject.getClass().toString().contains(String.class.toString())){
		                	model.addElement(index+" - "+readObject + " "+client.getInetAddress().getHostName());
		                	index++;
				        }
		                new Thread(new Runnable() {
					
							public void run() {
								try {
									Object object = null;
									while((object = input.readObject()) != null){
										System.out.println("skickar objekt");
							        	new SendMessage(clientList.values(), object);
									}
								}catch(Exception e) {
									System.out.println("Error: " + e.getMessage());
								}
							}
						  }).start();
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }
			}
		  }).start(); 
	}

	// skapa bottenrad med knappar för att uppdatera listan och ta bort klienter från servern
	private Component createBottomBar() {
		JPanel pane = new JPanel();
		JButton b1 = new JButton("Ta bort");
		JButton b2 = new JButton("Uppdatera");
		b1.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				String selectedValue = text.getSelectedValue();
				String index = selectedValue.split(" ")[0];
				model.removeElement(selectedValue);
				clientList.remove(Integer.parseInt(index));
				System.out.println("Klient borttagen "+ selectedValue);
			}
		});
		pane.add(b1);
		pane.add(b2);
		return pane;
	}

	// skapa listan med anslutna klienter
	private Component createCenterInfo() {
		text = new JList<String>();
		model = new DefaultListModel<String>();
		text.setModel(model);
		return text;
	}

	// skapa ruta med serverinfo
	private Component createTopInfo() {
		JPanel pane = new JPanel(new GridLayout(3,1, 3, 3));
		JLabel l1 = new JLabel("Serverinfo");
		
		JPanel pIp = new JPanel(null);
		JLabel l2 = new JLabel("Ip:");
		l2.setBounds(2, 0, 35, 15);
		l2.setFont(new Font(null, Font.BOLD, 12));
		
		JLabel l2a = new JLabel(serverIp);
		l2a.setBounds(35, 0, 150, 15);
		pIp.add(l2);
		pIp.add(l2a);
		
		JPanel pPort = new JPanel(null);
		JLabel l3 = new JLabel("Port:");
		l3.setBounds(2, 0, 35, 15);
		l3.setFont(new Font(null, Font.BOLD, 12));
		JLabel l3a = new JLabel(serverPort);
		l3a.setBounds(35, 0, 150, 15);
		pPort.add(l3);
		pPort.add(l3a);
		pane.add(l1);
		pane.add(pIp);
		pane.add(pPort);
		return pane;
	}
	
	// skapa ruta som låter användaren skriva in vilken port servern ska lyssna på
	private void createForm(){
		final JDialog frame = new JDialog(this, true);
		frame.setLocationRelativeTo(null);
		JPanel p = new JPanel(new GridLayout(3, 1));
		JLabel lb = new JLabel("Skriv in en port för servern(1024 - 65535)");
		JTextField tx = new JTextField();
		JButton b1 = new JButton("OK");
		b1.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(tx.getText() != null && !tx.getText().isEmpty()){
					serverPort = tx.getText();
					frame.dispose();
				}
			}
		});
		JButton b2 = new JButton("Avbryt");
		b2.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		JPanel p1 = new JPanel();
		p1.add(b1);
		p1.add(b2);
		
		p.add(lb);
		p.add(tx);
		p.add(p1);
		
		frame.getContentPane().add(p);
		frame.setMinimumSize(new Dimension(200, 150));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		try { 
	        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); 
	    }catch(Exception e){ 
	    }
		new Server().setVisible(true);
	}
}
