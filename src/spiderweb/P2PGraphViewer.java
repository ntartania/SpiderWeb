/*
 * File:         P2PGraphVeiwer.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 20/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb;

//[start] Imports
import spiderweb.graph.*;
import spiderweb.visualizer.*;
import spiderweb.graph.savingandloading.*;
import spiderweb.visualizer.eventplayer.*;

import spiderweb.networking.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import org.jdom.JDOMException;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
//[end] Imports

/**
 * P2PGraphViewer is the main program which contains the swing application 
 * and maintains all the visualizers and event players.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @author Alan Davoust
 * @version Date: 20/07/2011 
 */
public class P2PGraphViewer extends JApplet implements EventPlayerListener, NetworkGraphListener {
	//[start] Attributes
	
	//[start] Static Final Attributes
	// for the length of the edges in the graph layout
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Transformer<P2PConnection,Integer> UNITLENGTHFUNCTION = new ConstantTransformer(100);

	//default size for the swing graphic components
	public static final int DEFWIDTH = 1360;
	public static final int DEFHEIGHT = 768;
	//[end] Static Final Attributes
	
	//[start] Private Variables
	private static final long serialVersionUID = 2L;
	
	private PersonalizedVisualizationViewer fullViewViewer = null;
	private PersonalizedVisualizationViewer collapsedDocumentViewViewer = null;
	private PersonalizedVisualizationViewer collapsedPeerViewViewer = null;
	private PersonalizedVisualizationViewer collapsedPeerAndDocumentViewViewer = null;
	
	private PersonalizedVisualizationViewer currentViewer = null;

	private AbstractLayout<P2PVertex,P2PConnection> layout = null;
	
	private List<LogEvent> myGraphEvolution;

	//a hidden graph that contains all the nodes that will ever be added... 
	//in order to calculate the positions of all the nodes
	private ReferencedNetworkGraph graph;
	
	//private P2PNetworkGraph fullGraph; 
	//private P2PNetworkGraph dynamicGraph = null;
	
	private List<LoadingListener> loadingListeners;
	
	private HTTPClient networkClient;
	
	private Component mainPane;
	
	private GraphPopupMenu popupMenu;
	
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

	protected EventPlayer eventThread;
	//[end] Protected Variables
	
	//[end] Attributes
	
	//[start] Constructor
	public P2PGraphViewer() {
		
		networkClient = new HTTPClient(this);
		
		loadingListeners = new LinkedList<LoadingListener>();
		init();
		start();
		JFrame frame = new JFrame();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(this);
		
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		
	}
	//[end] Constructor

	//[start] Initialization
	
	//[start] Calculate the Spring Layout
	/**
	 * Helper Method for setting up the spring layout.
	 * @return The initialized spring layout.
	 */
	/*private SpringLayout<P2PVertex,P2PConnection> springLayoutBuilder(int width, int height, P2PNetworkGraph graph) {
		final int numSteps = 100;
		for(LoadingListener l : loadingListeners) {
			l.loadingChanged(numSteps, "Spring Layout");
		}
		SpringLayout<P2PVertex,P2PConnection> sp_layout;
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
	}*/
	//[end] Calculate the Spring Layout
	
	//[start] Create Components
	
