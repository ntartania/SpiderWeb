/*
 * File:         P2PGraphVeiwer.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 29/08/2011 
 * Author:       Matthew Smith
 * 				 Alan Davoust
 * 				 Andrew O'Hara
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.jdom.JDOMException;

import spiderweb.graph.LogEvent;
import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PVertex;
import spiderweb.graph.ReferencedNetworkGraph;
import spiderweb.graph.savingandloading.DocumentGraphLoader;
import spiderweb.graph.savingandloading.LoadingListener;
import spiderweb.graph.savingandloading.P2PNetworkGraphLoader;
import spiderweb.graph.savingandloading.P2PNetworkGraphSaver;
import spiderweb.networking.ConnectDialog;
import spiderweb.networking.HTTPClient;
import spiderweb.networking.NetworkGraphListener;
import spiderweb.visualizer.NetworkGraphVisualizer;
import spiderweb.visualizer.ViewState;
import spiderweb.visualizer.eventplayer.EventPlayer;
import spiderweb.visualizer.eventplayer.EventPlayerListener;
import spiderweb.visualizer.eventplayer.PlayState;
import spiderweb.visualizer.transformers.P2PVertexPlacer;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;

/**
 * P2PGraphViewer contains the main entry point and contains the
 * primary functionality and glue which holds together the SpiderWeb project's
 * application. P2PGraphViewer contains the primary swing components 
 * and maintains any graphs, graph visualizers and event players.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @author Alan Davoust
 * @author Andrew O'Hara
 * @version Date: 29/08/2011 
 */
public class P2PGraphViewer extends JApplet implements EventPlayerListener, NetworkGraphListener {
	/**eclipse generated serial UID*/
	private static final long serialVersionUID = -1536972184940704765L;

	//default size for the swing graphic components
	public static final int DEFWIDTH = 1360;
	public static final int DEFHEIGHT = 768;

	// List of loading listeners for visualizing the time it will take to load
	protected List<LoadingListener> loadingListeners;
	
	protected LogEventTable eventTable;
	protected JPanel graphsPanel;
	protected PlaybackPanel playbackPanel;
	protected EventPlayer eventThread;
	protected Component mainPane;
	protected GraphPopupMenu popupMenu;

	protected HTTPClient networkClient;

	protected List<LogEvent> logEvents;
	protected ReferencedNetworkGraph graph;
	protected NetworkGraphVisualizer visualizer;

	/**
	 * P2PGraphViewer contains the main entry point and contains the
	 * primary functionality and glue which holds together the SpiderWeb project's
	 * application. P2PGraphViewer contains the primary swing components 
	 * and maintains any graphs, graph visualizers and event players.
	 */
	public P2PGraphViewer() {
		init();
		start();
	}

