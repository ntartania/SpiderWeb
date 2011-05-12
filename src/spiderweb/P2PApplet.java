package spiderweb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
//import edu.uci.ics.jung.algorithms.layout.SpringLayout.LengthFunction;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * an applet that will display a graph using a spring layout, and as the graph changes the layout is updated.
 * @author alan
 * 
 */
public class P2PApplet extends JApplet {
	
	//[start] Static Final Attributes
	// for the length of the edges in the graph layout
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Transformer<P2PConnection,Integer> UNITLENGTHFUNCTION = new ConstantTransformer(100);

	//the log file (default value)
	private static final String DEF_LOG_FILE = "ProcessedLog.txt";
	private static final String DEF_LOG_URL = "http://www.sce.carleton.ca/~adavoust/simuldemo/ProcessedLog.txt";
	//[end]
	
	//[start] Private Variables
	private static final long serialVersionUID = 2L;
	
	//default size for the swing graphic components
	private static int DEFWIDTH = 800;
	private static int DEFHEIGHT = 600;

	private static final boolean ONWEB = true;
	
	// this boolean indicates, as ONWEB above, that we are running on the web.
	//it can be changed through the main()
	private boolean ontheweb=ONWEB;
	
	//private String logfilename=null;
	private File mylogfile =null;
	
	private P2PNetworkGraph visibleGraph = null;

	private VisualizationViewer<P2PVertex,P2PConnection> vv = null;

	private AbstractLayout<P2PVertex,P2PConnection> layout = null;
	
	private LinkedList<LogEvent> myGraphEvolution;

	//a hidden graph that contains all the nodes that will ever be added... 
	//in order to calculate the positions of all the nodes
	private P2PNetworkGraph hiddenGraph; 
	//[end]
	
	//[start] Protected Variables
	protected JButton relaxerButton;
	protected JButton fastforwardButton;
	protected JButton forwardButton;
	protected JButton pauseButton;
	protected JButton reverseButton;
	protected JButton fastReverseButton;

	protected EventPlayingThread eventthread;
	//[end]
	
	//[start] LittleGUI class
	/////////additional swing components for the file chooser, etc./////////////////////////
	// only used in non-web mode
	class LittleGUI extends JFrame implements ActionListener{
		
		private static final long serialVersionUID = 1L;
		
		JTextArea log;
		
		JScrollPane logScrollPane;
		JButton openButton;
		JFileChooser fc;
		P2PApplet myapp;
		
		//constructor
		public LittleGUI(P2PApplet app) {
			// Create the log first, because the action listeners
			// need to refer to it.
			log = new JTextArea(8,60);
			log.setMargin(new Insets(5,5,5,5));
			log.setEditable(false);
			logScrollPane = new JScrollPane(log);
			
			log.setLineWrap(true);
			
			
			myapp = app;
			//running through this main method means we're not running the applet on the web
			myapp.setOnWeb(false);

			//Create a file chooser
			fc = new JFileChooser();

			
			//Create the open button.  We use the image from the JLF
			//Graphics Repository (but we extracted it from the jar).
			openButton = new JButton("Open a File...");
			openButton.addActionListener(this);

			//For layout purposes, put the buttons in a separate panel
			JPanel buttonPanel = new JPanel(); //use FlowLayout
			buttonPanel.add(openButton);
			add(new JLabel("P2P Network Simulation Viewer"), BorderLayout.PAGE_START);
			//Add the buttons and the log to this panel.
			add(buttonPanel, BorderLayout.PAGE_END);
			log.append("Open a processed log file to get started...");
			add(logScrollPane, BorderLayout.CENTER);

			
		}