	//[start] File Menu
	private JMenuBar createFileMenu() {
		//[start] File Menu
		JMenu file = new JMenu("File");
		//[start] Connect Entry
		JMenuItem connect = new JMenuItem("Connect to..");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pauseButton.doClick();
				
				String option = (String)JOptionPane.showInputDialog(null, "Enter a URL:", "http://134.117.60.66:8080/graphServer");
				
				if(option != null ) {
					if(option.startsWith("http://")){
						//networkClient.closeNetwork();
						networkClient.startNetwork(option);
						//client.addNetworkListener();
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
					P2PNetworkGraphSaver saver = new P2PNetworkGraphSaver(graph.getDynamicGraph(),eventThread.getSaveEvents(), eventThread.getCurrentTime());
					saver.addProgressListener(new LoadingBar());
					saver.doSave();
				}
				else if(option == JOptionPane.NO_OPTION) {
					P2PNetworkGraphSaver saver = new P2PNetworkGraphSaver(graph.getDynamicGraph());
					saver.addProgressListener(new LoadingBar());
					saver.doSave();
				}
				//else cancel option, don't do anything
			}	
		});
		//[end] Save Entry
		
		//[start] Load Entry
		JMenuItem load = new JMenuItem("Load");
		load.addActionListener(new LoadListener());
		//[end] Load Entry
		
		//[start] Exit Entry
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent arg0) {
				pauseButton.doClick();
				int option = JOptionPane.showConfirmDialog(null, "Would you like to save before quitting?", "Save", 
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if(option == JOptionPane.OK_OPTION) {
					//GraphSaverAndLoader.save(graph.getDynamicGraph());
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
		JCheckBoxMenuItem logTable = new JCheckBoxMenuItem("Show Log Table");
		logTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if(myGraphEvolution != null) { //graph has been initialized
					
					JCheckBoxMenuItem button = (JCheckBoxMenuItem) ae.getSource();
                    if (button.isSelected()) { //Add the log table
                        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                        p.setResizeWeight(1);
                        p.add(mainPane);
                        p.add(initializeLogList(myGraphEvolution));
                        p.setDividerSize(3);

                        getContentPane().add(p);
                        validate();
                    } else { //Remove the log table
                        getContentPane().removeAll();
                        getContentPane().add(mainPane);
                        validate();
                    }
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
	
	//[start] Table Panel
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
		
		for(LoadingListener l : loadingListeners) {
			l.loadingComplete();
		}
		
		return tablePanel;
	}
	//[end] Table Panel
	
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
		
	//[end] Create Components
	
	//[start] init method
	/**
	 * applet initialization
	 */
	public void init() {
		
		//[start] Tabs Pane
		tabsPane = new JTabbedPane(JTabbedPane.TOP) {
			
			private static final long serialVersionUID = -4075340829665484983L;
			//private int i=0;
			@Override
			public void paint(Graphics g) {
				//catches an exception in the look and feel which I am not sure why it is happening or how to fix it
				try {
					super.paint(g);
				} catch(Exception ignored) {
				}
			}
			
		};
		JPanel initialTab = new JPanel();
		tabsPane.addTab("Welcome", initialTab);
		
		tabsPane.setEnabled(false);
		
		//[end] Tabs Pane
		
		JPanel graphsPanel = new JPanel(new GridLayout(1,1));
		graphsPanel.add(tabsPane);
				
		JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		p.setResizeWeight(1);
		p.add(graphsPanel);
		p.add(initializeSouthPanel());
		p.setDividerSize(0);
		p.setEnabled(false);
		
		
		getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
		//try set the size
		getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
		setJMenuBar(createFileMenu());
		getContentPane().add(p);
		setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));
		
		mainPane = p;
		
		//startGraph();
		loadingListeners.add(new LoadingBar());
	}
	//[end] init method

	//[start] Init Graph
	public void startGraph() {
		for(LoadingListener l : loadingListeners) {
			l.loadingStarted(7,"Building Visualizer");
		}
		for(ChangeListener listener : tabsPane.getListeners(ChangeListener.class)) {
			if(listener.getClass().equals(TabChangeListener.class)) {
				tabsPane.removeChangeListener(listener);
			}
		}
		tabsPane.removeAll();
		
		//layout = springLayoutBuilder(DEFWIDTH,DEFHEIGHT,graph.getReferenceGraph());
		layout = new FRLayout2<P2PVertex, P2PConnection>(graph.getReferenceGraph());
		layout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH,DEFHEIGHT)));
		
		for(LoadingListener l : loadingListeners) {
			l.loadingChanged(5, "Building Visualizer");
		}
		
		DefaultModalGraphMouse<P2PVertex,P2PConnection> gm = new DefaultModalGraphMouse<P2PVertex,P2PConnection>(); 
		MouseClickListener clickListener = new MouseClickListener();
		popupMenu = new GraphPopupMenu(this, gm, clickListener);
		
		//[start] Full Visualization Viewer Init
		fullViewViewer = new PersonalizedVisualizationViewer(layout, DEFWIDTH,DEFHEIGHT, gm, clickListener, graph.getReferenceGraph());
		fullViewViewer.setName("Full View");
		//add my own vertex shape & color fill transformers
		fullViewViewer.setSpecialTransformers(VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
								EdgeShapeType.QUAD_CURVE,
								EdgeShapeType.CUBIC_CURVE,
								EdgeShapeType.LINE,
								EdgeShapeType.LINE);
		
		fullViewViewer.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(graph.getDynamicGraph()));
		fullViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(graph.getDynamicGraph()));
		//[end] Full Visualization Viewer Init
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(1);
		}
		//[start] Collapsed Document Visualization Viewer Init
		collapsedDocumentViewViewer = new PersonalizedVisualizationViewer(fullViewViewer.getGraphLayout(), DEFWIDTH,DEFHEIGHT, 
				gm, clickListener, graph.getReferenceGraph());
		collapsedDocumentViewViewer.setName("Collapsed Document View");		
		collapsedDocumentViewViewer.getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(
				collapsedDocumentViewViewer.getPickedVertexState(),Color.RED, Color.YELLOW, Color.MAGENTA, Color.RED, Color.RED, Color.BLUE));
		collapsedDocumentViewViewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(
				VertexShapeType.ELLIPSE, VertexShapeType.PENTAGON, VertexShapeType.ELLIPSE, 
				P2PVertexShapeTransformer.PEER_SIZE, P2PVertexShapeTransformer.DOC_SIZE, P2PVertexShapeTransformer.PEER_SIZE));
		collapsedDocumentViewViewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
		collapsedDocumentViewViewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer(EdgeShapeType.QUAD_CURVE,
				EdgeShapeType.CUBIC_CURVE,EdgeShapeType.LINE,EdgeShapeType.LINE)); //stroke width
		
		collapsedDocumentViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(graph.getDynamicGraph(),PeerVertex.class));
		collapsedDocumentViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(graph.getDynamicGraph()));
		//[end] Collapsed Document Visualization Viewer Init
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(2);
		}
		//[start] Collapsed Peer Visualization Viewer Init
		collapsedPeerViewViewer = new PersonalizedVisualizationViewer(fullViewViewer.getGraphLayout(), DEFWIDTH,DEFHEIGHT, 
				gm, clickListener, graph.getReferenceGraph());
		collapsedPeerViewViewer.addMouseListener(clickListener);
		collapsedPeerViewViewer.setName("Collapsed Peer View");
		collapsedPeerViewViewer.setSpecialTransformers(VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
								EdgeShapeType.QUAD_CURVE,
								EdgeShapeType.CUBIC_CURVE,
								EdgeShapeType.LINE,
								EdgeShapeType.LINE);
		
		collapsedPeerViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(graph.getDynamicGraph(), DocumentVertex.class));
		collapsedPeerViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(graph.getDynamicGraph()));
		//[end] Collapsed Peer Visualization Viewer Init
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(3);
		}
		//[start] Collapsed Peer AndDocument Visualization Viewer Init
		collapsedPeerAndDocumentViewViewer = new PersonalizedVisualizationViewer(fullViewViewer.getGraphLayout(), DEFWIDTH,DEFHEIGHT, 
				gm, clickListener, graph.getReferenceGraph());
		collapsedPeerAndDocumentViewViewer.addMouseListener(clickListener);
		collapsedPeerAndDocumentViewViewer.setName("Collapsed Peer and Document View");
		collapsedPeerAndDocumentViewViewer.setSpecialTransformers(VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
								EdgeShapeType.QUAD_CURVE,
								EdgeShapeType.CUBIC_CURVE,
								EdgeShapeType.LINE,
								EdgeShapeType.LINE);
		
		collapsedPeerAndDocumentViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(graph.getDynamicGraph(),PeerDocumentVertex.class));
		collapsedPeerAndDocumentViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(graph.getDynamicGraph()));
		//[end] Collapsed Peer AndDocument Visualization Viewer Init
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(4);
		}
		
		if(myGraphEvolution.isEmpty()) {
			SliderListener s = new SliderListener();
			
			playbackSlider.setMaximum(0);
			playbackSlider.addChangeListener(s);
			playbackSlider.addMouseListener(s);
			
			/// create the event player
			eventThread = new EventPlayer(graph);
			eventThread.addEventPlayerListener(this);
		}
		else {
			SliderListener s = new SliderListener();
			//playbackSlider.setMinimum((int)myGraphEvolution.getFirst().getTime());
			playbackSlider.setMinimum(0);
			playbackSlider.setMaximum((int)myGraphEvolution.get(myGraphEvolution.size()-1).getTime());
			playbackSlider.addChangeListener(s);
			playbackSlider.addMouseListener(s);
			
			
			/// create the event player
			eventThread = new EventPlayer(graph, myGraphEvolution, playbackSlider);
			eventThread.addEventPlayerListener(this);
		}
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(5);
		}
		
		tabsPane.addTab(collapsedPeerViewViewer.getName(), collapsedPeerViewViewer);
		tabsPane.addTab(collapsedDocumentViewViewer.getName(), collapsedDocumentViewViewer);
		tabsPane.addTab(collapsedPeerAndDocumentViewViewer.getName(), collapsedPeerAndDocumentViewViewer);
		tabsPane.addTab(fullViewViewer.getName(),fullViewViewer);
		tabsPane.setEnabled(true);
		tabsPane.setIgnoreRepaint(false);
		tabsPane.addChangeListener(new TabChangeListener());
		
		currentViewer = (PersonalizedVisualizationViewer) ((JTabbedPane) tabsPane).getSelectedComponent();
		
		for(LoadingListener l : loadingListeners) {
			l.loadingComplete();
		}
		
		fastReverseButton.setEnabled(true);
		reverseButton.setEnabled(true);
		pauseButton.setEnabled(true);
		forwardButton.setEnabled(false);
		fastForwardButton.setEnabled(true);
		playbackSlider.setEnabled(true);
		fastSpeedSlider.setEnabled(true);
		
		layout.lock(true);
		doRepaint();
		eventThread.run();
	}
	//[end] Init Graph
	
	//[end] Initialization
	
	//[start] Main
	/**
	 * 
	 * to run this applet as a java application
	 * @param args optional argument : the log file to process
	 */
	public static void main(String[] args) {

		P2PGraphViewer myapp = new P2PGraphViewer();
		myapp.validate();
	}
	//[end] Main

	//[start] Helper Methods
	
	
	
	//[start] getters
	
	public ReferencedNetworkGraph getGraph() {
		return graph;
	}
	
	public PersonalizedVisualizationViewer getCurrentViewer() {
		return currentViewer;
	}
	
	//[end] getters
	
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
	
	//[start] Tabs Listener
	/**
     * If the graphsPanel is a JTabbedPane, updates the current viewer when it is changed
     * so that the playbackPanel knows which viewer to repaint
     */
    public class TabChangeListener implements ChangeListener{

        public void stateChanged(ChangeEvent e) {
            currentViewer = (PersonalizedVisualizationViewer) ((JTabbedPane) tabsPane).getSelectedComponent();
        }

    }
    //[end] Tabs Listener
	
	//[start] Relaxer and mouse context listener
	 /**
	 * an actionlistener that defines the use of a button to stop the spring-layout processing
	 * @author adavoust
	 *
	 */
	private class MouseClickListener extends MouseAdapter implements ActionListener {
				
		@Override 
		public void mousePressed(MouseEvent e) {
			currentViewer = (PersonalizedVisualizationViewer) e.getComponent();
			if (e.isPopupTrigger()) {
                popupMenu.showPopupMenu(e.getX(), e.getY());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //Windows only shows the popup menu on right-click release.  Silly Windows
            if (System.getProperty("os.name").toLowerCase().startsWith("windows") && e.isPopupTrigger()){
                popupMenu.showPopupMenu(e.getX(), e.getY());
            }
        }

        public void actionPerformed(ActionEvent e) {
            popupMenu.popupMenuEvent(((AbstractButton)e.getSource()).getText());
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
	/**
	 * @author  Matty
	 */
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
		public void mousePressed(MouseEvent e) {
			if(((JSlider)(e.getSource())).isEnabled()){
				prevState = eventThread.getPlayState();
				eventThread.pause();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(((JSlider)(e.getSource())).isEnabled()){
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
	
	
	//[start] Load Listener
	class LoadListener implements ActionListener {
	
		public void actionPerformed(ActionEvent arg0) {
			
			pauseButton.doClick();
			Thread loadingThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					P2PNetworkGraphLoader loader = new P2PNetworkGraphLoader();
					loader.addProgressListener(new LoadingBar());
					if(loader.doLoad()) {
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
							
							//When loading a new Graph, if the collapsed document view has a tree layout it crashes because of setsize()
							AbstractLayout<P2PVertex, P2PConnection> graphLayout = new CircleLayout<P2PVertex, P2PConnection>(graph.getReferenceGraph());
							collapsedDocumentViewViewer.getModel().setGraphLayout(graphLayout);
						}
						myGraphEvolution = loader.getLogList();
						graph = loader.getGraph();
						//dynamicGraph = loader.getDynamicP2PNetworkGraph();
						startGraph();
						eventThread.setRobustMode(false);
					}
				}
			});
			loadingThread.start();
		}
	}
	//[end] Load Listener
	
	//[end] Swing Event Listeners
	
	//[start] Network Listeners
	@Override
	public synchronized void incomingLogEvents(InputStream inStream) {
		try {
			eventThread.pause();
			List<LogEvent> events;
			synchronized(graph.getReferenceGraph()) {
				events = P2PNetworkGraphLoader.buildLogs(inStream, networkClient, graph);
			}
			if(!events.isEmpty()) {				
				for(int i=0;i<events.size();i++) {
					graph.getReferenceGraph().robustGraphEvent(events,i); //apply events to graph
				}//any events that didn't match up with the current graph will have been handled and new events created to compensate.
				events.add(LogEvent.getEndEvent(events.get(events.size()-1)));
				eventThread.addEvents(events);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void incomingGraph(InputStream inStream) {
		
		try {
			P2PNetworkGraphLoader loader = P2PNetworkGraphLoader.buildGraph(inStream, networkClient);
			
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
				
				//When loading a new Graph, if the collapsed document view has a tree layout it crashes because of setsize()
				AbstractLayout<P2PVertex, P2PConnection> graphLayout = new CircleLayout<P2PVertex, P2PConnection>(graph.getReferenceGraph());
				collapsedDocumentViewViewer.getModel().setGraphLayout(graphLayout);
			}
			
			myGraphEvolution = loader.getLogList();
			graph = loader.getGraph();
			//dynamicGraph = loader.getDynamicP2PNetworkGraph();
			startGraph();
			eventThread.setRobustMode(true);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	//[end] Network Listeners
}