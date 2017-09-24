import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * PanelClient class
 */
public class PanelClient extends JFrame{

	// klassmedlemmar
	private static final long serialVersionUID = 1L;
	private static final String RECTANGLE = "Rektangel";
	private static final String FREEHAND = "Frihand";
	private Double lastWidth;
	public Color selectedColour = Color.BLACK;
	private JComboBox<String> choose;
	private JLabel coord;
	private JPanel pnColor;
	private RectanglePane area;
	private Socket client;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	protected String serverPort;
	protected String name;
	protected String serverIp;

	// default konstruktor
	public PanelClient(){
		setTitle("Drawing Client");
		setSize(600, 300);
		setLocationRelativeTo(null);
		lastWidth = getSize().getWidth();
		setJMenuBar(createMenu());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().add(createColorPane(), BorderLayout.PAGE_START);
		getContentPane().add(createDrawArea(), BorderLayout.CENTER);
		getContentPane().add(createBottomBar(), BorderLayout.PAGE_END);
		createForm();
		loginServer();
	}
	
	// anslut till servern
	private void loginServer() {
		try {
			this.client = new Socket(InetAddress.getByName(serverIp).getHostAddress(), Integer.parseInt(serverPort));
			output = new ObjectOutputStream(client.getOutputStream());
			output.writeObject(name);
			output.flush();
			this.input = new ObjectInputStream(client.getInputStream());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "ERROR: Det gick inte ansluta till servern", "error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

        new Thread(new Runnable() {
		
		public void run() {				
			try{
				Object buffer = null;
				while ((buffer = input.readObject()) != null) {
					area.setShapes((Map<Shape, Color>) buffer);
					System.out.println("Objekt mottaget ");
				}		          
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		 }
      }).start(); 
	}

	// skapa en meny
	private JMenuBar createMenu() {
		JMenuBar jMenu = new JMenuBar();
		JMenu file = new JMenu("Arkiv");
		JMenuItem newF = new JMenuItem("Ny");
		newF.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				area.clear();
			}
		});
		JMenuItem exitF = new JMenuItem("Avsluta");
		exitF.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		file.add(newF);
		file.add(exitF);
		
