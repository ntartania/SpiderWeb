package spiderweb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
	private static int DEFWIDTH = 1200;
	private static int DEFHEIGHT = 800;

	private static final boolean ONWEB = true;
	
	// this boolean indicates, as ONWEB above, that we are running on the web.
	//it can be changed through the main()
	private boolean ontheweb=ONWEB;
	
	//private String logfilename=null;
	private File mylogfile =null;
	
	private P2PNetworkGraph visibleGraph = null;

	private VisualizationViewer<P2PVertex,P2PConnection> vv = null;

	private AbstractLayout<P2PVertex,P2PConnection> layout = null;

	private int edgecounter; // just a counter so that I don't try to add twice the same edge number.

	protected JButton fastforward;
	protected JButton pausebutton;

	private LinkedList<LogEvent> myGraphEvolution;

	//a hidden graph that contains all the nodes that will ever be added... 
	//in order to calculate the positions of all the nodes
	private P2PNetworkGraph hiddenGraph; 

	protected EventPlayingThread eventthread;

	Timer timer;

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
		public LittleGUI(P2PApplet app){
			//Create the log first, because the action listeners
			//need to refer to it.
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

		edgecounter = 0;

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
		//TODO : make it possible to laod a different log file
		SpringLayout<P2PVertex,P2PConnection> sp_layout=null;
		try {
			BufferedReader in;
			if (ontheweb){ // hack : when running on SCE server I can't read the log file without opening it through this URL reader ...
				URL yahoo = new URL(DEF_LOG_URL);
				in = new BufferedReader(new InputStreamReader(yahoo.openStream()));
				}
			else
				if (mylogfile == null)
					in = new BufferedReader(new FileReader(DEF_LOG_FILE));
				else{
					in = new BufferedReader(new FileReader(mylogfile));
					System.out.println("reading from the file"+mylogfile.getAbsolutePath());
				}
			
			String str;

			myGraphEvolution = new LinkedList<LogEvent>();
			hiddenGraph = new P2PNetworkGraph();

			/*calculate the resulting layout !!*/
			//create a spring layout for the hidden graph and give it my own parameters ----------
			
			
			sp_layout = new SpringLayout<P2PVertex,P2PConnection>(hiddenGraph, new P2PNetEdgeLengthFunction()); // here is my length calculation
			//do it with the F-R  layout
			//sp_layout = new FRLayout<P2PVertex,Number>(hiddenGraph); // here is my length calculation
			sp_layout.setSize(new Dimension(DEFWIDTH,DEFHEIGHT));
			sp_layout.setForceMultiplier(0.6); //testing this value
			//((SpringLayout<Number,Number>)layout).setRepulsionRange(50);
			//-----------------------------------------------------------------
			sp_layout.setInitializer(new P2PVertexPlacer(sp_layout, new Dimension(DEFWIDTH,DEFHEIGHT)));
			
			
			/// we're about to start working on that layout
			sp_layout.initialize();
			int count = 0;
			while ((str = in.readLine()) != null) //reading lines log file
			{
				count++;
				//if(count %100 ==0) System.out.println(count+ "lines read");
				LogEvent gev = new LogEvent(str);
				myGraphEvolution.addLast(gev);

				//add all the nodes to construct the new graph
				if (gev.isConstructing()){
					hiddenGraph.event(gev); //change graph
					sp_layout.step(); //do one step in changing the layout of the graph
					sp_layout.step(); //and another few
					/*sp_layout.step();
					sp_layout.step();
					sp_layout.step();*/
				}
				
				
		
			}//end while
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

		System.out.println("Done !!");

		/*for (int i=0; i<500; i++)sp_layout.step(); // give it another few hundred steps of figuring itself out
		System.out.println("Layout finalized !!");
		
		
		//freeze  the layout
		layout = new StaticLayout<P2PVertex,Number>(hiddenGraph, sp_layout);		
*/
		
		layout = sp_layout;
		//create viewer
		vv = new VisualizationViewer<P2PVertex,P2PConnection>(layout, new Dimension(DEFWIDTH,DEFHEIGHT));

		JRootPane rp = this.getRootPane();
		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(java.awt.Color.lightGray);
		getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));
		//try set the size
		//getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);

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
		
		timer = new Timer();
		
		System.out.println("dododo");
		
		Relaxer relaxer = new VisRunner((IterativeContext)layout);
		relaxer.stop();
		relaxer.setSleepTime(80L);
		relaxer.relax();

		//button to freeze layout, then to fast-forward
		fastforward = new JButton("Click here when you're happy with the layout...");
		fastforward.addActionListener(new MyButtonListener(relaxer)); 
		
		//
		pausebutton = new JButton("Pause");
		pausebutton.addActionListener(new PauseButtonListener()); 
		
		JPanel south = new JPanel();
		JPanel p = new JPanel();
		//add little combo box to choose between the mouse picking and the mouse transforming the layout
		p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		p.add(gm.getModeComboBox());
		south.add(p);
		south.add(fastforward);
		south.add(pausebutton);
		pausebutton.setEnabled(false);
		
		getContentPane().add(south, BorderLayout.SOUTH);

	}

	//// Methods to be called from separate thread that runs simulation or reads simulation log /////////////

	/*
	 * add a vertex to the graph
	 * @param number
	 * /
    public void addVertex(int vert) {


    }*/

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
	class MyButtonListener implements ActionListener {
 		
		//this is the SpringLayout relaxer for the beginning
		private Relaxer relaxer;
		boolean started;
		
		public MyButtonListener (Relaxer rr){ // the relaxer is the thread that's doing the dynamic layout.
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
				fastforward.setText(" -- This will be the static layout -- simulation will now start");
				System.out.println("freezing layout !");
				layout = new StaticLayout<P2PVertex,P2PConnection>(hiddenGraph, layout);

				//change the layout we're viewing
				vv.getModel().setGraphLayout(layout);
				vv.repaint();
				//	tie the "include functions" of the viewer to the visible graph
				
				started = true;

				Thread.sleep(1000);
				
				//activate the pause / resume button
				pausebutton.setEnabled(true);

				System.out.println("starting activity now !");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //wait 5 seconds
			//these predicates say : if the considered node /edge (which will be evaluated in the context of the hiddengraph) is found in the visible graph, then show it !
			vv.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(visibleGraph));
			vv.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));

			eventthread.start();
			
			fastforward.setText("Fast Forward");


		} else if (fastforward.getText().startsWith("Fast")) {
				fastforward.setText("Normal Speed");
				eventthread.fastForward();
				if (eventthread.getState().equals(Thread.State.TIMED_WAITING))
					eventthread.interrupt(); //if we were waiting for the next event, we'll just wake the thread.
				
			} else {
				fastforward.setText("Fast Forward");
				eventthread.normalSpeed();
			}
					
		}
	}
	
	 /**
	 * an actionlistener that defines the use of the button at the bottom of the applet 
	 * @author adavoust
	 *
	 */
		class PauseButtonListener implements ActionListener {
	 		
			
/*			public PauseButtonListener (){ // the relaxer is the thread that's doing the dynamic layout.
											  // we need to stop it then switch to a static layout,
											  // then show the full graph layout for one sec then make it hidden
				this.relaxer = rr;
				started = false; // says if we've passed the initial problem of freezing the layout and getting started
				
			}*/

			/**
			 * handles the button : pause /resume the eventthread
			 */
			public void actionPerformed(ActionEvent ae) {

				if (pausebutton.getText().startsWith("Pause")) {
					pausebutton.setText("Resume");
					eventthread.pause();
					} else {
					pausebutton.setText("Pause");
					eventthread.myresume();
					
				}

			}
		}
	
	
	/**
	 * this class is a task to undo "highlighting" changes in the graph :
	 * - picked nodes that stay highlighted for a second after they appear on screen
	 * - querying nodes that keep a "query" state after a few seconds
	 * - query answering nodes that just highlight briefly to show that they got the query
	 * @author adavoust
	 *
	 */
	class RemindTask extends TimerTask {

		public static final int UNPICK_VERTEX = 1;
		public static final int UNPICK_EDGE = 2;
		public static final int UNQUERY_PEER = 3;
		public static final int UN_ANSWER_PEER = 4;
		public static final int UN_ANSWER_DOC = 5;
		public static final int UNQUERY_EDGE = 6;

		
		Object toChange;
		int tasktype; //0 : unpick
		//boolean isVertex;

		public int getDelay(){
			switch(tasktype){
			case UNPICK_VERTEX:
				return 1000; // 1 sec to unpick a vertex
			case UNPICK_EDGE:
				return 1000;
			case UNQUERY_PEER:
				return 1500;
			case UN_ANSWER_PEER:
			case UN_ANSWER_DOC:
				return 500;
			case UNQUERY_EDGE:
				return 750;				
			default:
				return 0;
			}
			
		}

		/**
		 * constructor
		 * @param kk the object that the task applies to, either a P2PVertex or a P2PConnection
		 * @param whattodo is the task, from a predefined list
		 */
		public RemindTask(Object kk, int whatTodo){ // the object will be a number or a P2PVertex
			toChange = kk;    	
			tasktype = whatTodo;
		}

		/**
		 * execute this task
		 */
		public void run() {

			switch(tasktype){
			case UNPICK_VERTEX:
				vv.getRenderContext().getPickedVertexState().pick((P2PVertex)toChange, false);
				break;
			case UNPICK_EDGE:
				vv.getRenderContext().getPickedEdgeState().pick((P2PConnection)toChange, false);
				break;
			case UNQUERY_PEER:
			case UN_ANSWER_PEER:
			case UN_ANSWER_DOC:
				((P2PVertex)toChange).backToNormal();
				break;
				
			case UNQUERY_EDGE:
				((P2PConnection)toChange).backToNormal();
				break;
			default:
				//do nothing
			}

			eventthread.notifyTask(this); // notify the eventthread that this task has been done
			//update the layout after we just did something
			vv.repaint();
		
		}
		
		@Override
		public Object clone(){
			return new RemindTask(toChange, tasktype); // not much to be cloned... "tochange" is a reference anyway
		}
		
		@Override 
		public String toString(){
		 return "TASK:"+tasktype+" on graph component "+ toChange.toString();	
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

	/**
	 * an internal class extending thread, that can play the sequence of events from the log file in real time
	 * or fast forward.
	 * @author alan
	 *
	 */
	private class EventPlayingThread extends Thread {

		private static final long ffspeed = 50L; // 50 millisec between events while we're fast-forwarding

		private boolean ffwd = false;
		
		Queue<LogEvent> my_eventlist;
		
		List<RemindTask> taskspending;//maintain a list of scheduled tasks... so that we don't lose them on a pause() action

		public EventPlayingThread(Queue<LogEvent> eventlist){
			my_eventlist = eventlist;
			taskspending = new LinkedList<RemindTask>();
		}

		/** notify : this task has been done we can remove it from the to-do list
		 * 
		 * @param tsk the task that's been done
		 */
		public void notifyTask(RemindTask tsk) {
			taskspending.remove(tsk);
			
		}

		//a hack for the pause / resume... the timer needs to be notified that it can also continue
		public void myresume() {
			timer = new Timer();
			for(RemindTask t : taskspending){
				timer.schedule((RemindTask)t.clone(),t.getDelay());//re-schedule all the tasks that were on hold 
				//System.out.println("Rescheduling task : "+t.toString());
			}
			this.resume();//it's deprecated but for now it's the easiest way of doing it.
			
		}

		public void fastForward(){
			ffwd = true;
		}

		public void normalSpeed(){
			ffwd = false;
		}
		
		public void pause(){
			//pb : I may have tasks pending... will need to reschedule them once timer restarts.
			timer.cancel();
			this.suspend();
			
		}


		//convenience method to schedule "remindtasks" and not forget to add them to the list
		private void taskSchedule(RemindTask tsk){
			timer.schedule(tsk, tsk.getDelay());  // schedule it for execution using the timer
			taskspending.add(tsk); // store it in case of pause
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
				if (v.equals(tofind)){
					v.query(q); //change state to "querying" and record which is the query
					//schedule a task in one second to return the color of that vertex to normal.
					taskSchedule(new RemindTask(v,RemindTask.UNQUERY_PEER)); 
					
					break;}
			}
			vv.repaint();// update visual
		}
		
		/**
		 * Visualize a query reaches peer event
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
						//schedule a task in half a second to return the color of that vertex to normal.
						for (P2PConnection edge : hiddenGraph.getIncidentEdges(v)){
							P2PVertex otherV= hiddenGraph.getOpposite(v, edge);
							if (otherV.getQueryState()>P2PVertex.PEER && otherV.getmessageid()==mid){ //this is true if the peer is in one of the states query, getquery,answering, for the same qid
								edge.query();// the edge is passing the query
								taskSchedule(new RemindTask(edge,RemindTask.UNQUERY_EDGE)); //undo hte querying state of the edge
								//System.out.println("found originator");
								break;
							} //else 
								//System.out.println(otherV.getLabel() +"   "+otherV.getQueryState());
						}
						taskSchedule(new RemindTask(v,RemindTask.UN_ANSWER_PEER));
						
					} else {
						v.query(mid);// we let the peer query, but set the query number to the message id 
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
					//schedule a task in one second to return the color of that vertex to normal.
					taskSchedule(new RemindTask(v,RemindTask.UN_ANSWER_PEER));
					foundpeer=true;
					if(founddoc)break;
				}
				else if(v.equals(docToFind)){
					v.answering(); //change state to "matching doc"
					//schedule a task in one second to return the color of that vertex to normal.
					taskSchedule(new RemindTask(v,RemindTask.UN_ANSWER_DOC));
					founddoc=true;
					if(foundpeer)break;
				}
			}
			vv.repaint();// update visual
		}


		public void run() {
			System.out.println("Starting log event sequence.");


			long mytimenow = 0L;//System.currentTimeMillis();
			long nexttime;

			//READING FROM CD++ LOG FILE/////////////
			while (!my_eventlist.isEmpty()) //reading lines from config file to get parameter list
			{
				LogEvent evt = my_eventlist.poll(); //get from the front of the queue

				nexttime = evt.getTime();
				//System.out.println("next event at :"+nexttime);
				try{
					if (!ffwd) //fast forwarding ?
						Thread.sleep(Math.max(0,nexttime-mytimenow)); //wait until that event time comes up
					else 
						Thread.sleep(ffspeed);//wait for the internal "fast-forward" speed
				} catch (InterruptedException e){
					System.err.println("log event thread interrupted !");
					// but don't stop, we probably just went from real-time to fast-fwd 
				}

				mytimenow = nexttime; //advance time
				
				//if the event is to modify the structure of the graph
				if (evt.isStructural()){
					visibleGraph.event(evt);
					vv.repaint();
				} else { //other events: queries
					String what = evt.getType();
					int val1 = evt.getParam(1);
					int val2 = evt.getParam(2);

					if(what.equals("query")){
						doQuery(val1, val2);
					}
					else if (what.equals("queryhit")){
						doQueryHit(val1, val2);
					}
					else if (what.equals("queryreachespeer")){
						doQueryReachesPeer(val1,val2);
					}
				}

			}//end while

		}
	}
}