	/**
	 * Helper method creates the 'file' menu bar at the top of the screen; adds each 
	 * component to its respective sub menu and defines their action when pressed.
	 * @return <code>JMenuBar</code> Containing the 'file' menu bar for SpiderWeb
	 */
	private JMenuBar createFileMenu() {
		JMenu file = new JMenu("File");
		{ //Connect Entry
			JMenuItem connect = new JMenuItem("Connect to..");
			connect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(eventThread != null) {
						eventThread.pause();
					}
	
					String url = ConnectDialog.getConnectURL();
	
					if(!url.equals(ConnectDialog.CANCELED)){
						networkClient.startNetwork(url);
					}
					//else cancel option, don't do anything
				}	
			});
			file.add(connect);
		}
		{ //Save and Load Entry
			JMenuItem save = new JMenuItem("Save");
			save.addActionListener(new SaveListener());
			
			JMenuItem load = new JMenuItem("Load");
			load.addActionListener(new LoadListener());
			
			JMenuItem loadDocumentGraph = new JMenuItem("Load Document Graph");
			loadDocumentGraph.addActionListener(new LoadDocumentGraphListener());
			
			file.addSeparator();
			file.add(save);
			file.add(load);
			file.add(loadDocumentGraph);
		}
		{ //Exit Entry
			JMenuItem exit = new JMenuItem("Exit");
			exit.addActionListener(new ActionListener() {	
				public void actionPerformed(ActionEvent arg0) {
					if(eventThread != null) {
						eventThread.pause();
					}
					int option = JOptionPane.showConfirmDialog(null, "Would you like to save before quitting?", "Save", 
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	
					if(option == JOptionPane.OK_OPTION) {
						//GraphSaverAndLoader.save(graph.getDynamicGraph());
					}
	
					System.exit(0);
				}	
			});
			
			file.addSeparator();
			file.add(exit);
		}

		JMenu window = new JMenu("Window");
		ButtonGroup windowGroup = new ButtonGroup();
		
		{ //Just Graph Entry
			JRadioButtonMenuItem noneItem = new JRadioButtonMenuItem("Just Graph");
			noneItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					getContentPane().removeAll();
					getContentPane().add(mainPane);
					validate();
				}
			});
			window.add(noneItem);
			windowGroup.add(noneItem);
			noneItem.setSelected(true);
		}
		{ //Log Table Entry
			JRadioButtonMenuItem logTableItem = new JRadioButtonMenuItem("Show Log Table");
			logTableItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if(logEvents != null) { //graph has been initialized
						JRadioButtonMenuItem button = (JRadioButtonMenuItem) ae.getSource();
						if (button.isSelected()) { //Add the log table
							JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
							p.setResizeWeight(0.8f);
							p.add(mainPane);
							p.add(eventTable.getInPanel());
							p.setDividerSize(3);
	
							getContentPane().removeAll();
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
			window.add(logTableItem);
			windowGroup.add(logTableItem);
		}
		{ //Visual Options Entry
			JRadioButtonMenuItem visualOptionsItem = new JRadioButtonMenuItem("Show Visual Options Pane");
			visualOptionsItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if(logEvents != null) { //graph has been initialized
						JRadioButtonMenuItem button = (JRadioButtonMenuItem) ae.getSource();
						if (button.isSelected()) { //Add the log table
							JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
							p.setResizeWeight(0.75f);
							p.add(mainPane);
							p.add(visualizer.getOptionsPanel());
							p.setDividerSize(3);
	
							getContentPane().removeAll();
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
			window.add(visualOptionsItem);
			windowGroup.add(visualOptionsItem);
		}

		JMenuBar bar = new JMenuBar();
		bar.add(file);
		bar.add(window);
		bar.setVisible(true);
		return bar;
	}

	@Override
	public void init() {
		setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));

		networkClient = new HTTPClient(this);

		loadingListeners = new LinkedList<LoadingListener>();

		graphsPanel = new JPanel(new GridLayout(1,1));
		graphsPanel.setSize(800,600);
		graphsPanel.setBorder(BorderFactory.createTitledBorder("Graphs"));
		//graphsPanel.add(tabsPane);

		playbackPanel = new PlaybackPanel();

		JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		p.setResizeWeight(1);
		p.add(graphsPanel);
		p.add(playbackPanel);
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
		loadingListeners.add(playbackPanel);
	}

	/**
	 * Helper Method initializes and starts all the nessisary components that 
	 * are used for the playback of a Network Graph
	 */
	private void startGraph() {
		AbstractLayout<P2PVertex, P2PConnection> layout = new FRLayout2<P2PVertex, P2PConnection>(graph.getFullGraph());
		layout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH,DEFHEIGHT)));

		for(LoadingListener l : loadingListeners) {
			l.loadingStarted(3, "Building Visualizer");
		}

		DefaultModalGraphMouse<P2PVertex,P2PConnection> gm = new DefaultModalGraphMouse<P2PVertex,P2PConnection>(); 
		MouseClickListener clickListener = new MouseClickListener();

		visualizer = NetworkGraphVisualizer.getPersonalizedVisualizer(layout, gm, graph, graphsPanel.getWidth(), graphsPanel.getHeight());
		visualizer.addMouseListener(clickListener);//This listener handles the mouse clicks to see if a popup event was done
		
		popupMenu = new GraphPopupMenu(visualizer, gm);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(1);
		}
		
		if(logEvents.isEmpty()) {
			playbackPanel.getPlaybackSlider().setMaximum(0);

			/// create the event player
			eventThread = new EventPlayer(graph);
		}
		else {
			playbackPanel.getPlaybackSlider().setMaximum((int)logEvents.get(logEvents.size()-1).getTime());

			/// create the event player
			eventThread = new EventPlayer(graph, logEvents, playbackPanel.getPlaybackSlider());
		}
		eventThread.addEventPlayerListener(this);
		eventTable = new LogEventTable(logEvents, eventThread);


		playbackPanel.getPlaybackSlider().addChangeListener(eventTable);

		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(2);
		}

		graphsPanel.add(visualizer);
		
		for(LoadingListener l : loadingListeners) {
			l.loadingComplete();
		}

		playbackPanel.startPlayback(eventThread);

		for(LoadingListener l : loadingListeners) {
			l.loadingProgress(3);
		}
		
		layout.lock(true);
		validate();
		doRepaint();
		eventThread.beginPlayback();
	}

	/**
	 * Returns the application's <code>ReferencedNetworkGraph</code>
	 * @return <code>ReferencedNetworkGraph</code> being displayed in this application.
	 */
	public ReferencedNetworkGraph getGraph() {
		return graph;
	}

	/**
	 * Returns the application's <code>NetworkGraphVisualizer</code>
	 * @return <code>NetworkGraphVisualizer</code> being displayed in this application.
	 */
	public NetworkGraphVisualizer getVisualizer() {
		return visualizer;
	}


	@Override
	public void stateChanged(PlayState state) {
		playbackPanel.updateButtons(state);
	}

	@Override
	public void doRepaint() {
		visualizer.repaint();
	}

	/**
	 * Listener for checking when the mouse has triggered the right click context menu.
	 * 
	 * @author Andrew O'Hara
	 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
	 */
	private class MouseClickListener extends MouseAdapter {

		@Override 
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				popupMenu.showPopupMenu(e.getX(), e.getY());
			}
		}
	}

	/**
	 * Listener for Loading a new file to be played in the viewer/player.
	 * 
	 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
	 */
	class LoadListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			Thread loadingThread = new Thread(new Runnable() {

				@Override
				public void run() {
					if(eventThread !=null) {
						eventThread.pause();
					}
					P2PNetworkGraphLoader loader = new P2PNetworkGraphLoader();
					loader.addProgressListener(playbackPanel);
					if(loader.doLoad()) {
						getContentPane().removeAll();
						getContentPane().add(mainPane);
						validate();
						if(logEvents != null) {
							graphsPanel.removeAll();
							logEvents.clear();
							eventThread.stopPlayback();
							playbackPanel.stopPlayback();
						}
						logEvents = loader.getLogList();
						graph = loader.getGraph();
						startGraph();
						eventThread.setRobustMode(false);
					}
				}
			});
			loadingThread.start();
		}
	}
	
	/**
	 * Loader for loading a special type of document graph file for displaying in the viewer/player.
	 * 
	 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
	 */
	class LoadDocumentGraphListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// a thread to load the graph on so it is not on Swing's event dispatch thread.
			Thread loadingThread = new Thread(new Runnable() {

				@Override
				public void run() {
					if(eventThread !=null) {
						eventThread.pause();
					}
					DocumentGraphLoader loader = new DocumentGraphLoader();
					loader.addProgressListener(playbackPanel);
					if(loader.doLoad()) {
						getContentPane().removeAll();
						getContentPane().add(mainPane);
						validate();
						if(logEvents != null) {
							graphsPanel.removeAll();
							logEvents.clear();
							eventThread.stopPlayback();
							playbackPanel.stopPlayback();
						}
						logEvents = loader.getLogList();
						graph = loader.getGraph();
						startGraph();
						eventThread.setRobustMode(false);
						visualizer.setView(ViewState.CollapsedDocumentView);
					}
				}
			});
			loadingThread.start();
		}
	}

	/**
	 * Listener for saving the currently viewed graph to an xml file.
	 * 
	 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
	 */
	class SaveListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(logEvents != null) {
				eventThread.pause();

				int option = JOptionPane.showConfirmDialog(null, "Would you like to save the log events after this graph snapshot", 
						"Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

				if(option == JOptionPane.YES_OPTION) {
					P2PNetworkGraphSaver saver = new P2PNetworkGraphSaver(graph.getDynamicGraph(),eventThread.getSaveEvents(), eventThread.getCurrentTime());
					saver.addProgressListener(playbackPanel);
					saver.doSave();
				}
				else if(option == JOptionPane.NO_OPTION) {
					P2PNetworkGraphSaver saver = new P2PNetworkGraphSaver(graph.getDynamicGraph());
					saver.addProgressListener(playbackPanel);
					saver.doSave();
				}
				//else cancel option, don't do anything
			}
		}	
	}
	
	@Override
	public synchronized void incomingLogEvents(InputStream inStream) {
		try {

			List<LogEvent> events;
			synchronized(graph.getFullGraph()) {
				events = P2PNetworkGraphLoader.buildLogs(inStream, networkClient, graph);
			}
			if(!events.isEmpty()) {	
				//eventThread.pause();
				for(int i=0;i<events.size();i++) {
					graph.getFullGraph().robustGraphEvent(events,i); //apply events to graph
				}//any events that didn't match up with the current graph will have been handled and new events created to compensate.
				events.add(LogEvent.getEndEvent(events.get(events.size()-1)));
				eventThread.addEvents(events);
				eventTable.addEvents(events);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void incomingGraph(InputStream inStream) {
		
		try {
			P2PNetworkGraphLoader loader = P2PNetworkGraphLoader.buildGraph(inStream, networkClient);
			getContentPane().removeAll();
			getContentPane().add(mainPane);
			validate();
			if(logEvents != null) {
				graphsPanel.removeAll();
				logEvents.clear();
				eventThread.stopPlayback();
				playbackPanel.stopPlayback();
			}
			logEvents = loader.getLogList();
			graph = loader.getGraph();
			startGraph();
			eventThread.setRobustMode(true);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * To run this applet as a java application
	 */
	public static void main(String[] args) {

		P2PGraphViewer graphViewer = new P2PGraphViewer();

		JFrame frame = new JFrame();
		frame.add(graphViewer);
		frame.setVisible(true);
		frame.setSize(new Dimension(800,600));
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}