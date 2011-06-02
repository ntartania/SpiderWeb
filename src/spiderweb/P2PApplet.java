package spiderweb;

//[start] Imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
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
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
//[end] Imports

/**
 * an applet that will display a graph using a spring layout, and as the graph changes the layout is updated.
 * @author Alan
 * @author Matt
 */
public class P2PApplet extends JApplet implements EventPlayerListener {
	//[start] Attributes
	
	//[start] Static Final Attributes
	// for the length of the edges in the graph layout
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Transformer<P2PConnection,Integer> UNITLENGTHFUNCTION = new ConstantTransformer(100);

	//the log file (default value)
	private static final String DEF_LOG_FILE = "ProcessedLog.txt";
	private static final String DEF_LOG_URL = "http://www.sce.carleton.ca/~adavoust/simuldemo/ProcessedLog.txt";
	
	//default size for the swing graphic components
	public static final int DEFWIDTH = 1360;
	public static final int DEFHEIGHT = 768;
	//[end] Static Final Attributes
	
	//[start] Private Variables
	private static final long serialVersionUID = 2L;
	
	private static final boolean ONWEB = true;
	
	// this boolean indicates, as ONWEB above, that we are running on the web.
	//it can be changed through the main()
	private boolean ontheweb=ONWEB;
	
	private File mylogfile =null;
	
	private VisualizationViewer<P2PVertex,P2PConnection> fullViewViewer = null;
	private VisualizationViewer<P2PVertex,P2PConnection> collapsedDocumentViewViewer = null;
	private VisualizationViewer<P2PVertex,P2PConnection> collapsedPeerViewViewer = null;
	private VisualizationViewer<P2PVertex,P2PConnection> collapsedPeerAndDocumentViewViewer = null;

	private AbstractLayout<P2PVertex,P2PConnection> layout = null;
	
	private LinkedList<LogEvent> myGraphEvolution;

	//a hidden graph that contains all the nodes that will ever be added... 
	//in order to calculate the positions of all the nodes
	private P2PNetworkGraph hiddenGraph; 
	private P2PNetworkGraph visibleGraph = null;
	
	private List<LoadingListener> loadingListeners;
	
	//[end] Private Variables
	
	//[start] Protected Variables
	protected JTable logList;
	protected JTabbedPane tabsPane;
	protected JButton fastForwardButton;
	protected JButton forwardButton;
	protected JButton pauseButton;
	protected JButton reverseButton;
	protected JButton fastReverseButton;
	protected JSlider fastSpeedSlider;
	protected JSlider playbackSlider;
	protected JPopupMenu mouseContext;

	protected EventPlayer eventThread;
	//[end] Protected Variables
	
	//[end] Attributes
	
