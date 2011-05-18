package spiderweb;

//[start] Imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
//[end] Imports

/**
 * an applet that will display a graph using a spring layout, and as the graph changes the layout is updated.
 * @author Alan
 * @author Matt
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
	public static final int DEFWIDTH = 800;
	public static final int DEFHEIGHT = 600;

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
	
	private List<LoadingListener> loadingListeners;
	
	//[end] Private Variables
	
	//[start] Protected Variables
	protected JButton relaxerButton;
	protected JButton fastforwardButton;
	protected JButton forwardButton;
	protected JButton pauseButton;
	protected JButton reverseButton;
	protected JButton fastReverseButton;
	protected JButton stopButton;
	
	protected JSlider playbackSlider;

	protected EventPlayingThread eventthread;
	//[end]
	
	//[start] Constructor
	public P2PApplet(boolean onWeb) {
		ontheweb = onWeb;
		if(!onWeb) {
			LogPanelWithFileChooser mygui = new LogPanelWithFileChooser(this);
	
			mygui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//mygui.validate();
			//mygui.setBounds(200,200,500,200);
			mygui.pack();
			mygui.setVisible(true);
			loadingListeners = new LinkedList<LoadingListener>();
			loadingListeners.add(mygui);
		}
	}
	//[end] Constructor

	//[start] Initialization
	
	//[start] setters for when on the web
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
	//[end] setters for when on the web
	
	//[start] Calculate the Spring Layout
	/**
	 * Helper Method for setting up the spring layout.
	 * @return The initialized spring layout.
	 */
	private SpringLayout<P2PVertex,P2PConnection> springLayoutInit(List<LogEvent> graphEvents) {
		final int numSteps = 100;
		for(LoadingListener l : loadingListeners) {
			l.loadingChanged(numSteps, "Spring Layout");
		}
		SpringLayout<P2PVertex,P2PConnection> sp_layout=null;
		sp_layout = new SpringLayout<P2PVertex,P2PConnection>(hiddenGraph, new P2PNetEdgeLengthFunction()); // here is my length calculation
		//do it with the F-R  layout
		//sp_layout = new FRLayout<P2PVertex,Number>(hiddenGraph); // here is my length calculation
		sp_layout.setSize(new Dimension(DEFWIDTH,DEFHEIGHT));
		sp_layout.setForceMultiplier(0.6); //testing this value
		//((SpringLayout<Number,Number>)layout).setRepulsionRange(50);
		sp_layout.setInitializer(new P2PVertexPlacer(sp_layout, new Dimension(DEFWIDTH,DEFHEIGHT)));
		
		sp_layout.initialize();
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(1);
		}
		
		for(int j =1;j<=numSteps;j++) {
			sp_layout.step();
			for(LoadingListener l : loadingListeners) {
				l.loadingProgress(j);
			}
		}
		return sp_layout;
	}
	//[end] Calculate the Spring Layout

	//[start] Open the log file for reading
	/**
	 * Helper Method for opening the Log File.
	 * @return The Log File for reading.
	 */
	private BufferedReader openLogFile() {
		BufferedReader in;
		try {
			if (ontheweb){ // hack : when running on SCE server I can't read the log file without opening it through this URL reader ...
				URL yahoo = new URL(DEF_LOG_URL);
				in = new BufferedReader(new InputStreamReader(yahoo.openStream()));
			} else {
				if (mylogfile == null) {
					in = new BufferedReader(new FileReader(DEF_LOG_FILE));
				} else{
					in = new BufferedReader(new FileReader(mylogfile));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return in;
	}
	//[end] Open the log file for reading
	
	//[start] Build the list of LogEvents
	/**
	 * 
	 * @param logFile The file for which to read from 
	 * @return
	 */
	private LinkedList<LogEvent> logEventBuilder(BufferedReader logFile) {
		LinkedList<LogEvent> logEvents = new LinkedList<LogEvent>();
		try {
			String str;
			List<LogEvent> colouringEvents = new LinkedList<LogEvent>();
			List<P2PVertex> queryPeers = new LinkedList<P2PVertex>();
			
			int totalLines = Integer.parseInt(logFile.readLine());
			int lineCount = 0;
			
			for(LoadingListener l : loadingListeners) {
				l.loadingChanged(totalLines, "LogEvents");
			}
			
			logEvents.add(new LogEvent("0:start:0:0")); //a start event to know when to stop playback of a reversing graph
			while ((str = logFile.readLine()) != null) //reading lines log file
			{
				lineCount++;
				LogEvent gev = new LogEvent(str);
				
				if (gev.isConstructing()){
					graphConstructionEvent(gev,hiddenGraph);
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
						logEvents.addLast(event);
						iter.remove();
					} else {
						break; //if this Event's time is greater than gev, the rest should be too, then there is no point continuing
					}
				}
				logEvents.addLast(gev);
				for(LoadingListener l : loadingListeners) {
					l.loadingProgress(lineCount);
				}
				
			}//end while
			logEvents.add(new LogEvent((logEvents.getLast().getTime())+":end:0:0")); //add an end log to know to stop the playback of the graph
		} catch(IOException e) {
			e.printStackTrace();
		}
		return logEvents;
	}
	//[end] Build the list of LogEvents
	
	//[start] Create the visualization viewer
	/**
	 * 
	 * @return The Initialized Visualization Viewer
	 */
	private VisualizationViewer<P2PVertex,P2PConnection> visualizationViewerInit(final Layout<P2PVertex,P2PConnection>  layout, int width, int height) {
		VisualizationViewer<P2PVertex,P2PConnection> viewer = new VisualizationViewer<P2PVertex,P2PConnection>(layout, new Dimension(width,height));

		JRootPane rp = this.getRootPane();
		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

		getContentPane().setLayout(new BorderLayout());
		//getContentPane().setBackground(java.awt.Color.lightGray);
		getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));
		//try set the size
		getContentPane().setBounds(0, 0, width, height);

		// the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
		DefaultModalGraphMouse<P2PVertex,P2PConnection> gm =new DefaultModalGraphMouse<P2PVertex,P2PConnection>(); 
		viewer.setGraphMouse(gm);

		//set graph rendering parameters & functions
		viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		//the vertex labeler will use the tostring method which is fine, the P2PVertex class has an appropriate toString() method implementation
		viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<P2PVertex>());

		//add my own vertex shape & color fill transformers
		viewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer());
		// note :the color depends on being picked.
		
		//make the p2p edges different from the peer to doc edges 
		viewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
		viewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer()); //stroke width
		
		
		// P2PVertex objects also now have multiple states : we can represent which nodes are documents, picked, querying, queried, etc.
		viewer.getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(viewer.getPickedVertexState()));
		viewer.getRenderContext().setVertexStrokeTransformer(new P2PVertexStrokeTransformer());
		viewer.setForeground(Color.white);
		
		viewer.addComponentListener(new ComponentAdapter() {
			
			/**
			 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
			 */
			@Override
			public void componentResized(ComponentEvent arg0) {
				super.componentResized(arg0);
				//System.err.println("resized");
				layout.setSize(arg0.getComponent().getSize());
			}
		});
		
		return viewer;
	}
	//[end] Create the visualization viewer
	
	//[start] Create Components
	
	//[start] South Panel
	/**
	 * Helper Method for initializing the Buttons and slider for the South Panel.
	 * @return The South Panel, laid out properly, to be displayed.
	 */
	private JPanel initializeSouthPanel() {
		for(LoadingListener l : loadingListeners) {
			l.loadingChanged(9, "South Panel");
		}
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(1);
		}
		
		fastReverseButton = new JButton("<|<|");
		fastReverseButton.addActionListener(new FastReverseButtonListener()); 
		fastReverseButton.setEnabled(false);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(2);
		}
		
		reverseButton = new JButton("<|");
		reverseButton.addActionListener(new ReverseButtonListener());
		reverseButton.setEnabled(false);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(3);
		}
		
		pauseButton = new JButton("||");
		pauseButton.addActionListener(new PauseButtonListener()); 
		pauseButton.setEnabled(false);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(4);
		}
		
		forwardButton = new JButton("|>");
		forwardButton.addActionListener(new ForwardButtonListener());
		forwardButton.setEnabled(false);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(5);
		}
		
		fastforwardButton = new JButton("|>|>");
		fastforwardButton.addActionListener(new FastforwardButtonListener());
		fastforwardButton.setEnabled(false);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(6);
		}
		
		playbackSlider = new JSlider(JSlider.HORIZONTAL,0,(int)myGraphEvolution.getLast().getTime(),0);
		SliderListener s = new SliderListener();
		playbackSlider.addChangeListener(s);
		playbackSlider.addMouseListener(s);
		playbackSlider.setMajorTickSpacing(playbackSlider.getExtent()/4);
		playbackSlider.setMajorTickSpacing(playbackSlider.getExtent()/8);
		playbackSlider.setPaintTicks(true);
		playbackSlider.setPaintLabels(false);
		playbackSlider.setBackground(Color.GRAY);
		playbackSlider.setEnabled(false);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(7);
		}
		
		GridBagLayout southLayout = new GridBagLayout();
		GridBagConstraints southConstraints = new GridBagConstraints();
		
		JPanel south = new JPanel();
		south.setBackground(Color.GRAY);
		south.setLayout(new GridLayout(2,1));
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(Color.GRAY);
		buttonPanel.setLayout(southLayout);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(8);
		}
		
		buttonPanel.add(fastReverseButton);
		buttonPanel.add(reverseButton);
		buttonPanel.add(pauseButton);
		buttonPanel.add(forwardButton);
		southConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
		southLayout.setConstraints(fastforwardButton, southConstraints);
		buttonPanel.add(fastforwardButton);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(9);
		}
		
		south.add(buttonPanel);
		south.add(playbackSlider);
		
		return south;
	}
	//[end] South Panel
	
	//[start] West Panel
	/**
	 * Helper Method for initializing the Buttons and drop down menu for the West Panel.
	 * @return The west Panel, laid out properly, to be displayed.
	 */
	private JPanel initializeWestPanel() {
		for(LoadingListener l : loadingListeners) {
			l.loadingChanged(8, "West Panel");
		}
		
		//[start] Relaxer creation
		Relaxer relaxer = new VisRunner((IterativeContext)layout);
		relaxer.stop();
		relaxer.setSleepTime(80L);
		relaxer.relax();
		//[end] Relaxer creation
		relaxerButton = new JButton("Finalize Layout");
		relaxerButton.addActionListener(new relaxerButtonListener(relaxer)); 
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(1);
		}
		

		stopButton = new JButton("Stop Playback");
		stopButton.addActionListener(new StopButtonListener());
		stopButton.setEnabled(false);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(2);
		}
		
		GridBagLayout westLayout = new GridBagLayout();
		GridBagConstraints westConstraints = new GridBagConstraints();
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(3);
		}
		
		JPanel west = new JPanel();
		west.setBorder(BorderFactory.createTitledBorder("Options"));
		west.setBackground(Color.GRAY);
		west.setLayout(westLayout);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(4);
		}
		
		JPanel p = new JPanel();
		//add little combo box to choose between the mouse picking and the mouse transforming the layout
		p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		p.setBackground(Color.GRAY);
		p.add(((DefaultModalGraphMouse<P2PVertex,P2PConnection>)vv.getGraphMouse()).getModeComboBox());
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(5);
		}
		
		westConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
		westLayout.setConstraints(p, westConstraints);
		west.add(p);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(6);
		}
		
		westLayout.setConstraints(relaxerButton, westConstraints);
		west.add(relaxerButton);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(7);
		}
		
		westLayout.setConstraints(stopButton, westConstraints);
		west.add(stopButton);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(8);
		}
		
		return west;
	}
	//[end] West Panel
	
	//[end] Create Components
	
	/**
	 * applet initialization
	 */
	public void init() {
		for(LoadingListener l : loadingListeners) {
			l.loadingStarted(1, "Graphs");
		}
		hiddenGraph = new P2PNetworkGraph();
		visibleGraph = new P2PNetworkGraph();
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(1);
		}
		
		myGraphEvolution = logEventBuilder(openLogFile());
		
		layout = springLayoutInit(myGraphEvolution);
		
		/// create the event player
		eventthread = new EventPlayingThread(myGraphEvolution);
		
		vv = visualizationViewerInit(layout,DEFWIDTH,DEFHEIGHT);
		
		JPanel graphsPanel = new JPanel();
		
		graphsPanel.add(vv);
		
		getContentPane().add(graphsPanel,BorderLayout.CENTER);
		getContentPane().add(initializeSouthPanel(), BorderLayout.SOUTH);
		getContentPane().add(initializeWestPanel(),BorderLayout.WEST);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingComplete();
		}
	}
	
	@Override
	public void start() {
		validate();

		vv.repaint();
		///----------run the spring layout algorithm with the full hidden graph for a bit -------
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

		@SuppressWarnings("unused")
		P2PApplet myapp = new P2PApplet(false); 
	}
	//[end] Main
	
	//[start] Event Playing Thread
	/**
	 * an internal class extending thread, that can play the sequence of events from the log file in real time
	 * or fast forward.
	 * @author alan
	 *
	 */
	public class EventPlayingThread extends Thread {
		
		private Timer schedule;
		private TimeCounter timeCounter;
		
		private static final int speed = 33; // 33 millisec between events while playing regularly
		private static final int ffMultiplier = 10;
		
		private PlayState state;
		private boolean playing;

		private List<LogEvent> my_eventlist;
		
		private int current_index;
		
		public EventPlayingThread(LinkedList<LogEvent> eventlist){
			playing = true;
			my_eventlist = eventlist;
			current_index = 0; 
			state = PlayState.FORWARD;
			//currentTime = 0;
			timeCounter = new TimeCounter(speed,0,eventlist.getFirst().getTime(),eventlist.getLast().getTime());
		}
		
		//[start] Playback Properties
		/**
		 * Returns whether or not the graph is playing forward or backwards.
		 * @return <code>true</code> if the Play State is forward or fast forward.
		 */
		public boolean isForward() {
			return ((state == PlayState.FASTFORWARD) || (state == PlayState.FORWARD));
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
			if(timeCounter.getTime() == timeCounter.getLowerBound()) {
				return true;
			}
			return false;
		}
		/**
		 * 
		 * @return
		 */
		public boolean atBack() {
			if (timeCounter.getTime() == timeCounter.getUpperBound()) {
				return true;
			}
			return false;
		}
		/**
		 * 
		 * @return
		 */
		public boolean atAnEnd() {
			if(atFront() || atBack()) {
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
				fastReverseButton.setEnabled(false);
				reverseButton.setEnabled(true);
				pauseButton.setEnabled(true);
				forwardButton.setEnabled(true);
				fastforwardButton.setEnabled(true);
				
				PlayState prevState = state;
				state = PlayState.FASTREVERSE;
				timeCounter.setIncrement(-speed*ffMultiplier);
				wakeup(prevState);
			}
		}
		
		public void reverse() {
			if(state != PlayState.REVERSE) {
				fastReverseButton.setEnabled(true);
				reverseButton.setEnabled(false);
				pauseButton.setEnabled(true);
				forwardButton.setEnabled(true);
				fastforwardButton.setEnabled(true);
				
				PlayState prevState = state;
				state = PlayState.REVERSE;
				timeCounter.setIncrement(-speed);
				wakeup(prevState);
			}
		}

		public void fastForward(){
			if(state != PlayState.FASTFORWARD) {
				
				fastReverseButton.setEnabled(true);
				reverseButton.setEnabled(true);
				pauseButton.setEnabled(true);
				forwardButton.setEnabled(true);
				fastforwardButton.setEnabled(false);
				
				PlayState prevState = state;
				state = PlayState.FASTFORWARD;
				timeCounter.setIncrement(speed*ffMultiplier);
				wakeup(prevState);
			}
		}

		public void forward(){
			if(state != PlayState.FORWARD) {
				fastReverseButton.setEnabled(true);
				reverseButton.setEnabled(true);
				pauseButton.setEnabled(true);
				forwardButton.setEnabled(false);
				fastforwardButton.setEnabled(true);
				
				PlayState prevState = state;
				state = PlayState.FORWARD;
				timeCounter.setIncrement(speed);
				wakeup(prevState);
			}
		}
		
		private synchronized void wakeup(PlayState previousState) {
			if(previousState == PlayState.PAUSE) {
				if(atAnEnd()) {
					timeCounter.doIncrement();
				}
				schedule.start();
				notify();
			}
			if (this.getState().equals(Thread.State.TIMED_WAITING)) {
				interrupt(); //if we were waiting for the next event, we'll just wake the thread.
			}
			
		}
		
		public synchronized void pause(){
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
			if(state != PlayState.PAUSE) {
				
				state = PlayState.PAUSE;
				notify();
				schedule.stop();
				vv.repaint();
			}
		}
		
		public void goToTime(int value) {
			PlayState prevState = state;
			
			if(value < timeCounter.getTime()) {
				state = PlayState.REVERSE;
			}
			else {
				state = PlayState.FORWARD;
			}
			
			for( LogEvent evt : getLogEventsUntil(value) ) {
				handleLogEvent(evt);
			}
			vv.repaint();
			
			timeCounter.setTime(value);
			state = prevState;
		}
		
		public void stopPlayback() {
			playing = false;
			wakeup(state);
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
			//System.out.println("Starting log event sequence.");

			long myTimeNow = 0L;//System.currentTimeMillis();
			long nextTime;
			boolean oldDirection;
			//READING FROM CD++ LOG FILE/////////////
			
			schedule = new Timer(speed,timeCounter);
			schedule.start();
			
			while (playing) //reading lines from config file to get parameter list
			{
				if(timeCounter.getTime() == myTimeNow) {
					try {
						Thread.sleep(20);
						continue;
					} catch (InterruptedException e) { }
				}
				
				if(state != PlayState.PAUSE && !playbackSlider.getValueIsAdjusting()) {
					nextTime = timeCounter.getTime();
					oldDirection = isForward();
					if(atAnEnd()) {
						pause();
					}
					
					for( LogEvent evt : getLogEventsUntil(nextTime) ) {
						if(oldDirection==isForward()) { 
							handleLogEvent(evt);
						} else {//if the playback direction changed while getting the events
							break;
						}
					}
					myTimeNow = nextTime; //advance time
					//playbackSlider.setValueIsAdjusting(true);
					playbackSlider.setValue((int)myTimeNow);
					//playbackSlider.setValueIsAdjusting(false);
					vv.repaint();// update visual
				}				
				else {
					try {
						synchronized(this) {
							while (state == PlayState.PAUSE) {
								wait();
							}
						}
					} catch (InterruptedException e) { }
				}
				
			}//end while

		}
		
		//[start] Graph Event Getting & Handling
		/**
		 * current_index is always the next event with time greater than the simulation time.
		 * 
		 * if current index is 3, simulation time (represented by '|') will be less than the index.
		 * [0]-[1]-[2]-[3]-[4]-[5]-[6]
		 *            |
		 * 
		 * @param timeGoingTo The simulation time (in milliseconds) to play events up to.
		 * @return	The list of log events which need to be taken care of for this time span.
		 */
		private List<LogEvent> getLogEventsUntil(long timeGoingTo) {
			List<LogEvent> events = new LinkedList<LogEvent>();
			//System.out.println(current_index+", "+timeGoingTo);
			LogEvent evt;
			if(isForward()) {
				evt = my_eventlist.get(current_index);
				while(evt.getTime() < timeGoingTo) {
					current_index++;
					if(current_index >= my_eventlist.size()) {
						current_index = my_eventlist.size()-1;
						break;
					}
					events.add(evt);
					evt = my_eventlist.get(current_index);
										
				}
			}
			else {
				evt = my_eventlist.get(current_index-1);
				while(evt.getTime() > timeGoingTo) {
					current_index--;
					if(current_index < 1) {
						break;
					}
					
					events.add(evt);
					evt = my_eventlist.get(current_index-1);
					
				}
			}
			return events;
		}
		
		/**
		 * Handles the passed LogEvent be it structural or visual.
		 * @param evt The Log event to handle.
		 */
		private void handleLogEvent(LogEvent evt) {
			if (evt.isStructural()){ //if the event is to modify the structure of the graph
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
		}
		//[end] Graph Event Handling
	}
	//[end] Event Playing Thread

	//[start] Swing Event Listeners
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
					
					stopButton.setEnabled(true);
					fastReverseButton.setEnabled(true);
					reverseButton.setEnabled(true);
					pauseButton.setEnabled(true);
					forwardButton.setEnabled(false);
					fastforwardButton.setEnabled(true);
					playbackSlider.setEnabled(true);
	
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
	
	class StopButtonListener implements ActionListener {
		
		public void actionPerformed(ActionEvent ae) {
			fastReverseButton.setEnabled(false);
			reverseButton.setEnabled(false);
			pauseButton.setEnabled(false);
			forwardButton.setEnabled(false);
			fastforwardButton.setEnabled(false);
			playbackSlider.setEnabled(false);
			eventthread.stopPlayback();
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
	
	class SliderListener implements ChangeListener, MouseListener {

		PlayState prevState = PlayState.PAUSE;
		
		@Override
		public void stateChanged(ChangeEvent ce) {
			
			JSlider source = (JSlider)ce.getSource();
			//eventthread.schedule.stop();
			eventthread.goToTime(source.getValue());
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			prevState = eventthread.state;
			eventthread.pause();
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if(prevState == PlayState.FASTREVERSE) {
				eventthread.fastReverse();
			}
			else if (prevState == PlayState.REVERSE) {
				eventthread.reverse();
			}
			else if (prevState == PlayState.FORWARD) {
				eventthread.forward();
			}
			else if (prevState == PlayState.FASTFORWARD) {
				eventthread.fastForward();
			}
			else if (prevState == PlayState.PAUSE) {
				eventthread.pause();
			}
			
		}
	}
	//[end] Swing Event Listeners
}