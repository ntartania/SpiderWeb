package spiderweb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

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
	/**
	 * 
	 */
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
	
	protected JButton relaxerButton;
	protected JButton fastforwardButton;
	protected JButton forwardButton;
	protected JButton pauseButton;
	protected JButton reverseButton;
	protected JButton fastReverseButton;

	private LinkedList<LogEvent> myGraphEvolution;

	//a hidden graph that contains all the nodes that will ever be added... 
	//in order to calculate the positions of all the nodes
	private P2PNetworkGraph hiddenGraph; 

	protected EventPlayingThread eventthread;

	//Timer timer;

	boolean done;

	// for the length of the edges in the graph layout
	public static final Transformer<P2PConnection,Integer> UNITLENGTHFUNCTION = new ConstantTransformer(100);

	//the log file (default value)
	private static final String DEF_LOG_FILE = "ProcessedLog.txt";
	private static final String DEF_LOG_URL = "http://www.sce.carleton.ca/~adavoust/simuldemo/ProcessedLog.txt";
	
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


	////////////////////////////////////////////////////////////////////////////////////////

	/*  to run on a particular file name* /
	public void setLogFileName(String fname){
		logfilename = fname;
	}*/
	
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

		//create a graph, that will be the one updated during the visualization process
		//Graph<P2PVertex,P2PConnection> ig = Graphs.<P2PVertex,P2PConnection>synchronizedUndirectedGraph(new UndirectedSparseGraph<P2PVertex,P2PConnection>());//  synchronizedDirectedGraph(new DirectedSparseMultigraph<Number,Number>());

		/*ObservableGraph<P2PVertex,P2PConnection> og = new ObservableGraph<P2PVertex,P2PConnection>(ig);
		og.addGraphEventListener(new GraphEventListener<P2PVertex,P2PConnection>() {

			public void handleGraphEvent(GraphEvent<P2PVertex,P2PConnection> evt) {
				System.err.println("got "+evt);

			}});*/
		//this.visibleGraph = og;
		//TODO : see if synchronization (multi-thread safety) is necessary
		visibleGraph = new P2PNetworkGraph();

		/// load the whole list of graph events
		//logreader = new LogReaderThread(this,DEF_LOG_FILE);

		System.out.println("Reading the logs ...");
		//TODO : make it possible to load a different log file
		SpringLayout<P2PVertex,P2PConnection> sp_layout=null;
		try {
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
			String str;

			myGraphEvolution = new LinkedList<LogEvent>();
			hiddenGraph = new P2PNetworkGraph();

			///*----------set up the spring layout!!----------*///
			//create a spring layout for the hidden graph and give it my own parameters ----------
			
			sp_layout = new SpringLayout<P2PVertex,P2PConnection>(hiddenGraph, new P2PNetEdgeLengthFunction()); // here is my length calculation
			//do it with the F-R  layout
			//sp_layout = new FRLayout<P2PVertex,Number>(hiddenGraph); // here is my length calculation
			sp_layout.setSize(new Dimension(DEFWIDTH,DEFHEIGHT));
			sp_layout.setForceMultiplier(0.6); //testing this value
			//((SpringLayout<Number,Number>)layout).setRepulsionRange(50);
			sp_layout.setInitializer(new P2PVertexPlacer(sp_layout, new Dimension(DEFWIDTH,DEFHEIGHT)));
			
			//------------------------------------------------------------------------------------
			
			

			///*----------Read the file and start calculating the resulting layout !!----------*///
						
			sp_layout.initialize();
			//int count = 0;
			List<LogEvent> colouringEvents = new LinkedList<LogEvent>();
			while ((str = in.readLine()) != null) //reading lines log file
			{
				//count++;
				//if(count %100 ==0) System.out.println(count+ "lines read");
				
				LogEvent gev = new LogEvent(str);
				
				if(gev.getType().equals("query") || gev.getType().equals("queryhit") || gev.getType().equals("queryreachespeer"))
				{
					colouringEvents.add(LogEvent.createOpposingLogEvent(gev)); // add an opposing event to decolour/debold
				}
				for(int i=0;i<colouringEvents.size();i++) { //start at first element (time should increase with each index)
					if(colouringEvents.get(i).getTime() < gev.getTime()) { //add only if the event takes place before the LogEvent that was read this iteration
						myGraphEvolution.addLast(colouringEvents.get(i));
						colouringEvents.remove(i);
						i--;
					}
				}
				myGraphEvolution.addLast(gev);
				//add all the nodes to construct the new graph
				if (gev.isConstructing()){
					graphConstructionEvent(gev,hiddenGraph);
					sp_layout.step(); //do one step in changing the layout of the graph
					sp_layout.step(); //and another few
					/*sp_layout.step();
					sp_layout.step();
					sp_layout.step();*/
				}
			}//end while
		} catch (Exception e) {
			e.printStackTrace();
			
		}

		//System.out.println(myGraphEvolution);

		/*for (int i=0; i<500; i++)sp_layout.step(); // give it another few hundred steps of figuring itself out
		System.out.println("Layout finalized !!");
		
		
		//freeze  the layout
		layout = new StaticLayout<P2PVertex,Number>(hiddenGraph, sp_layout);		
*/
		
		layout = sp_layout;
		//layout = ci_layout;
		//create viewer
		vv = new VisualizationViewer<P2PVertex,P2PConnection>(layout, new Dimension(DEFWIDTH,DEFHEIGHT));

		JRootPane rp = this.getRootPane();
		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(java.awt.Color.lightGray);
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
				System.err.println("resized");
				layout.setSize(arg0.getComponent().getSize());
			}});

		getContentPane().add(vv);
		
		//timer = new Timer();
		
		System.out.println("dododo");
		
		Relaxer relaxer = new VisRunner((IterativeContext)layout);
		relaxer.stop();
		relaxer.setSleepTime(80L);
		relaxer.relax();

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
		
		getContentPane().add(south, BorderLayout.SOUTH);
		getContentPane().add(west,BorderLayout.WEST);
	}
	
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

	@Override
	public void start() {
		validate();
		//TODO : perhaps place the log reader here.
		//set timer so applet will change

		vv.repaint();
		///----------run the spring layout algorithm with the full hidden graph for a bit -------
		
	}

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
					//relaxerButton.setText("--This will be the static layout--\nsimulation will now start");
					relaxerButton.setEnabled(false);
					System.out.println("freezing layout !");
					layout = new StaticLayout<P2PVertex,P2PConnection>(hiddenGraph, layout);
	
					//change the layout we're viewing
					vv.getModel().setGraphLayout(layout);
					vv.repaint();
					//	tie the "include functions" of the viewer to the visible graph
					
					started = true;
	
					Thread.sleep(1000);
					
					//activate the pause / resume and reverse forward buttons
					
					fastReverseButton.setEnabled(true);
					reverseButton.setEnabled(true);
					pauseButton.setEnabled(true);
					forwardButton.setEnabled(true);
					fastforwardButton.setEnabled(true);
	
					System.out.println("starting activity now !");
	
				} catch (InterruptedException e) {
					e.printStackTrace();
				} //wait 5 seconds
				//these predicates say : if the considered node /edge (which will be evaluated in the context of the hiddengraph) is found in the visible graph, then show it !
				vv.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(visibleGraph));
				vv.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
	
				eventthread.start();
				
				//fastforwardButton.setText("Quick Speed");
	
	
			}
			
					
		}
	}
	
	
	class FastReverseButtonListener implements ActionListener {
		
		public void actionPerformed(ActionEvent ae) {
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
			eventthread.reverse();
		}
	
	}
	
	class PauseButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			eventthread.pause();
		}
	}
	
	class ForwardButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			eventthread.forward();
		}
	}
	
	class FastforwardButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			eventthread.fastForward();
		}
	}
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
		private int current_index = 0;
		
		public EventPlayingThread(LinkedList<LogEvent> eventlist){
			my_eventlist = eventlist;
			state = PlayState.FORWARD;
		}
		
		public boolean isForward() {
			return ((state == PlayState.FASTFORWARD) || (state == PlayState.FORWARD));
		}
		public boolean isFast() {
			return ((state == PlayState.FASTFORWARD) || (state == PlayState.FASTREVERSE));
		}
		
		public void fastReverse() {
			resumeIfPaused();
			if(state != PlayState.FASTREVERSE) {
				state = PlayState.FASTREVERSE;
				if (eventthread.getState().equals(Thread.State.TIMED_WAITING)) {
					eventthread.interrupt(); //if we were waiting for the next event, we'll just wake the thread.
				}
			}
		}
		
		public void reverse() {
			resumeIfPaused();
			if(state != PlayState.REVERSE) {
				state = PlayState.REVERSE;
				if (eventthread.getState().equals(Thread.State.TIMED_WAITING)) {
					eventthread.interrupt(); //if we were waiting for the next event, we'll just wake the thread.
				}
			}
		}

		public void fastForward(){
			resumeIfPaused();
			if(state != PlayState.FASTFORWARD) {
				state = PlayState.FASTFORWARD;
				if (eventthread.getState().equals(Thread.State.TIMED_WAITING)) {
					eventthread.interrupt(); //if we were waiting for the next event, we'll just wake the thread.
				}
			}
		}

		public void forward(){
			resumeIfPaused();
			if(state != PlayState.FORWARD) {
				state = PlayState.FORWARD;
				if (eventthread.getState().equals(Thread.State.TIMED_WAITING)) {
					eventthread.interrupt(); //if we were waiting for the next event, we'll just wake the thread.
				}
			}
		}
		
		private void resumeIfPaused() {
			if(state == PlayState.PAUSE) {
				this.resume();//it's deprecated but for now it's the easiest way of doing it.
			}
		}
		
		public void pause(){
			if(state != PlayState.PAUSE) {
				state = PlayState.PAUSE;
				this.suspend();
			}
		}
		
		/////////////////////////////////////////////////////////
		 

		/**
		 * Visualize a query
		 * @param peer
		 * @param q
		 */
		public void doQuery(int peer, int q){

			P2PVertex tofind = P2PVertex.makePeerVertex(peer);
			for (P2PVertex v : hiddenGraph.getVertices())// find the vertex "peer"
			{
				if (v.equals(tofind)) {
					v.query(q); //change state to "querying" and record which is the query
					break;
				}
			}
			vv.repaint();// update visual
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
		public void doQueryReachesPeer(int peer, int mid){

			P2PVertex Ptofind = P2PVertex.makePeerVertex(peer);
			for (P2PVertex v : hiddenGraph.getVertices())// find the vertex "peer" in the right graph
			{
				if (v.equals(Ptofind)){
					if (v.getQueryState()!=P2PVertex.QUERYING){
						v.receivingQuery(mid);//change state to "receiving a query"
						for (P2PConnection edge : hiddenGraph.getIncidentEdges(v)){
							P2PVertex otherV= hiddenGraph.getOpposite(v, edge);
							if (otherV.getQueryState()>P2PVertex.PEER && otherV.getmessageid()==mid){ //this is true if the peer is in one of the states query, getquery,answering, for the same qid
								edge.query();// the edge is passing the query
								//System.out.println("found originator");
								break;
							} //else 
								//System.out.println(otherV.getLabel() +"   "+otherV.getQueryState());
						}
						
					} else {
						v.query(mid);// we let the peer query, but set the query number to the message id 
					}
					break;
				}
			}
			vv.repaint();// update visual
		}
		/**
		 * Visualize a query reaches peer event (bold edges)
		 * @param peer
		 * @param q
		 */
		public void undoQueryReachesPeer(int peer, int mid){

			P2PVertex Ptofind = P2PVertex.makePeerVertex(peer);
			for (P2PVertex v : hiddenGraph.getVertices())// find the vertex "peer" in the right graph
			{
				if (v.equals(Ptofind)){
					if (v.getQueryState()!=P2PVertex.QUERYING){
						v.backToNormal();//change state to "receiving a query"
						for (P2PConnection edge : hiddenGraph.getIncidentEdges(v)){
							P2PVertex otherV= hiddenGraph.getOpposite(v, edge);
							if (otherV.getQueryState()>P2PVertex.PEER && otherV.getmessageid()==mid){ //this is true if the peer is in one of the states query, getquery,answering, for the same qid
								edge.backToNormal();// the edge that was passing the query
								
								break;
							}
						}
						
					} else {
						v.backToNormal();
					}
					break;
				}
			}
			vv.repaint();// update visual
		}

		/**
		 * Visualize a queryHit
		 * @param peer
		 * @param q
		 */
		public void doQueryHit(int peer, int doc){

			P2PVertex Ptofind = P2PVertex.makePeerVertex(peer);
			P2PVertex docToFind = P2PVertex.PeerPublishesDoc(peer, doc);//doc published by peer
			boolean foundpeer= false;
			boolean founddoc=false; 
			for (P2PVertex v : hiddenGraph.getVertices())// find the vertex "peer"
			{
				if (v.equals(Ptofind)){
					v.answering(); //change state to "answering"
					foundpeer=true;
					if(founddoc)break;
				}
				else if(v.equals(docToFind)){
					v.answering(); //change state to "matching doc"
					founddoc=true;
					if(foundpeer)break;
				}
			}
			vv.repaint();// update visual
		}
		
		public void decolour(int peernumber)
		{
			P2PVertex Ptofind = P2PVertex.makePeerVertex(peernumber);
			for (P2PVertex v : hiddenGraph.getVertices())// find the vertex "peer" in the right graph
			{
				if (v.equals(Ptofind)){
					v.backToNormal();
				}
			}
			vv.repaint();// update visual
		}
		
		public void decolourDoc(int peernumber, int docnumber)
		{
			P2PVertex Ptofind = P2PVertex.PeerPublishesDoc(peernumber, docnumber);
			for (P2PVertex v : hiddenGraph.getVertices())// find the vertex "peer" in the right graph
			{
				if (v.equals(Ptofind)){
					v.backToNormal();
				}
			}
			vv.repaint();// update visual
		}


		public void run() {
			System.out.println("Starting log event sequence.");

			long mytimenow = 0L;//System.currentTimeMillis();
			long nexttime;
			boolean oldDirection;
			//READING FROM CD++ LOG FILE/////////////
			while (!my_eventlist.isEmpty()) //reading lines from config file to get parameter list
			{
				oldDirection = isForward(); //used to continue loop if reversed while thread sleeping
				LogEvent evt = my_eventlist.get(current_index); //get from the front of the queue
				
				nexttime = evt.getTime();
				//System.out.println("next event at :"+nexttime);
				try{
					if (!isFast()) //fast forwarding ?
						Thread.sleep(Math.max(0,Math.abs(nexttime-mytimenow))); //wait until that event time comes up
					else 
						Thread.sleep(ffspeed);//wait for the internal "fast-forward" speed
				} catch (InterruptedException e){
					System.err.println("log event thread interrupted !");
					// but don't stop, we probably just went from real-time to fast-fwd 
				}
				if(isForward()) { //increment after thread so that if reverse was pressed while sleeping it will increment properly
					if(current_index < my_eventlist.size()-1) {
						current_index++;
					} else {
						pauseButton.doClick(); //when beginning is reached pause the playback.
					}
				} else {
					if(current_index > 0) {
						current_index--;
					} else {
						pauseButton.doClick(); //when end is reached pause the playback.
					}
				}
				if(oldDirection != isForward()) { // if the reverse direction was switched during the thread sleeping stop what was going to happen and go back
					continue; 
				}
				mytimenow = nexttime; //advance time
				
				//if the event is to modify the structure of the graph
				if (evt.isStructural()){
					graphEvent(evt,isForward(),visibleGraph,hiddenGraph);
					vv.repaint();
				} else { //other events: queries
					String what = evt.getType();
					int val1 = evt.getParam(1);
					int val2 = evt.getParam(2);
					
					if(what.equals("query")) {
						if(isForward()) {
							doQuery(val1, val2);
						} else {
							decolour(val1);
						}
					}
					else if (what.equals("unquery")) {
						if(isForward()) {
							decolour(val1);
						} else {
							doQuery(val1, val2);
						}
					}
					else if (what.equals("queryhit")) {
						if(isForward()) {
							doQueryHit(val1, val2);
						} else {
							decolour(val1);
							decolourDoc(val1, val2);
						}
					}
					else if (what.equals("unqueryhit")) {
						if(isForward()) {
							decolour(val1);
							decolourDoc(val1, val2);
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

			}//end while

		}
	}
}