	//[start] Constructor
	public P2PApplet(boolean onWeb) {
		ontheweb = onWeb;
		loadingListeners = new LinkedList<LoadingListener>();
		init();
		start();
		
		if(!onWeb) {
			
			JFrame frame = new JFrame();
			//frame.setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(this);

			frame.pack();
			frame.setVisible(true);
			
			//LogPanelWithFileChooser mygui = new LogPanelWithFileChooser(this);
	
			//mygui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//mygui.validate();
			//mygui.setBounds(200,200,500,200);
			//mygui.pack();
			//mygui.setVisible(true);
			
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
	
	//[start] Calculate the Spring Layout
	/**
	 * Helper Method for setting up the spring layout.
	 * @return The initialized spring layout.
	 */
	private SpringLayout<P2PVertex,P2PConnection> springLayoutBuilder(List<LogEvent> graphEvents, int width, int height, P2PNetworkGraph graph) {
		final int numSteps = 100;
		for(LoadingListener l : loadingListeners) {
			l.loadingChanged(numSteps, "Spring Layout");
		}
		SpringLayout<P2PVertex,P2PConnection> sp_layout=null;
		sp_layout = new SpringLayout<P2PVertex,P2PConnection>(graph, new P2PNetEdgeLengthFunction()); // here is my length calculation
		
		sp_layout.setSize(new Dimension(width,height));
		sp_layout.setForceMultiplier(0.6); //testing this value

		sp_layout.setInitializer(new P2PVertexPlacer(sp_layout, new Dimension(width,height)));
		
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
	
	//[start] Create the visualization viewer
	/**
	 * 
	 * @return The Initialized Visualization Viewer
	 */
	private VisualizationViewer<P2PVertex,P2PConnection> visualizationViewerBuilder(final Layout<P2PVertex, P2PConnection> layout, int width, int height, GraphMouse gm) {
		VisualizationViewer<P2PVertex,P2PConnection> viewer = new VisualizationViewer<P2PVertex,P2PConnection>(layout, new Dimension(width,height));
		JRootPane rp = this.getRootPane();
		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

		

		// the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
		viewer.setGraphMouse(gm);

		//set graph rendering parameters & functions
		viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		//the vertex labeler will use the tostring method which is fine, the P2PVertex class has an appropriate toString() method implementation
		viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<P2PVertex>());
		viewer.getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(viewer.getPickedVertexState()));
		// P2PVertex objects also now have multiple states : we can represent which nodes are documents, picked, querying, queried, etc.
		
		viewer.getRenderContext().setVertexStrokeTransformer(new P2PVertexStrokeTransformer());
		viewer.setForeground(Color.white);
		viewer.setBackground(Color.GRAY);
		viewer.setBounds(0,0,width,height);
		
		viewer.addComponentListener(new ComponentAdapter() {
			
			/**
			 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
			 */
			@Override
			public void componentResized(ComponentEvent arg0) {
				super.componentResized(arg0);
				//viewer.getGraphLayout().setSize(arg0.getComponent().getSize());
			}
		});
		
		return viewer;
	}
	
	private void initSpecialTransformers(VisualizationViewer<P2PVertex,P2PConnection> viewer,
			VertexShapeType peerShape, VertexShapeType documentShape, VertexShapeType peerDocumentShape,
			EdgeShapeType P2PEdgeShape, EdgeShapeType P2DocEdgeShape, EdgeShapeType Doc2PDocEdgeShape, EdgeShapeType P2PDocEdgeShape) {
		
		//add my own vertex shape & color fill transformers
		viewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(peerShape,documentShape, peerDocumentShape));
		// note :the color depends on being picked.
		
		//make the p2p edges different from the peer to doc edges 
		viewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
		viewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer(P2PEdgeShape,P2DocEdgeShape,Doc2PDocEdgeShape,P2PDocEdgeShape)); //stroke width
		
	}
	//[end] Create the visualization viewer
	
	//[start] Create Components
	
	//[start] File Menu
	private JMenuBar createFileMenu() {
		//[start] File Menu
		JMenu file = new JMenu("File");
		//[start] Connect Entry
		JMenuItem connect = new JMenuItem("Connect to...");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pauseButton.doClick();
				String option = JOptionPane.showInputDialog(null, "Enter a URL:", "Connect", JOptionPane.PLAIN_MESSAGE);
				if(option != null ) {
					if(option.startsWith("http://")){
						
					}
					else {
						JOptionPane.showMessageDialog(null, "Invalid URL", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				//else cancel option, don't do anything
			}	
		});
		//[end] Connect Entry
		
		//[start] Save Entry
		JMenuItem save = new JMenuItem("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pauseButton.doClick();
				int option = JOptionPane.showConfirmDialog(null, "Would you like to save the first 500 log events after this graph snapshot", 
						"Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(option == JOptionPane.YES_OPTION) {
					GraphSaverAndLoader.save(visibleGraph,eventThread.getSaveEvents(), eventThread.getCurrentTime());
				}
				else if(option == JOptionPane.NO_OPTION) {
					GraphSaverAndLoader.save(visibleGraph);
				}
				//else cancel option, don't do anything
			}	
		});
		//[end] Save Entry
		
		//[start] Load Entry
		JMenuItem load = new JMenuItem("Load");
		load.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent arg0) {
				
				
				JFileChooser fileNamer = new JFileChooser();
				String [] extensions = { "xml","txt" };
				fileNamer.setFileFilter(new ExtensionFileFilter(".xml and .txt Files", extensions));
				int returnVal = fileNamer.showOpenDialog(null);
				

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					if(fileNamer.getSelectedFile().getAbsolutePath().endsWith(".xml") ||
							fileNamer.getSelectedFile().getAbsolutePath().endsWith(".txt")) {

						mylogfile = fileNamer.getSelectedFile();
						
						if(myGraphEvolution != null) {
							myGraphEvolution.clear();
							eventThread.stopPlayback();
							fastReverseButton.setEnabled(false);
							reverseButton.setEnabled(false);
							pauseButton.setEnabled(false);
							forwardButton.setEnabled(false);
							fastForwardButton.setEnabled(false);
							playbackSlider.setEnabled(false);
							playbackSlider.setValue(0);
							tabsPane.setEnabled(false);
							fastSpeedSlider.setEnabled(false);
						}
						Thread test = new Thread(new Runnable() {
							
							@Override
							public void run() {
								startGraph();
							}
						});
						test.start();
						
					}
					else {
						JOptionPane.showMessageDialog(null, "Wrong File Type", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else if(returnVal == JFileChooser.CANCEL_OPTION || returnVal == JFileChooser.ERROR_OPTION) {
					
				}
				
				
				//pauseButton.doClick();
				//P2PNetworkGraph graph = GraphSaverAndLoader.load();
				//if(graph != null) {
					//visibleGraph = graph;
				//}
			}	
		});
		//[end] Load Entry
		
		//[start] Exit Entry
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent arg0) {
				pauseButton.doClick();
				int option = JOptionPane.showConfirmDialog(null, "Would you like to save before quitting?", "Save", 
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if(option == JOptionPane.OK_OPTION) {
					GraphSaverAndLoader.save(visibleGraph);
					System.exit(0);
				}
				else if(option == JOptionPane.NO_OPTION) {
					System.exit(0);
				}
				//else if(option == JOptionPane.CANCEL_OPTION) {
					//do nothing
				//}
			}	
		});
		//[end] Exit Entry
		
		file.add(connect);
		file.addSeparator();
		file.add(save);
		file.add(load);
		file.addSeparator();
		file.add(exit);
		//[end] File Menu
		
		//[start] Window Menu
		JMenu window = new JMenu("Window");
		//[start] Connect Entry
		JMenuItem logTable = new JMenuItem("Show Log Table");
		logTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(myGraphEvolution != null) { //graph has been initialized
					JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
					p.setResizeWeight(1);
					p.add(getContentPane().getComponent(0));
					p.add(initializeLogList(myGraphEvolution));
					
					getContentPane().add(p);
				}
			}	
		});
		window.add(logTable);
		//[end] Window Menu
		
		JMenuBar bar = new JMenuBar();
		bar.add(file);
		bar.add(window);
		bar.setVisible(true);
		return bar;
	}
	//[end] File Menu
	
	//[start] East Panel
	
	/*private JPanel initializeEastPanel() {
		logList = new JTable();
		
		
		
		return eastPanel;
	}*/
	
	private JPanel initializeLogList(List<LogEvent> logEvents) {
		for(LoadingListener l : loadingListeners) {
			l.loadingStarted(logEvents.size(), "Log List");
		}
		
		Object[][] table = new Object[logEvents.size()][4];
		int i=0;
		for(LogEvent evt : logEvents) {
			table[i] = evt.toArray();
			i++;
			for(LoadingListener l : loadingListeners) {
				l.loadingProgress(i);
			}
		}
		Object[] titles = { "Time", "Type", "Param 1", "Param 2" };
		
		
		logList = new JTable(table, titles);
		
		logList.setBackground(Color.LIGHT_GRAY);
		logList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		logList.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		logList.setEnabled(false);
		logList.setColumnSelectionAllowed(false);
		logList.setVisible(true);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(logEvents.size()+1);
		}
		
		JScrollPane listScroller = new JScrollPane(logList);
		listScroller.setWheelScrollingEnabled(true);
		listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
		listScroller.setSize(logList.getWidth(),logList.getHeight());
		
		JPanel tablePanel = new JPanel(new GridLayout(1,1));
		tablePanel.add(listScroller);
		//tablePanel.setBackground(Color.GRAY);
		tablePanel.setBorder(BorderFactory.createTitledBorder("Log Events"));
		
		return tablePanel;
	}
	//[end] tablePanel
	
	//[start] South Panel
	/**
	 * Helper Method for initializing the Buttons and slider for the South Panel.
	 * @return The South Panel, laid out properly, to be displayed.
	 */
	private JPanel initializeSouthPanel() {
		
		fastSpeedSlider = new JSlider(JSlider.HORIZONTAL,0,100,25);
		fastSpeedSlider.addChangeListener(new SpeedSliderListener());
		fastSpeedSlider.setMajorTickSpacing((fastSpeedSlider.getMaximum()-fastSpeedSlider.getMinimum())/4);
		fastSpeedSlider.setFont(new Font("Arial",Font.PLAIN,8));
		fastSpeedSlider.setPaintTicks(false);
		fastSpeedSlider.setPaintLabels(true);
		//fastSpeedSlider.setBackground(Color.DARK_GRAY);
		fastSpeedSlider.setForeground(Color.BLACK);
		fastSpeedSlider.setBorder(BorderFactory.createTitledBorder("Quick Playback Speed"));
		fastSpeedSlider.setEnabled(false);
		
		fastReverseButton = new JButton("<|<|");
		fastReverseButton.addActionListener(new FastReverseButtonListener()); 
		fastReverseButton.setEnabled(false);
		
		reverseButton = new JButton("<|");
		reverseButton.addActionListener(new ReverseButtonListener());
		reverseButton.setEnabled(false);
		
		pauseButton = new JButton("||");
		pauseButton.addActionListener(new PauseButtonListener()); 
		pauseButton.setEnabled(false);
		
		forwardButton = new JButton("|>");
		forwardButton.addActionListener(new ForwardButtonListener());
		forwardButton.setEnabled(false);
		
		fastForwardButton = new JButton("|>|>");
		fastForwardButton.addActionListener(new FastForwardButtonListener());
		fastForwardButton.setEnabled(false);
		
		playbackSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
		
		//playbackSlider.setBackground(Color.LIGHT_GRAY);
		playbackSlider.setEnabled(false);
		
		GridBagLayout southLayout = new GridBagLayout();
		GridBagConstraints southConstraints = new GridBagConstraints();
		
		
		JPanel buttonPanel = new JPanel();
		//buttonPanel.setBackground(Color.LIGHT_GRAY);
		buttonPanel.setLayout(southLayout);
		
		buttonPanel.add(fastReverseButton);
		buttonPanel.add(reverseButton);
		buttonPanel.add(pauseButton);
		buttonPanel.add(forwardButton);
		buttonPanel.add(fastForwardButton);
		southConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
		southLayout.setConstraints(fastSpeedSlider, southConstraints);
		buttonPanel.add(fastSpeedSlider);
		
		
		JPanel south = new JPanel();
		//south.setBackground(Color.LIGHT_GRAY);
		south.setLayout(new GridLayout(2,1));
		south.setBorder(BorderFactory.createTitledBorder("Playback Options"));
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
	/*private JPanel initializeWestPanel(DefaultModalGraphMouse<P2PVertex,P2PConnection> gm) {
		for(LoadingListener l : loadingListeners) {
			l.loadingChanged(5, "West Panel");
		}
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(1);
		}
		
		
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
		westConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
		//westLayout.setConstraints(relaxerButton, westConstraints);
		//west.add(relaxerButton);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(5);
		}
		return west;
	}*/
	//[end] West Panel
	
	//[start] Mouse Context Menu
	private void initializeMouseContext(final DefaultModalGraphMouse<P2PVertex,P2PConnection> gm) {
		mouseContext = new JPopupMenu("Mouse Mode");
		JMenuItem picking = new JMenuItem("Picking");
		picking.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gm.setMode(Mode.PICKING);
				mouseContext.setVisible(false);
				mouseContext.setEnabled(false);
			}
		});
		
		JMenuItem transforming = new JMenuItem("Transforming");
		transforming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gm.setMode(Mode.TRANSFORMING);
				mouseContext.setVisible(false);
				mouseContext.setEnabled(false);
			}
		});
		mouseContext.add("Mouse Mode:").setEnabled(false);
		mouseContext.add(picking);
		mouseContext.add(transforming);
		mouseContext.addSeparator();
		mouseContext.add("Set Layout:").setEnabled(false);
	}
	
	//[end] Mouse Context Menu
	
	//[end] Create Components
	
	//[start] init method
	/**
	 * applet initialization
	 */
	public void init() {
		
		//[start] Tabs Pane
		tabsPane = new JTabbedPane(JTabbedPane.TOP);
		JPanel initialTab = new JPanel();
		//initialTab.setSize(DEFWIDTH, DEFHEIGHT);
		tabsPane.addTab("Welcome", initialTab);
		
		tabsPane.setEnabled(false);
		//[end] Tabs Pane
		
		JPanel graphsPanel = new JPanel(new GridLayout(1,1));
		//graphsPanel.setBackground(Color.GRAY);
		graphsPanel.add(tabsPane);
		//graphsPanel.setSize(DEFWIDTH, DEFHEIGHT);
		//graphsPanel.setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));
				
		JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		p.setResizeWeight(1);
		p.add(graphsPanel);
		p.add(initializeSouthPanel());
		
		
		getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
		//try set the size
		getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
		setJMenuBar(createFileMenu());
		getContentPane().add(p);
		setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));
		
		//startGraph();
		loadingListeners.add(new LoadingBar());
	}
	//[end] init method
	
	//[start] Init Graph
	public void startGraph() {
		for(LoadingListener l : loadingListeners) {
			l.loadingStarted(1, "Graphs");
		}
		hiddenGraph = new P2PNetworkGraph();
		visibleGraph = new P2PNetworkGraph();
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(1);
		}
		
		//[start] Build Log Event List
		LogEventListBuilder logBuilder = new LogEventListBuilder(hiddenGraph);
		for(LoadingListener l : loadingListeners) {
			logBuilder.addLoadingListener(l);
		}
		myGraphEvolution = logBuilder.createLinkedList(openLogFile());
		//[end] Build Log Event List
		
		layout = springLayoutBuilder(myGraphEvolution,DEFWIDTH,DEFHEIGHT,hiddenGraph);
		
		//[start] Relaxer creation
		Relaxer relaxer = new VisRunner((IterativeContext)layout);
		relaxer.stop();
		relaxer.setSleepTime(80L);
		relaxer.relax();
		//[end] Relaxer creation
		
		DefaultModalGraphMouse<P2PVertex,P2PConnection> gm = new DefaultModalGraphMouse<P2PVertex,P2PConnection>(); 
		
		//[start] Full Visualization Viewer Init
		fullViewViewer = visualizationViewerBuilder(layout, DEFWIDTH,DEFHEIGHT, gm);
		fullViewViewer.addMouseListener(new GraphMouseListener(relaxer));
		fullViewViewer.add(new MyGlassPane());
		fullViewViewer.setName("Full View");
		//add my own vertex shape & color fill transformers
		initSpecialTransformers(fullViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
								EdgeShapeType.QUAD_CURVE,
								EdgeShapeType.CUBIC_CURVE,
								EdgeShapeType.LINE,
								EdgeShapeType.LINE);
		//[end] Full Visualization Viewer Init
		
		//[start] Collapsed Document Visualization Viewer Init
		collapsedDocumentViewViewer = visualizationViewerBuilder(fullViewViewer.getGraphLayout(),DEFWIDTH,DEFHEIGHT, fullViewViewer.getGraphMouse());
		collapsedDocumentViewViewer.setName("Collapsed Document View");		
		collapsedDocumentViewViewer.getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(
				collapsedDocumentViewViewer.getPickedVertexState(),Color.RED, Color.YELLOW, Color.MAGENTA, Color.RED, Color.RED, Color.BLUE));
		collapsedDocumentViewViewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(
				VertexShapeType.ELLIPSE, VertexShapeType.PENTAGON, VertexShapeType.ELLIPSE, 
				P2PVertexShapeTransformer.PEER_SIZE, P2PVertexShapeTransformer.DOC_SIZE, P2PVertexShapeTransformer.PEER_SIZE));
		collapsedDocumentViewViewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
		collapsedDocumentViewViewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer(EdgeShapeType.QUAD_CURVE,
				EdgeShapeType.CUBIC_CURVE,EdgeShapeType.LINE,EdgeShapeType.LINE)); //stroke width
		//[end] Collapsed Document Visualization Viewer Init
		
		//[start] Collapsed Peer Visualization Viewer Init
		collapsedPeerViewViewer = visualizationViewerBuilder(fullViewViewer.getGraphLayout(),DEFWIDTH,DEFHEIGHT, fullViewViewer.getGraphMouse());
		collapsedPeerViewViewer.setName("Collapsed Peer View");
		initSpecialTransformers(collapsedPeerViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
								EdgeShapeType.QUAD_CURVE,
								EdgeShapeType.CUBIC_CURVE,
								EdgeShapeType.LINE,
								EdgeShapeType.LINE);
		//[end] Collapsed Peer Visualization Viewer Init
		
		//[start] Collapsed Peer AndDocument Visualization Viewer Init
		collapsedPeerAndDocumentViewViewer = visualizationViewerBuilder(fullViewViewer.getGraphLayout(),DEFWIDTH,DEFHEIGHT, fullViewViewer.getGraphMouse());
		collapsedPeerAndDocumentViewViewer.setName("Collapsed Peer and Document View");
		initSpecialTransformers(collapsedPeerAndDocumentViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
								EdgeShapeType.QUAD_CURVE,
								EdgeShapeType.CUBIC_CURVE,
								EdgeShapeType.LINE,
								EdgeShapeType.LINE);
		//[end] Collapsed Peer AndDocument Visualization Viewer Init
		
		initializeMouseContext(gm);
		
		//initializeLogList(myGraphEvolution);
		playbackSlider.setMaximum((int)myGraphEvolution.getLast().getTime());
		
		tabsPane.removeAll();
		tabsPane.addTab(fullViewViewer.getName(),fullViewViewer);
		tabsPane.addTab(collapsedDocumentViewViewer.getName(), collapsedDocumentViewViewer);
		tabsPane.addTab(collapsedPeerViewViewer.getName(), collapsedPeerViewViewer);
		tabsPane.addTab(collapsedPeerAndDocumentViewViewer.getName(), collapsedPeerAndDocumentViewViewer);
		
		setGlassPane(new MyGlassPane());
		getGlassPane().setVisible(true);
		
		/// create the event player
		eventThread = new EventPlayer(myGraphEvolution,hiddenGraph,visibleGraph, playbackSlider);
		eventThread.addEventPlayerListener(this);
		
		SliderListener s = new SliderListener();
		playbackSlider.addChangeListener(s);
		playbackSlider.addMouseListener(s);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingComplete();
		}
		
		fullViewViewer.repaint();
		collapsedDocumentViewViewer.repaint();
		collapsedPeerViewViewer.repaint();
		collapsedPeerAndDocumentViewViewer.repaint();
	}
	//[end] Init Graph
	
	//[start] overridden start method
	@Override
	public void start() {
		validate();

		
		///----------run the spring layout algorithm with the full hidden graph for a bit -------
	}
	//[end] overridden start method
	
	//[end] Initialization
	
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

	//[start] Helper Methods
	
	//[start] Layout Items for context menu
	public List<JMenuItem> getLayoutItems() {
		List<JMenuItem> menuItems = new LinkedList<JMenuItem>();
		
		
		if(tabsPane.getSelectedComponent().getName().equals("Full View")) {
			JMenuItem j = new JMenuItem("Full Layout 1");
			menuItems.add(j);
		}
		else if(tabsPane.getSelectedComponent().getName().equals("Collapsed Document View")) {
			JMenuItem j = new JMenuItem("Collapsed Document Layout 1");
			menuItems.add(j);
		}
		else if(tabsPane.getSelectedComponent().getName().equals("Collapsed Peer View")) {
			JMenuItem j = new JMenuItem("Collapsed Peer Layout 1");
			JMenuItem j2 = new JMenuItem("Collapsed Peer Layout 2");
			menuItems.add(j);
			menuItems.add(j2);
		}
		else if(tabsPane.getSelectedComponent().getName().equals("Collapsed Peer and Document View")) {
			JMenuItem j = new JMenuItem("Collapsed Peer and Document Layout 1");
			JMenuItem j2 = new JMenuItem("Collapsed Peer and Document Layout 2");
			JMenuItem j3 = new JMenuItem("Collapsed Peer and Document Layout 3");
			menuItems.add(j);
			menuItems.add(j2);
			menuItems.add(j3);
		}
		
		return menuItems;
	}
	//[end] Layout Items for context menu
	
	//[end] Helper Methods
	
	//[start] EventPlayer Handlers
	
	@Override
	public void playbackFastReverse() {
		fastReverseButton.setEnabled(false);
		reverseButton.setEnabled(true);
		pauseButton.setEnabled(true);
		forwardButton.setEnabled(true);
		fastForwardButton.setEnabled(true);
	}

	@Override
	public void playbackReverse() {
		fastReverseButton.setEnabled(true);
		reverseButton.setEnabled(false);
		pauseButton.setEnabled(true);
		forwardButton.setEnabled(true);
		fastForwardButton.setEnabled(true);
	}

	@Override
	public void playbackPause() {
		if(eventThread.atFront()) {
			fastReverseButton.setEnabled(false);
			reverseButton.setEnabled(false);
		} else {
			fastReverseButton.setEnabled(true);
			reverseButton.setEnabled(true);
		}
		pauseButton.setEnabled(false);
		if(eventThread.atBack()) {
			forwardButton.setEnabled(false);
			fastForwardButton.setEnabled(false);
		} else {
			forwardButton.setEnabled(true);
			fastForwardButton.setEnabled(true);
		}
	}

	@Override
	public void playbackForward() {
		fastReverseButton.setEnabled(true);
		reverseButton.setEnabled(true);
		pauseButton.setEnabled(true);
		forwardButton.setEnabled(false);
		fastForwardButton.setEnabled(true);
	}

	@Override
	public void playbackFastForward() {
		fastReverseButton.setEnabled(true);
		reverseButton.setEnabled(true);
		pauseButton.setEnabled(true);
		forwardButton.setEnabled(true);
		fastForwardButton.setEnabled(false);
	}
	
	@Override
	public void doRepaint() {
		fullViewViewer.repaint();
		collapsedDocumentViewViewer.repaint();
		collapsedPeerViewViewer.repaint();
		collapsedPeerAndDocumentViewViewer.repaint();
	}
	
	//[end] EventPlayer Handlers

	//[start] Swing Event Listeners
	
	//[start] Relaxer and mouse context listener
	 /**
	 * an actionlistener that defines the use of a button to stop the spring-layout processing
	 * @author adavoust
	 *
	 */
	private class GraphMouseListener extends MouseAdapter {
		//[start] Relaxer freeze
		
		//this is the SpringLayout relaxer for the beginning
		private Relaxer relaxer;
		boolean started;
		
		public GraphMouseListener (Relaxer rr){ // the relaxer is the thread that's doing the dynamic layout.
										  // we need to stop it then switch to a static layout,
										  // then show the full graph layout for one sec then make it hidden
			this.relaxer = rr;
			started = false; // says if we've passed the initial problem of freezing the layout and getting started
			
		}
		
		/**
		 * handles the button : first to freeze the layout, then to toggle between fast-forward and normal speed
		 */
		@Override
		public void mouseClicked(MouseEvent event) {

			if(!started && event.getButton() == MouseEvent.BUTTON1){ // this will be the first button task : freeze layout and start the simulation
				try {
					relaxer.stop();
					
					layout = new StaticLayout<P2PVertex,P2PConnection>(hiddenGraph, layout);
	
					//change the layout we're viewing
					fullViewViewer.getModel().setGraphLayout(layout);
					collapsedPeerAndDocumentViewViewer.getModel().setGraphLayout(layout);
					collapsedPeerViewViewer.getModel().setGraphLayout(layout);
					collapsedDocumentViewViewer.getModel().setGraphLayout(layout);
					//fullViewViewer.repaint();
					//	tie the "include functions" of the viewer to the visible graph
					
					
					started = true;
	
					Thread.sleep(1000);
					
					fastReverseButton.setEnabled(true);
					reverseButton.setEnabled(true);
					pauseButton.setEnabled(true);
					forwardButton.setEnabled(false);
					fastForwardButton.setEnabled(true);
					playbackSlider.setEnabled(true);
					tabsPane.setEnabled(true);
					fastSpeedSlider.setEnabled(true);
	
	
				} catch (InterruptedException e) {
					e.printStackTrace();
				} //wait 5 seconds

				getGlassPane().setVisible(false);
				
				//these predicates say : if the considered node /edge (which will be evaluated in the context of the hiddengraph) is found in the visible graph, then show it !
				fullViewViewer.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(visibleGraph));
				fullViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
				
				collapsedPeerAndDocumentViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(visibleGraph,PeerDocumentVertex.class));
				collapsedPeerAndDocumentViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
								
				collapsedPeerViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(visibleGraph, DocumentVertex.class));
				collapsedPeerViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
								
				collapsedDocumentViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(visibleGraph,PeerVertex.class));
				collapsedDocumentViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));

				for(MouseListener ml: fullViewViewer.getMouseListeners()) {
					collapsedPeerAndDocumentViewViewer.addMouseListener(ml);
					collapsedPeerViewViewer.addMouseListener(ml);
					collapsedDocumentViewViewer.addMouseListener(ml);
				}
				
				eventThread.run();
			}
		}
		//[end] Relaxer Freeze
		
		@Override 
		public void mousePressed(MouseEvent e) {
			doPop(e);
		}
		
		@Override 
		public void mouseReleased(MouseEvent e) {
			doPop(e);
		}
		
		private void doPop(MouseEvent e) {
			if (e.isPopupTrigger()) {
				for(JMenuItem item : getLayoutItems()) {
					mouseContext.add(item);
				}
				mouseContext.setEnabled(true);
	    		mouseContext.show(null, e.getXOnScreen(), e.getYOnScreen());
	    	}
			else if(mouseContext.isVisible()) {
				int size = mouseContext.getComponentCount();
				for(int i = size-1;i>4;i--) {
					mouseContext.remove(i);
				}
				mouseContext.setVisible(false);
				mouseContext.setEnabled(false);
			}
		}
	}
	//[end] Relaxer and mouse context listener

	//[start] Fast Reverse Button
	class FastReverseButtonListener implements ActionListener {
		
		public void actionPerformed(ActionEvent ae) {
			eventThread.fastReverse();
		}
	}
	//[end] Fast Reverse Button
	
	//[start] Reverse Button
	/**
	 * An ActionListener that defines the action of the reverse button for the applet
	 * @author Matthew
	 * @version May 2011
	 */
	class ReverseButtonListener implements ActionListener {
		
		/**
		 * Method called when the reverse button has an action performed(clicked)
		 * Tells eventThread to traverse the graph placement in reverse.
		 * @param ae	The ActionEvent that triggered the listener
		 */
		public void actionPerformed(ActionEvent ae) {
			eventThread.reverse();
		}
	
	}
	//[end] Reverse Button
	
	//[start] Pause Button
	class PauseButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			eventThread.pause();
		}
	}
	//[end] Pause Button
	
	//[start] Forward Button
	class ForwardButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			eventThread.forward();
		}
	}
	//[end] Forward Button
	
	//[start] Fast Forward Button
	class FastForwardButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {			
			eventThread.fastForward();
		}
	}
	//[end] Fast Forward Button
	
	//[start] Speed Slider
	class SpeedSliderListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			eventThread.setFastSpeed(((JSlider)arg0.getSource()).getValue());
		}
		
	}
	//[end] Speed Slider
	
	//[start] Playback Slider
	class SliderListener extends MouseAdapter implements ChangeListener {

		PlayState prevState = PlayState.PAUSE;
		
		@Override
		public void stateChanged(ChangeEvent ce) {
			JSlider source = (JSlider)ce.getSource();
			eventThread.goToTime(source.getValue());
			if(logList != null) { //if log list is initialized and showing
				if(logList.isVisible()) {
					logList.clearSelection();
					logList.addRowSelectionInterval(0, eventThread.getCurrentIndex()-1);
					logList.scrollRectToVisible(logList.getCellRect(eventThread.getCurrentIndex()-1, 0, true));
				}
			}
		}


		@Override
		public void mousePressed(MouseEvent arg0) {
			if(((JSlider)(arg0.getSource())).isEnabled()){
				prevState = eventThread.getPlayState();
				eventThread.pause();
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if(((JSlider)(arg0.getSource())).isEnabled()){
				if(prevState == PlayState.FASTREVERSE) {
					eventThread.fastReverse();
				}
				else if (prevState == PlayState.REVERSE) {
					eventThread.reverse();
				}
				else if (prevState == PlayState.FORWARD) {
					eventThread.forward();
				}
				else if (prevState == PlayState.FASTFORWARD) {
					eventThread.fastForward();
				}
				else if (prevState == PlayState.PAUSE) {
					eventThread.pause();
				}
			}
		}
	}
	//[end] Playback Slider
	
	//[start] Layout Context Listener
	
	class  LayoutContextListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			
		}
	}
	
	//[end] Layout Context Listener
	
	//[end] Swing Event Listeners
	
	//[start] Glass panel for drawing the click to finalize
	private class MyGlassPane extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public MyGlassPane() {
			setOpaque(false);
		}
		/**
	     * Handling Paint
	     */
	    public void paint(Graphics g)
	    {
	    	Graphics2D g2d = (Graphics2D)g;
	        super.paint(g);
	        
	        Component vv = this.getParent();
	        
	        int cx = (vv.getX()+vv.getWidth())/2;
	        int cy = (vv.getY()+vv.getHeight())/2;
	        
            g2d.setFont(new Font("Arial", Font.BOLD, 35));
            g2d.setColor(new Color(100,149,237,200));
            g2d.rotate(Math.toRadians(-45),cx,cy);
            g2d.drawString("Click to Finalize Layout", cx-cx/2,cy-cy/2);
            
	    }
	}
	//[end] Glass panel for drawing the click to finalize
}