		jMenu.add(file);		
		return jMenu;
	}

	// skapa statusrad
	private Component createBottomBar() {
		JPanel pane = new JPanel(new BorderLayout());
		pane.setBackground(Color.LIGHT_GRAY);
		coord = new JLabel("Koordinater ");
		
		JPanel pn = new JPanel();
		pn.setBackground(Color.LIGHT_GRAY);
		JLabel colorTxt = new JLabel("Färgval:");
		pnColor = new JPanel();
		pnColor.setPreferredSize(new Dimension(50, 20));
		pnColor.setBackground(selectedColour);
		pn.add(colorTxt);
		pn.add(pnColor);
		pane.add(coord,  BorderLayout.LINE_START);
		pane.add(pn,  BorderLayout.LINE_END);
		return pane;
	}

	// skapa rityta
	private Component createDrawArea() {
		area = new RectanglePane();
		return area;
	}

	// skapa verktygsrad
	private Component createColorPane() {
		JPanel colorPanel = new JPanel();
		colorPanel.setPreferredSize(new Dimension(lastWidth.intValue(), 30));
		colorPanel.setLayout(new GridLayout());
		
		JPanel colorGreen = new JPanel();
		colorGreen.setBackground(Color.GREEN);
		colorGreen.addMouseListener(new ColourListener());
		JPanel colorBlue = new JPanel();
		colorBlue.setBackground(Color.BLUE);
		colorBlue.addMouseListener(new ColourListener());
		JPanel colorBlack = new JPanel();
		colorBlack.setBackground(Color.BLACK);
		colorBlack.addMouseListener(new ColourListener());
		JPanel colorRed = new JPanel();
		colorRed.setBackground(Color.RED);
		colorRed.addMouseListener(new ColourListener());
		JPanel colorYellow = new JPanel();
		colorYellow.setBackground(Color.YELLOW);
		colorYellow.addMouseListener(new ColourListener());
		
		choose = new JComboBox<String>();
		choose.addItem(RECTANGLE);
		choose.addItem(FREEHAND);
		
		colorPanel.add(colorGreen, 0);
		colorPanel.add(colorBlue, 1);
		colorPanel.add(colorBlack, 2);
		colorPanel.add(colorRed, 3);
		colorPanel.add(colorYellow, 4);
		colorPanel.add(choose);
		return colorPanel;
	}
	
	// inlogg till servern
	private void createForm(){
		final JDialog frame = new JDialog(this, true);
		frame.setLocationRelativeTo(null);
		JPanel p = new JPanel(new GridLayout(7, 1));
		JLabel lb = new JLabel("Namn");
		JTextField tx = new JTextField();
		JLabel lb1 = new JLabel("IP till servern");
		JTextField tx1 = new JTextField();
		JLabel lb2 = new JLabel("Portnummer");
		JTextField tx2 = new JTextField();
		JButton b1 = new JButton("OK");
		b1.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(tx.getText() != null && !tx.getText().isEmpty()){
					name = tx.getText();
					serverPort = tx2.getText();
					serverIp = tx1.getText();
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
		p.add(lb1);
		p.add(tx1);
		p.add(lb2);
		p.add(tx2);
		p.add(p1);
		
		frame.getContentPane().add(p);
		frame.setMinimumSize(new Dimension(200, 150));
		frame.pack();
		frame.setVisible(true);
	}
	
	// skicka ritobjekt
	private void sendInfo(Object obj) {
		try {
		  output.writeObject(obj);
		  output.flush();
	    }
	    catch(Exception e) {
	      System.out.println("Error2: " + e.getMessage());
	    }
	}
	
	/**
	 * ColourListener class (nested)
	 * Låter använderen välja en färg i verktygsraden
	 */
	class ColourListener implements MouseListener{

		public void mouseClicked(MouseEvent e) {
			selectedColour = ((JPanel) e.getSource()).getBackground();
			pnColor.setBackground(selectedColour);
		}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}	
	}
	
	/**
	 * RectanglePane class (nested)
	 * Låter användaren interagera med ritytan
	 */
	class RectanglePane extends JPanel implements MouseListener, MouseMotionListener{
		
		private static final long serialVersionUID = 1L;
		
		Point pointStart = null;
	    Point pointEnd   = null;
	    private List<Point> points = new LinkedList<Point>();
		private Rectangle rect;
		Map<Shape, Color> shapes = new HashMap<Shape,Color>();
        
        RectanglePane(){
        	addMouseListener(this);
        	addMouseMotionListener(this);
        }
        
        // rensa ritytan
        public void clear(){
        	points.clear();
        	shapes.clear();
        	repaint();
        }
        
        public void setShapes(Map<Shape, Color> shapes){
        	this.shapes = shapes;
        	repaint();
        }

		public void mouseClicked(MouseEvent e) {}

		// hämta startpositionen när användaren trycker ner musknappen och lägg till i points om frihandsverktyget används
		public void mousePressed(MouseEvent e) {
			pointStart = e.getPoint();
			if(choose.getSelectedItem().equals(FREEHAND)){
				points.add(pointStart);
			}
		}

		// skicka ritobjekt till servern när användaren släpper musknappen
		public void mouseReleased(MouseEvent e) {
			if(choose.getSelectedItem().equals(FREEHAND)){
				points.clear();
			}
			pointStart = null;
			if(rect != null){
				shapes.put(rect, selectedColour);
				rect = null;
			}
			sendInfo(shapes);
		}

		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		
		// hämta slutpositionen när användaren har flyttat muspekaren samtidigt som knappen varit nertryckt, 
		// och lägg till i points om frihandsverktyget används
		public void mouseDragged(MouseEvent e) {
			pointEnd = e.getPoint();				
			if(choose.getSelectedItem().equals(FREEHAND)){
				points.add(pointEnd);
			}
            repaint();
		}

		// hämta muspekarens position och skriv ut i statusraden
		public void mouseMoved(MouseEvent e) {
			pointEnd = e.getPoint();			
			coord.setText("Koordinater "+ pointEnd.x+", "+pointEnd.y);
		}
		
		// rita objekt på ritytan
		public void paint(Graphics g) {
            super.paint(g);
            for (Shape s : shapes.keySet()) {
            	Graphics2D g2 = (Graphics2D) g;
            	if(s.getClass().toString().contains(Line2D.class.toString())){
            		g2.setStroke(new BasicStroke(3));
            	}
            	g2.setPaint(shapes.get(s));
            	g2.draw(s);
            	g2.setStroke(new BasicStroke(1));
              }
            if (pointStart != null && choose.getSelectedItem().equals(RECTANGLE)) {
                g.setColor(selectedColour);
                Rectangle r = new Rectangle(pointStart.x, pointStart.y, pointEnd.x-pointStart.x, pointEnd.y-pointStart.y);
                g.drawRect(pointStart.x, pointStart.y, pointEnd.x-pointStart.x, pointEnd.y-pointStart.y);
                rect = r;
            }else if(pointStart != null && choose.getSelectedItem().equals(FREEHAND)){
            	for (int i = 0; i < points.size() - 1; i++){
            		Line2D lin = new Line2D.Float(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
            		shapes.put(lin, selectedColour);
            		g.setColor(selectedColour);
            		g.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
            	}
            }
        }
	}

	public static void main(String[] args) {
		try { 
	        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); 
	    }catch(Exception e){ 
	    }
		new PanelClient().setVisible(true);
	}
}