		/**
		 * handles the button to open a file 
		 */
		public void actionPerformed(ActionEvent e) {


			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();

				//use this log file
				myapp.setLogFile(file);
				log.append("\nOpening: " + file.getAbsolutePath() + ".");

				log.append("\nPlease be patient while the processed log file is read.");
				log.append("\nThis may take around 30 seconds for a 200kB file, as the graph layout is also processed at the same time.");
				log.append("\nOnce the data is loaded, a new screen will pop up. The layout of the P2P " +
						"graph will be visible, you may modify it by zooming, panning, and moving nodes around.\n" +
						"once you are happy with the layout and are ready to start the simulation, click the button at the bottom.");
				log.setCaretPosition(log.getDocument().getLength());

				/*if (args.length>0) // log file name can be input on running the applet as well
		myapp.setLogFileName(args[0]);*/
				
				//start the applet using a new thread. 
				Thread starter = new Thread(new Runnable() {
					public void run() {
						//Turn off metal's use of bold fonts
						JFrame frame = new JFrame();
						frame.setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						frame.getContentPane().add(myapp);

						myapp.init();
						myapp.start();
						frame.pack();
						//this.setVisible(false);
						frame.setVisible(true);

					}
				});
				starter.start();

			} else {
				log.append("Open command cancelled by user.");
			}
			
		}
	}
	//[end]

	//[start] Initialization
	/** to use a particular log file (can be chosen from GUI)*/
	public void setLogFile(File lf){
		mylogfile = lf;
	}
	
	/**
	 * to set if the applet is running on the web 
	 * (in which case we need to access the log file through an http connection)
	 * @param b true if the applet is running on the web (default). If not, it must be set from the main()
	 */
	public void setOnWeb(boolean b){
		ontheweb = b;	
	}

	
	/**
	 * applet initialization
	 */
	public void init() {
		//TODO : see if synchronization (multi-thread safety) is necessary
		
		//[start] Open and Read Log file, create the layout of the graph
		System.out.println("Reading the logs ...");
		//TODO : make it possible to load a different log file
		SpringLayout<P2PVertex,P2PConnection> sp_layout=null;
		try {
			//[start] Open the log file for reading
			BufferedReader in;
			if (ontheweb){ // hack : when running on SCE server I can't read the log file without opening it through this URL reader ...
				URL yahoo = new URL(DEF_LOG_URL);
				in = new BufferedReader(new InputStreamReader(yahoo.openStream()));
			} else {
				if (mylogfile == null)
					in = new BufferedReader(new FileReader(DEF_LOG_FILE));
				else{
					in = new BufferedReader(new FileReader(mylogfile));
					System.out.println("reading from the file"+mylogfile.getAbsolutePath());
				}
			}
			
			//[end] Open the log file for reading
			
			myGraphEvolution = new LinkedList<LogEvent>();
			hiddenGraph = new P2PNetworkGraph();

			//[start] set up the spring layout
			//create a spring layout for the hidden graph and give it my own parameters ----------
			
			sp_layout = new SpringLayout<P2PVertex,P2PConnection>(hiddenGraph, new P2PNetEdgeLengthFunction()); // here is my length calculation
			//do it with the F-R  layout
			//sp_layout = new FRLayout<P2PVertex,Number>(hiddenGraph); // here is my length calculation
			sp_layout.setSize(new Dimension(DEFWIDTH,DEFHEIGHT));
			sp_layout.setForceMultiplier(0.6); //testing this value
			//((SpringLayout<Number,Number>)layout).setRepulsionRange(50);
			sp_layout.setInitializer(new P2PVertexPlacer(sp_layout, new Dimension(DEFWIDTH,DEFHEIGHT)));
			
			//[end] set up the spring layout
			
			//[start]Read the file, create log events and start calculating the resulting layout
						
			sp_layout.initialize();
			String str;
			List<LogEvent> colouringEvents = new LinkedList<LogEvent>();
			List<P2PVertex> queryPeers = new LinkedList<P2PVertex>();
			myGraphEvolution.add(new LogEvent("0:start:0:0")); //a start event to know when to stop playback of a reversing graph
			while ((str = in.readLine()) != null) //reading lines log file
			{
				LogEvent gev = new LogEvent(str);
				
				//add all the nodes to construct the new graph
				if (gev.isConstructing()){
					graphConstructionEvent(gev,hiddenGraph);
					sp_layout.step(); //do one step in changing the layout of the graph
					sp_layout.step(); //and another
				}
				
				if(gev.getType().equals("query") || gev.getType().equals("queryhit"))
				{
					colouringEvents.add(LogEvent.createOpposingLogEvent(gev,2000)); // add an opposing event to decolour/debold
					if(gev.getType().equals("query")) {
						queryPeers.add(new PeerVertex(gev.getParam(1)));
					}
					
				} 
				else if(gev.getType().equals("queryreachespeer")) {
					colouringEvents.add(LogEvent.createOpposingLogEvent(gev,750));
					P2PVertex queriedPeer = new PeerVertex(gev.getParam(1));
					for(P2PVertex querySender : queryPeers) {
						if(hiddenGraph.findEdge(querySender, queriedPeer) != null) {
							LogEvent ev = new LogEvent(gev.getTime()+1,"queryedge",querySender.getKey(),queriedPeer.getKey());
							
							colouringEvents.add(ev);
							colouringEvents.add(LogEvent.createOpposingLogEvent(ev,750));
							break;
						}
					}
					
					queryPeers.add(queriedPeer);
				}
				Collections.sort(colouringEvents);
				
				
				for(Iterator<LogEvent> iter = colouringEvents.iterator(); iter.hasNext(); ) {//start at first element (time should increase with each index)
				
					LogEvent event = (LogEvent)iter.next();
					if(event.getTime() < gev.getTime() ) { //add only if the event takes place before the LogEvent that was read this iteration
						myGraphEvolution.addLast(event);
						iter.remove();
					} else {
						break; //if this Event's time is greater than gev, the rest should be too, then there is no point continuing
					}
				}
				myGraphEvolution.addLast(gev);
				
				
			}//end while
			myGraphEvolution.add(new LogEvent((myGraphEvolution.getLast().getTime())+":end:0:0")); //add an end log to know to stop the playback of the graph
			
			//[end] Read the file, create log events and start calculating the resulting layout
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		layout = sp_layout;
		//[end] Open and Read Log file, create the layout of the graph
		
		//[start] Create Visualization Viewer
		visibleGraph = new P2PNetworkGraph();
		
		vv = new VisualizationViewer<P2PVertex,P2PConnection>(layout, new Dimension(DEFWIDTH,DEFHEIGHT));

		JRootPane rp = this.getRootPane();
		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

		getContentPane().setLayout(new BorderLayout());
		//getContentPane().setBackground(java.awt.Color.lightGray);
		getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));
		//try set the size
		getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);

		// the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
		DefaultModalGraphMouse<P2PVertex,P2PConnection> gm =new DefaultModalGraphMouse<P2PVertex,P2PConnection>(); 
		vv.setGraphMouse(gm);

		//set graph rendering parameters & functions
		vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		//the vertex labeller will use the tostring method which is fine, the P2PVertex class has an appropriate toString() method implementation
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<P2PVertex>());

		//add my own vertex shape & color fill transformers
		vv.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer());
		// note :the color depends on being picked.
		
		//make the p2p edges different from the peer to doc edges 
		vv.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
		vv.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer()); //stroke width
		
		
		// P2PVertex objects also now have multiple states : we can represent which nodes are documents, picked, querying, queried, etc.
		vv.getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(vv.getPickedVertexState()));
		vv.getRenderContext().setVertexStrokeTransformer(new P2PVertexStrokeTransformer());
		vv.setForeground(Color.white);
		
		/// create the event player
		eventthread = new EventPlayingThread(myGraphEvolution);
		
		vv.addComponentListener(new ComponentAdapter() {
			
			/**
			 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
			 */
			@Override
			public void componentResized(ComponentEvent arg0) {
				super.componentResized(arg0);
				//System.err.println("resized");
				layout.setSize(arg0.getComponent().getSize());
			}});
		
		JPanel graphsPanel = new JPanel();
		
		graphsPanel.add(vv);
		//[end] Create Visualization Viewer
		
		//[start] Relaxer creation
		Relaxer relaxer = new VisRunner((IterativeContext)layout);
		relaxer.stop();
		relaxer.setSleepTime(80L);
		relaxer.relax();
		//[end] Relaxer creation
		
		//[start] Panel Layout
		//button to freeze layout, then to fast-forward
		relaxerButton = new JButton("Finalize Layout");
		relaxerButton.addActionListener(new relaxerButtonListener(relaxer)); 
				
		fastReverseButton = new JButton("<|<|");
		fastReverseButton.addActionListener(new FastReverseButtonListener()); 
		
		reverseButton = new JButton("<|");
		reverseButton.addActionListener(new ReverseButtonListener());		
		
		pauseButton = new JButton("||");
		pauseButton.addActionListener(new PauseButtonListener()); 
		
		forwardButton = new JButton("|>");
		forwardButton.addActionListener(new ForwardButtonListener());
		
		fastforwardButton = new JButton("|>|>");
		fastforwardButton.addActionListener(new FastforwardButtonListener()); 
		
		JPanel south = new JPanel();
		south.setBackground(Color.GRAY);
		
		south.add(fastReverseButton);
		south.add(reverseButton);
		south.add(pauseButton);
		south.add(forwardButton);
		south.add(fastforwardButton);
		
		fastReverseButton.setEnabled(false);
		reverseButton.setEnabled(false);
		pauseButton.setEnabled(false);
		forwardButton.setEnabled(false);
		fastforwardButton.setEnabled(false);
		
		
		GridBagLayout westLayout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		
		JPanel west = new JPanel();
		west.setBorder(BorderFactory.createTitledBorder("Options"));
		west.setBackground(Color.GRAY);
		west.setLayout(westLayout);
		JPanel p = new JPanel();
		//add little combo box to choose between the mouse picking and the mouse transforming the layout
		p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		p.setBackground(Color.GRAY);
		p.add(gm.getModeComboBox());
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
		westLayout.setConstraints(p, constraints);
		west.add(p);
		
		westLayout.setConstraints(relaxerButton, constraints);
		west.add(relaxerButton);
		
		getContentPane().add(graphsPanel,BorderLayout.CENTER);
		getContentPane().add(south, BorderLayout.SOUTH);
		getContentPane().add(west,BorderLayout.WEST);
		//[end] Panel Layout
	}
	
	@Override
	public void start() {
		validate();
		//TODO : perhaps place the log reader here.

		vv.repaint();
		///----------run the spring layout algorithm with the full hidden graph for a bit -------
		
	}
	
	public void LOG(String message) {
		System.out.println(message);
	}
	
	//[end] Initialization
	
	//[start] Structural Graph Events
	/**
	 * Limited version of graphEvent for construction a graph for layout purposes
	 * @param gev	The Log event which needs to be handled.
	 * @param g		The Graph to perform the event on.
	 */
	private void graphConstructionEvent(LogEvent gev, P2PNetworkGraph g) {
		if (gev.getType().equals("online")){
			g.addPeer(gev.getParam(1));
		} else if(gev.getType().equals("connect")){
			g.connectPeers(gev.getParam(1), gev.getParam(2));
		} else if(gev.getType().equals("publish")){
			g.addDocument(gev.getParam(2), gev.getParam(1));
		}
	}
	
	/**
	 * Handles the Log Events which affect the structure of the graph.
	 * @param gev				The Log event which needs to be handled.
	 * @param forward			<code>true</code> if play-back is playing forward.
	 * @param eventGraph		The Graph to perform the event on.
	 * @param referenceGraph	The Graph to get edge numbers from.
	 */
	private void graphEvent(LogEvent gev, boolean forward, P2PNetworkGraph eventGraph, P2PNetworkGraph referenceGraph) {
		
		if(forward) {
			if (gev.getType().equals("online")){
				eventGraph.addPeer(gev.getParam(1));
			} else if (gev.getType().equals("offline")){
				eventGraph.removePeer(gev.getParam(1));
			} else if(gev.getType().equals("connect")){
				eventGraph.connectPeers(gev.getParam(1), gev.getParam(2), referenceGraph.findPeerConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("disconnect")){
				eventGraph.disconnectPeers(gev.getParam(1), gev.getParam(2));
			} else if(gev.getType().equals("publish")){
				eventGraph.addDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("depublish")){
				eventGraph.removeDocument(gev.getParam(2), gev.getParam(1));
			}
		} else {
			if (gev.getType().equals("online")){
				eventGraph.removePeer(gev.getParam(1));
			} else if (gev.getType().equals("offline")){
				eventGraph.addPeer(gev.getParam(1));
			} else if(gev.getType().equals("connect")){
				eventGraph.disconnectPeers(gev.getParam(1), gev.getParam(2));
			} else if(gev.getType().equals("disconnect")){
				eventGraph.connectPeers(gev.getParam(1), gev.getParam(2), referenceGraph.findPeerConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("publish")){
				eventGraph.removeDocument(gev.getParam(2), gev.getParam(1));
			} else if(gev.getType().equals("depublish")){
				eventGraph.addDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
			}
		}
	}
	//[end] Structural Graph Events

	//[start] Main
	/**
	 * 
	 * to run this applet as a java application
	 * @param args optional argument : the log file to process
	 */
	public static void main(String[] args) {

		P2PApplet myapp = new P2PApplet();
		LittleGUI mygui = myapp.new LittleGUI(myapp);

		mygui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//mygui.validate();
		//mygui.setBounds(200,200,500,200);
		mygui.pack();
		mygui.setVisible(true);
		//mygui.log.append("\nho ho ho");
	}
	//[end] Main
	
	//[start] Event Playing Thread
	private enum PlayState {
		FASTREVERSE, REVERSE, PAUSE, FORWARD, FASTFORWARD;
	}

	/**
	 * an internal class extending thread, that can play the sequence of events from the log file in real time
	 * or fast forward.
	 * @author alan
	 *
	 */
	private class EventPlayingThread extends Thread {

		private static final long ffspeed = 50L; // 50 millisec between events while we're fast-forwarding
		
		private PlayState state;

		List<LogEvent> my_eventlist;
		
		private int current_index;
		
		public EventPlayingThread(LinkedList<LogEvent> eventlist){
			my_eventlist = eventlist;
			current_index = 0; 
			state = PlayState.FORWARD;
		}
		
		//[start] Playback Properties
		/**
		 * Returns whether or not the graph is playing forward or backwards.
		 * @return <code>true</code> if the Play State is forward or fast forward.
		 */
		public boolean isForward() {
			return ((state == PlayState.FASTFORWARD) || (state == PlayState.FORWARD) || (state == PlayState.PAUSE));
		}
		/**
		 * Returns whether or not the graph is playing fast
		 * @return <code>true</code> if the Play State is fast in forward or reverse.
		 */
		public boolean isFast() {
			return ((state == PlayState.FASTFORWARD) || (state == PlayState.FASTREVERSE));
		}
		/**
		 * 
		 * @return
		 */
		public boolean atFront() {
			if(current_index <= 0) {
				return true;
			}
			return false;
		}
		/**
		 * 
		 * @return
		 */
		public boolean atBack() {
			if (current_index >= my_eventlist.size()-1) {
				return true;
			}
			return false;
		}
		/**
		 * 
		 * @return
		 */
		public boolean atAnEnd() {
			if(my_eventlist.get(current_index).getType().equals("end") || my_eventlist.get(current_index).getType().equals("start")) {
				return true;
			}
			return false;
		}
		//[end]
		
		//[start] State Change handlers for button clicks.
		/**
		 * 
		 */
		public void fastReverse() {
			if(state != PlayState.FASTREVERSE) {
				PlayState prevState = state;
				state = PlayState.FASTREVERSE;
				wakeup(prevState);
			}
		}
		
		public void reverse() {
			if(state != PlayState.REVERSE) {
				PlayState prevState = state;
				state = PlayState.REVERSE;
				wakeup(prevState);
			}
		}

		public void fastForward(){
			if(state != PlayState.FASTFORWARD) {
				PlayState prevState = state;
				state = PlayState.FASTFORWARD;
				wakeup(prevState);
			}
		}

		public void forward(){
			if(state != PlayState.FORWARD) {
				PlayState prevState = state;
				state = PlayState.FORWARD;
				wakeup(prevState);
			}
		}
		
		private synchronized void wakeup(PlayState previousState) {
			if (this.getState().equals(Thread.State.TIMED_WAITING)) {
				interrupt(); //if we were waiting for the next event, we'll just wake the thread.
			}
			if(previousState == PlayState.PAUSE) {
				notify();
			}
		}
		
		public synchronized void pause(){
			if(state != PlayState.PAUSE) {
				state = PlayState.PAUSE;
				notify();
			}
		}
		//[end]
				 
		//[start] Graph Editors for highlighting and changing colours
		/**
		 * Visualize a query
		 * @param peer
		 * @param q
		 */
		public void doQuery(int peer, int queryMessageID){
			hiddenGraph.getPeer(peer).query(queryMessageID);
		}
		
		/**
		 * Visualize a query
		 * @param peer
		 * @param q
		 */
		public void undoQuery(int peer, int queryMessageID){
			hiddenGraph.getPeer(peer).endQuery(queryMessageID);
		}
		
		
		public void doQueryEdge(int peerFrom, int peerTo) {
			hiddenGraph.findPeerConnection(peerFrom, peerTo).query();
		}
		
		public void undoQueryEdge(int peerFrom, int peerTo) {
			hiddenGraph.findPeerConnection(peerFrom, peerTo).backToNormal();
		}
		
		/**
		 * Visualize a query reaches peer event (bold edges)
		 * @param peer
		 * @param q
		 */
		public void doQueryReachesPeer(int peer, int queryMessageID){
			hiddenGraph.getPeer(peer).receiveQuery(queryMessageID);
		}
		/**
		 * Visualize a query reaches peer event (bold edges)
		 * @param peer
		 */
		public void undoQueryReachesPeer(int peer, int queryMessageID){
			hiddenGraph.getPeer(peer).endReceivedQuery(queryMessageID);
		}

		/**
		 * Visualize a queryHit
		 * @param peer
		 * @param q
		 */
		public void doQueryHit(int peerNumber, int documentNumber) {
			hiddenGraph.getDocument(peerNumber, documentNumber).setQueryHit(true);
		}
		
		/**
		 * Visualize a queryHit
		 * @param peer
		 * @param q
		 */
		public void undoQueryHit(int peerNumber, int documentNumber) {
			hiddenGraph.getDocument(peerNumber, documentNumber).setQueryHit(false);
		}
		//[end]


		public void run() {
			System.out.println("Starting log event sequence.");

			long mytimenow = 0L;//System.currentTimeMillis();
			long nexttime;
			boolean oldDirection;
			boolean firstRun = true;
			//READING FROM CD++ LOG FILE/////////////
			while (!my_eventlist.isEmpty()) //reading lines from config file to get parameter list
			{
				oldDirection = isForward(); //used to continue loop if reversed while thread sleeping
				LogEvent evt = my_eventlist.get(current_index); //get from the front of the queue
				
				nexttime = evt.getTime();
				
				if(atAnEnd()) {
					if(!firstRun){
						pauseButton.doClick();
					} else {
						firstRun=false;
					}
				}
				
				try{
					if (!isFast()) //fast forwarding ?
						Thread.sleep(Math.max(0,Math.abs(nexttime-mytimenow))); //wait until that event time comes up
					else 
						Thread.sleep(ffspeed);//wait for the internal "fast-forward" speed
					if(state == PlayState.PAUSE)
					{
						synchronized(this) {
							while(state == PlayState.PAUSE) {
								wait();
							}
						}
					}
				} catch (InterruptedException e){
					//System.err.println("log event thread interrupted !");
					// but don't stop, we probably just went from real-time to fast-fwd 
				}
				
				if(isForward()) { //increment after thread so that if reverse was pressed while sleeping it will increment properly
					current_index++;
				} else {
					current_index--;
				}
				if(oldDirection != isForward()) { // if the reverse direction was switched during the thread sleeping stop what was going to happen and go back
					continue; 
				}
				mytimenow = nexttime; //advance time
				
				//[start] Graph Event Handling
				//if the event is to modify the structure of the graph
				if (evt.isStructural()){
					graphEvent(evt,isForward(),visibleGraph,hiddenGraph);
				} else { //other events: queries
					String what = evt.getType();
					int val1 = evt.getParam(1);
					int val2 = evt.getParam(2);
					if(what.equals("query")) {
						if(isForward()) {
							doQuery(val1, val2);
						} else {
							undoQuery(val1,val2);
						}
					}
					else if (what.equals("unquery")) {
						if(isForward()) {
							undoQuery(val1,val2);
						} else {
							doQuery(val1, val2);
						}
					}
					else if (what.equals("queryhit")) {
						if(isForward()) {
							doQueryHit(val1, val2);
						} else {
							undoQueryHit(val1, val2);
						}
					}
					else if (what.equals("unqueryhit")) {
						if(isForward()) {
							undoQueryHit(val1, val2);
						} else {
							doQueryHit(val1, val2);
						}
					}
					else if (what.equals("queryreachespeer")) {
						if(isForward()) {
							doQueryReachesPeer(val1,val2);
						} else {
							undoQueryReachesPeer(val1,val2);
						}
					}
					else if (what.equals("unqueryreachespeer")) {
						if(isForward()) {
							undoQueryReachesPeer(val1,val2);
						} else {
							doQueryReachesPeer(val1,val2);
						}
					}
					else if (what.equals("queryedge")) {
						if(isForward()) {
							doQueryEdge(val1,val2);
						} else {
							undoQueryEdge(val1,val2);
						}
					}
					else if (what.equals("unqueryedge")) {
						if(isForward()) {
							undoQueryEdge(val1,val2);
						} else {
							doQueryEdge(val1,val2);
						}
					}
				}
				//[end] Graph Event Handling
				vv.repaint();// update visual
				
			}//end while

		}
	}
	//[end] Event Playing Thread

	//[start] Button Listeners
	 /**
	 * an actionlistener that defines the use of the button at the bottom of the applet 
	 * @author adavoust
	 *
	 */
	class relaxerButtonListener implements ActionListener {
		
		//this is the SpringLayout relaxer for the beginning
		private Relaxer relaxer;
		boolean started;
		
		public relaxerButtonListener (Relaxer rr){ // the relaxer is the thread that's doing the dynamic layout.
										  // we need to stop it then switch to a static layout,
										  // then show the full graph layout for one sec then make it hidden
			this.relaxer = rr;
			started = false; // says if we've passed the initial problem of freezing the layout and getting started
			
		}
		/**
		 * handles the button : first to freeze the layout, then to toggle between fast-forward and normal speed
		 */
		public void actionPerformed(ActionEvent ae) {

			if(!started){ // this will be the first button task : freeze layout and start the simulation
				try {
					relaxer.stop();
					relaxerButton.setText("Layout Complete");
					relaxerButton.setEnabled(false);
					//System.out.println("freezing layout !");
					layout = new StaticLayout<P2PVertex,P2PConnection>(hiddenGraph, layout);
	
					//change the layout we're viewing
					vv.getModel().setGraphLayout(layout);
					vv.repaint();
					//	tie the "include functions" of the viewer to the visible graph
					
					started = true;
	
					Thread.sleep(1000);
					
					fastReverseButton.setEnabled(true);
					reverseButton.setEnabled(true);
					pauseButton.setEnabled(true);
					forwardButton.setEnabled(false);
					fastforwardButton.setEnabled(true);
	
					//System.out.println("starting activity now !");
	
				} catch (InterruptedException e) {
					e.printStackTrace();
				} //wait 5 seconds
				//these predicates say : if the considered node /edge (which will be evaluated in the context of the hiddengraph) is found in the visible graph, then show it !
				vv.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(visibleGraph));
				vv.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
	
				eventthread.start();
			}		
		}
	}
	
	class FastReverseButtonListener implements ActionListener {
		
		public void actionPerformed(ActionEvent ae) {
			fastReverseButton.setEnabled(false);
			reverseButton.setEnabled(true);
			pauseButton.setEnabled(true);
			forwardButton.setEnabled(true);
			fastforwardButton.setEnabled(true);
			eventthread.fastReverse();
		}
	}
	
	/**
	 * An ActionListener that defines the action of the reverse button for the applet
	 * @author Matthew
	 * @version May 2011
	 */
	class ReverseButtonListener implements ActionListener {
		
		/**
		 * Method called when the reverse button has an action performed(clicked)
		 * Tells eventthread to traverse the graph placement in reverse.
		 * @param ae	The ActionEvent that triggered the listener
		 */
		public void actionPerformed(ActionEvent ae) {
			fastReverseButton.setEnabled(true);
			reverseButton.setEnabled(false);
			pauseButton.setEnabled(true);
			forwardButton.setEnabled(true);
			fastforwardButton.setEnabled(true);
			eventthread.reverse();
		}
	
	}
	
	class PauseButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			if(eventthread.atFront()) {
				fastReverseButton.setEnabled(false);
				reverseButton.setEnabled(false);
			} else {
				fastReverseButton.setEnabled(true);
				reverseButton.setEnabled(true);
			}
			pauseButton.setEnabled(false);
			if(eventthread.atBack()) {
				forwardButton.setEnabled(false);
				fastforwardButton.setEnabled(false);
			} else {
				forwardButton.setEnabled(true);
				fastforwardButton.setEnabled(true);
			}
			
			eventthread.pause();
		}
	}
	
	class ForwardButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			fastReverseButton.setEnabled(true);
			reverseButton.setEnabled(true);
			pauseButton.setEnabled(true);
			forwardButton.setEnabled(false);
			fastforwardButton.setEnabled(true);
			eventthread.forward();
		}
	}
	
	class FastforwardButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			fastReverseButton.setEnabled(true);
			reverseButton.setEnabled(true);
			pauseButton.setEnabled(true);
			forwardButton.setEnabled(true);
			fastforwardButton.setEnabled(false);
			
			eventthread.fastForward();
		}
	}
	//[end] Button Listeners
}