/*
 * File:         VisualOptionsPanel.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      15/08/2011
 * Last Changed: Date: 19/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import spiderweb.visualizer.predicates.PredicateChangeEvent;
import spiderweb.visualizer.predicates.PredicateChangeListener;
import spiderweb.visualizer.transformers.TransformerChangeEvent;
import spiderweb.visualizer.transformers.TransformerChangeListener;

/**
 * The Visual Options Panel is a user interface for allowing the changing of  
 * options in the way that the graph is viewed.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 19/08/2011 
 */
public class VisualOptionsPanel extends JPanel implements ActionListener, ChangeListener {

	/**eclipse generated Serial UID*/
	private static final long serialVersionUID = 3026513624881148363L;

	private JRadioButton collapsedPeerAndDocumentView;
	private JRadioButton collapsedDocumentView;
	private JRadioButton collapsedPeerView;
	private JRadioButton fullView;

	private JCheckBox showHiddenVertices;
	private JCheckBox disregardTime;

	private JCheckBox inEdgePeerCutoff;
	private JCheckBox outEdgePeerCutoff;
	private JSpinner minimumInEdgePeerCutoff;
	private JSpinner minimumOutEdgePeerCutoff;

	private JCheckBox inEdgeDocumentCutoff;
	private JCheckBox outEdgeDocumentCutoff;
	private JSpinner minimumInEdgeDocumentCutoff;
	private JSpinner minimumOutEdgeDocumentCutoff;


	/*List of View Change Listeners for notifying*/
	private List<ViewChangeListener> viewListeners;

	/**
	 * Add a <code>ViewChangeListener</code> to the panel.
	 * @param listener the <code>ViewChangeListener</code> to be added
	 */
	public void addViewListener(ViewChangeListener listener) {
		viewListeners.add(listener);
	}

	/**
	 * Removes a <code>ViewChangeListener</code> from the panel.
	 * @param listener the <code>ViewChangeListener</code> to be removed
	 */
	public void removeViewListener(ViewChangeListener listener) {
		viewListeners.remove(listener);
	}

	/*List of Predicate Change Listeners for notifying*/
	private List<PredicateChangeListener> predicateListeners;
	/**
	 * Add a <code>PredicateChangeListener</code> to the panel.
	 * @param listener the <code>PredicateChangeListener</code> to be added
	 */
	public void addPredicateListener(PredicateChangeListener listener) {
		predicateListeners.add(listener);
	}
	/**
	 * Removes a <code>PredicateChangeListener</code> from the panel.
	 * @param listener the <code>PredicateChangeListener</code> to be removed
	 */
	public void removePredicateListener(PredicateChangeListener listener) {
		predicateListeners.remove(listener);
	}

	/*List of Transformer Change Listeners for notifying*/
	private List<TransformerChangeListener> transformerListeners;
	/**
	 * Add a <code>TransformerChangeListener</code> to the panel.
	 * @param listener the <code>TransformerChangeListener</code> to be added
	 */
	public void addTransformerListener(TransformerChangeListener listener) {
		transformerListeners.add(listener);
	}
	/**
	 * Removes a <code>TransformerChangeListener</code> from the panel.
	 * @param listener the <code>TransformerChangeListener</code> to be removed
	 */
	public void removeTransformerListener(TransformerChangeListener listener) {
		transformerListeners.remove(listener);
	}

	/**
	 * for testing purposes to see how the layout looks
	 */
	public static void main(String[] args) {
		VisualOptionsPanel test = new VisualOptionsPanel();
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(test, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.setSize(new Dimension(300,720));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * The Visual Options Panel is a user interface for allowing the changing of  
	 * options in the way that the graph is viewed.
	 */
	public VisualOptionsPanel() {
		super(new BorderLayout());
		buildPanel();

		predicateListeners = new LinkedList<PredicateChangeListener>();
		transformerListeners = new LinkedList<TransformerChangeListener>();
		viewListeners = new LinkedList<ViewChangeListener>();
	}

	/**
	 * Changes the selected radio button to the corresponding <code>ViewState</code> and 
	 * informs all view listeners of the new <code>ViewState</code>
	 * @param view the <code>ViewState</code> to change to
	 */
	public void setView(ViewState view) {
		switch(view) {
		case FullView:
			fullView.doClick();
			break;
		case CollapsedPeerView:
			collapsedPeerView.doClick();
			break;
		case CollapsedDocumentView:
			collapsedDocumentView.doClick();
			break;
		case CollapsedPeerAndDocumentView:
			collapsedPeerAndDocumentView.doClick();
			break;
		}
		repaint();
	}

	/**
	 * Helper for building the visual options panel to condense the code in the constructor
	 */
	private void buildPanel() {
		JPanel scrollablePanel = new JPanel(new BorderLayout());

		JPanel predicateOptions = new JPanel();
		scrollablePanel.add(predicateOptions, BorderLayout.SOUTH);

		scrollablePanel.add(getViewPanel(),BorderLayout.NORTH);
		scrollablePanel.add(getTransformerOptions(),BorderLayout.CENTER);
		scrollablePanel.add(getPredicateOptions(),BorderLayout.SOUTH);

		JScrollPane listScroller = new JScrollPane(scrollablePanel);
		listScroller.setWheelScrollingEnabled(true);
		listScroller.setSize(scrollablePanel.getWidth(),scrollablePanel.getHeight());

		add(listScroller, BorderLayout.CENTER);
	}

	/**
	 * Helper for building the Graph Transformer Options Panel
	 * @return a <code>JPanel</code> containing other panels with components for modifying the graph's transformers
	 */
	private JPanel getTransformerOptions() {
		JPanel transformerOptions = new JPanel(new BorderLayout()); 
		transformerOptions.setBorder(BorderFactory.createTitledBorder("Transformer Options"));

		JPanel panelForProperLayout = new JPanel(new BorderLayout());
		panelForProperLayout.add(getScalePanel(), BorderLayout.NORTH);

		transformerOptions.add(panelForProperLayout,BorderLayout.NORTH);

		return transformerOptions;
	}

	/**
	 * Helper for building the Graph Predicate Options Panel
	 * @return a <code>JPanel</code> containing other panels with components for modifying the graph's predicates
	 */
	private JPanel getPredicateOptions() {
		JPanel predicateOptions = new JPanel(new BorderLayout()); 
		predicateOptions.setBorder(BorderFactory.createTitledBorder("Predicate Options"));

		JPanel panelForProperLayout = new JPanel(new BorderLayout());
		panelForProperLayout.add(getHiddenAndTimelessOptions(), BorderLayout.NORTH);
		panelForProperLayout.add(getVertexVisibilityPanel(), BorderLayout.CENTER);
		panelForProperLayout.add(getTemperalQueryPanel(), BorderLayout.SOUTH);

		predicateOptions.add(panelForProperLayout,BorderLayout.NORTH);

		return predicateOptions;
	}

	/**
	 * Helper for building a panel containing two checkboxes to ensure layout consistency
	 * @return a <code>JPanel</code> containing the Check Boxes for showing vertices when specified otherwise
	 */
	private JPanel getHiddenAndTimelessOptions() {
		JPanel p = new JPanel(new GridLayout(2,1));

		showHiddenVertices = new JCheckBox("Show Hidden Vertices");
		showHiddenVertices.addActionListener(this);
		showHiddenVertices.setToolTipText("Show Vertices hidden from the right click menu.");
		p.add(showHiddenVertices);

		disregardTime = new JCheckBox("Show Vertices Regardless of Time");
		disregardTime.addActionListener(this);
		disregardTime.setToolTipText("Show all vertices regardless of the time through playback");
		p.add(disregardTime);

		return p;
	}

	/**
	 * Helper for building the place-holder panel representing the temperal query information
	 * @return a <code>JPanel</code> containing place-holder buttons and input for doing temperal graph queries 
	 */
	private JPanel getTemperalQueryPanel() {
		JPanel temperalQueryPanel = new JPanel(new BorderLayout());
		temperalQueryPanel.setBorder(BorderFactory.createTitledBorder("Temperal Graph Query"));
		temperalQueryPanel.setToolTipText("");

		JPanel timePanel = new JPanel();
		temperalQueryPanel.add(timePanel, BorderLayout.NORTH);
		timePanel.setLayout(new GridLayout(3,1));

		ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButton allTimeOption = new JRadioButton("All Time");
		allTimeOption.setSelected(true);
		timePanel.add(allTimeOption);
		buttonGroup.add(allTimeOption);

		JRadioButton anyTimeOption = new JRadioButton("Any Time");
		timePanel.add(anyTimeOption);
		buttonGroup.add(anyTimeOption);

		JPanel atTimePanel = new JPanel();
		timePanel.add(atTimePanel);
		atTimePanel.setLayout(new BorderLayout());

		JRadioButton timeEqualsOption = new JRadioButton("At Time t =");
		atTimePanel.add(timeEqualsOption, BorderLayout.WEST);
		buttonGroup.add(timeEqualsOption);

		JFormattedTextField formattedTextField = new JFormattedTextField();
		formattedTextField.setText("5");
		atTimePanel.add(formattedTextField, BorderLayout.CENTER);

		JTextPane regex = new JTextPane();
		regex.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		regex.setText("some sort of regular expression?");
		temperalQueryPanel.add(regex, BorderLayout.SOUTH);
		
		return temperalQueryPanel;
	}

	/**
	 * Helper for building the Graph Vertex Degree Cutoff Panel
	 * @return a <code>JPanel</code> containing the buttons and input boxes for hiding vertices which do not meet specified parameters
	 */
	private JPanel getVertexVisibilityPanel() {
		JPanel vertexVisiblityPanel = new JPanel(new GridLayout(2,1));
		vertexVisiblityPanel.setBorder(BorderFactory.createTitledBorder("Vertex Degree Cutoff"));
		vertexVisiblityPanel.setToolTipText("Hide vertices which do not meet specified parameters");

		JPanel hidePeers = new JPanel(new GridLayout(2,1));
		hidePeers.setName("peer degree cutoff");
		hidePeers.setBorder(BorderFactory.createTitledBorder("Peers"));
		hidePeers.setToolTipText("Hide peer vertices on select parameters");
		vertexVisiblityPanel.add(hidePeers);
		{
			JPanel inDegreePanel = new JPanel(new BorderLayout());
			hidePeers.add(inDegreePanel);

			inEdgePeerCutoff = new JCheckBox("In Edge Count     X");
			inEdgePeerCutoff.addActionListener(this);
			inEdgePeerCutoff.setToolTipText("Hide Peers on the number of edges entering");
			inDegreePanel.add(inEdgePeerCutoff, BorderLayout.WEST);

			minimumInEdgePeerCutoff = new JSpinner();
			minimumInEdgePeerCutoff.addChangeListener(this);
			inDegreePanel.add(minimumInEdgePeerCutoff, BorderLayout.CENTER);
			minimumInEdgePeerCutoff.setToolTipText("minimum number of edges cutoff");
			minimumInEdgePeerCutoff.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		}

		{
			JPanel outDegreePanel = new JPanel(new BorderLayout());
			hidePeers.add(outDegreePanel);

			outEdgePeerCutoff = new JCheckBox("Out Edge Count  X");
			outEdgePeerCutoff.addActionListener(this);
			outEdgePeerCutoff.setToolTipText("Hide Peers on the number of edges leaving");
			outDegreePanel.add(outEdgePeerCutoff, BorderLayout.WEST);

			minimumOutEdgePeerCutoff = new JSpinner();
			minimumOutEdgePeerCutoff.addChangeListener(this);
			outDegreePanel.add(minimumOutEdgePeerCutoff, BorderLayout.CENTER);
			minimumOutEdgePeerCutoff.setToolTipText("minimum number of edges cutoff");
			minimumOutEdgePeerCutoff.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		}
		JPanel hideDocuments = new JPanel(new GridLayout(2,1));
		hideDocuments.setName("document degree cutoff");
		hideDocuments.setBorder(BorderFactory.createTitledBorder("Documents"));
		hideDocuments.setToolTipText("Hide document vertices on select parameters");
		vertexVisiblityPanel.add(hideDocuments);

		{
			JPanel inDegreePanel = new JPanel(new BorderLayout());
			hideDocuments.add(inDegreePanel);

			inEdgeDocumentCutoff = new JCheckBox("In Edge Count     X");
			inEdgeDocumentCutoff.addActionListener(this);
			inEdgeDocumentCutoff.setToolTipText("Hide documents on the number of edges entering");
			inDegreePanel.add(inEdgeDocumentCutoff, BorderLayout.WEST);

			minimumInEdgeDocumentCutoff = new JSpinner();
			minimumInEdgeDocumentCutoff.addChangeListener(this);
			inDegreePanel.add(minimumInEdgeDocumentCutoff, BorderLayout.CENTER);
			minimumInEdgeDocumentCutoff.setToolTipText("minimum number of edges cutoff");
			minimumInEdgeDocumentCutoff.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		}

		{
			JPanel outDegreePanel = new JPanel(new BorderLayout());
			hideDocuments.add(outDegreePanel);

			outEdgeDocumentCutoff = new JCheckBox("Out Edge Count  X");
			outEdgeDocumentCutoff.addActionListener(this);
			outEdgeDocumentCutoff.setToolTipText("Hide documents on the number of edges leaving");
			outDegreePanel.add(outEdgeDocumentCutoff, BorderLayout.WEST);

			minimumOutEdgeDocumentCutoff = new JSpinner();
			minimumOutEdgeDocumentCutoff.addChangeListener(this);
			outDegreePanel.add(minimumOutEdgeDocumentCutoff, BorderLayout.CENTER);
			minimumOutEdgeDocumentCutoff.setToolTipText("minimum number of edges cutoff");
			minimumOutEdgeDocumentCutoff.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		}

		return vertexVisiblityPanel;
	}

	/**
	 * Helper for building the Graph Scale Options
	 * @return a <code>JPanel</code> containing the buttons and input boxes for changing the vertex sizes on the graph
	 */
	private JPanel getScalePanel() {
		JPanel scaleOptions = new JPanel(new GridLayout(4,1));
		scaleOptions.setBorder(BorderFactory.createTitledBorder("Scale Options"));
		scaleOptions.setToolTipText("Scale Vertices select parameters");

		JCheckBox scalePeers = new JCheckBox("Scale Peers");
		scalePeers.addActionListener(this);
		scalePeers.setToolTipText("Scale peer vertices on select parameters");
		scaleOptions.add(scalePeers);

		JCheckBox scaleDocuments = new JCheckBox("Scale Documents");
		scaleDocuments.addActionListener(this);
		scaleDocuments.setToolTipText("Scale document vertices on select parameters");
		scaleOptions.add(scaleDocuments);

		{ //Out edges scaling
			JPanel outEdgesPanel = new JPanel(new BorderLayout());
			scaleOptions.add(outEdgesPanel);

			JCheckBox scaleOutEdgeCount = new JCheckBox("Out Edge Count  X");
			scaleOutEdgeCount.addActionListener(this);
			scaleOutEdgeCount.setToolTipText("Scale Vertices on the edges leaving");
			outEdgesPanel.add(scaleOutEdgeCount, BorderLayout.WEST);

			JSpinner outEdgeMultiplier = new JSpinner();
			outEdgeMultiplier.addChangeListener(this);
			outEdgesPanel.add(outEdgeMultiplier, BorderLayout.CENTER);
			outEdgeMultiplier.setToolTipText("Multiplier for out edge scaling");

			outEdgeMultiplier.setModel(new SpinnerNumberModel(new Float(2), new Float(1), null, new Float(1)));
		}
		{ //In edges scaling
			JPanel inEdgesPanel = new JPanel();
			scaleOptions.add(inEdgesPanel);
			inEdgesPanel.setLayout(new BorderLayout());

			JCheckBox scaleInEdgeCount = new JCheckBox("In Edge Count     X");
			scaleInEdgeCount.addActionListener(this);
			scaleInEdgeCount.setToolTipText("Scale Vertices on the edges entering");
			inEdgesPanel.add(scaleInEdgeCount, BorderLayout.WEST);

			JSpinner inEdgeMultiplier = new JSpinner();
			inEdgeMultiplier.addChangeListener(this);
			inEdgeMultiplier.setModel(new SpinnerNumberModel(new Float(2), new Float(1), null, new Float(1)));
			inEdgeMultiplier.setToolTipText("Multiplier for in edge scaling");
			inEdgesPanel.add(inEdgeMultiplier, BorderLayout.CENTER);
		}
		return scaleOptions;
	}

	/**
	 * Helper for building the Graph View Panel
	 * @return a <code>JPanel</code> containing the buttons for changing the graph's <code>ViewState</code>
	 */
	private JPanel getViewPanel() {
		ButtonGroup buttonGroup = new ButtonGroup();
		JPanel viewPanel = new JPanel(new GridLayout(4,1));
		viewPanel.setBorder(BorderFactory.createTitledBorder("Graph View"));

		fullView = new JRadioButton("Full View");
		fullView.addActionListener(this);
		fullView.setToolTipText("Display all vertices which are not set to be hidden");
		viewPanel.add(fullView);
		buttonGroup.add(fullView);

		collapsedPeerView = new JRadioButton("Collapsed Peer View");
		collapsedPeerView.addActionListener(this);
		collapsedPeerView.setToolTipText("Display Peer and local document vertices which are not set to be hidden");
		viewPanel.add(collapsedPeerView);
		buttonGroup.add(collapsedPeerView);

		collapsedDocumentView = new JRadioButton("Collapsed Document View");
		collapsedDocumentView.addActionListener(this);
		collapsedDocumentView.setToolTipText("Display the root document and any local copies of that document which are not set to be hidden");
		viewPanel.add(collapsedDocumentView);
		buttonGroup.add(collapsedDocumentView);

		collapsedPeerAndDocumentView = new JRadioButton("Collapsed Peer and Document View");
		collapsedPeerAndDocumentView.setName("view");
		collapsedPeerAndDocumentView.addActionListener(this);
		collapsedPeerAndDocumentView.setToolTipText("Display Peers and root documents which are not set to be hidden");
		viewPanel.add(collapsedPeerAndDocumentView);
		buttonGroup.add(collapsedPeerAndDocumentView);

		return viewPanel;
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		JSpinner source = (JSpinner) ce.getSource(); //only one type of change event presently
		System.out.println("state change event");
		if(source == minimumInEdgePeerCutoff || source == minimumOutEdgePeerCutoff || 
				source == minimumInEdgeDocumentCutoff || source == minimumOutEdgeDocumentCutoff) {
			notifyPredicateListeners(generatePredicateChangeEvent());
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		AbstractButton source = (AbstractButton) ae.getSource();
		System.out.println("action event");
		if(source == showHiddenVertices || source == disregardTime || source == inEdgePeerCutoff || source == outEdgePeerCutoff || 
				source == inEdgeDocumentCutoff || source == outEdgeDocumentCutoff) {
			notifyPredicateListeners(generatePredicateChangeEvent());
		}

		if(source == collapsedPeerAndDocumentView || source == collapsedDocumentView || source == collapsedPeerView || source == fullView) {
			notifyViewListeners(ViewState.getFromString(source.getText()));
		}
	}

	/**
	 * Helper for making a <code>PredicateChangeEvent</code> from the currently selected options in the panel.
	 * @return a <code>PredicateChangeEvent</code> with all the currently selected options.
	 */
	protected PredicateChangeEvent generatePredicateChangeEvent() {
		System.out.println("generating predicate change event");
		boolean cutoffPeers = inEdgePeerCutoff.isSelected() || outEdgePeerCutoff.isSelected();
		boolean cutoffDocuments = inEdgeDocumentCutoff.isSelected() || outEdgeDocumentCutoff.isSelected();
		int peerInDegreeCutoff = (Integer) minimumInEdgePeerCutoff.getValue();
		int peerOutDegreeCutoff = (Integer) minimumOutEdgePeerCutoff.getValue();
		int documentInDegreeCutoff = (Integer) minimumInEdgeDocumentCutoff.getValue();
		int documentOutDegreeCutoff = (Integer) minimumOutEdgeDocumentCutoff.getValue();

		PredicateChangeEvent pce = new PredicateChangeEvent(showHiddenVertices.isSelected(), disregardTime.isSelected(), 
				cutoffPeers, peerInDegreeCutoff, peerOutDegreeCutoff, 
				cutoffDocuments, documentInDegreeCutoff, documentOutDegreeCutoff);

		return pce;
	}

	/**
	 * Helper for making a <code>TransformerChangeEvent</code> from the currently selected options in the panel.
	 * @return a <code>TransformerChangeEvent</code> with all the currently selected options.
	 */
	protected TransformerChangeEvent generateTransformerChangeEvent() {
		TransformerChangeEvent tce = new TransformerChangeEvent();
		return tce;
	}

	/**
	 * Helper for notifying all the view listeners of the view state changing
	 * 
	 * Uses swing's event dispatch thread to notify.
	 * @param view The new <code>ViewState</code> to notify listeners about.
	 */
	protected void notifyViewListeners(final ViewState view) {

		Runnable notify = new Runnable() {
			@Override
			public void run() {
				for(ViewChangeListener l : viewListeners) {
					l.viewChanged(view);
				}
			}
		};
		//Run on swing's event dispatch thread
		SwingUtilities.invokeLater(notify);
	}

	/**
	 * Helper for notifying all the Predicate listeners of the predicate's options changing
	 * 
	 * Uses swing's event dispatch thread to notify.
	 * @param options The <code>PredicateChangeEvent</code> to notify listeners to update to.
	 */
	protected void notifyPredicateListeners(final PredicateChangeEvent options) {

		Runnable notify = new Runnable() {
			@Override
			public void run() {
				System.out.println("notifying "+predicateListeners.size()+" listeners");
				for(PredicateChangeListener l : predicateListeners) {

					l.changeOptions(options);
				}
			}
		};
		//Run on swing's event dispatch thread
		SwingUtilities.invokeLater(notify);
	}

	/**
	 * Helper for notifying all the Transformer listeners of the transformer's options changing
	 * 
	 * Uses swing's event dispatch thread to notify.
	 * @param options The <code>TransformerChangeEvent</code> to notify listeners to update to.
	 */
	protected void notifyTransformerListeners(final TransformerChangeEvent options) {

		Runnable notify = new Runnable() {
			@Override
			public void run() {
				for(TransformerChangeListener l : transformerListeners) {
					l.changeOptions(options);
				}
			}
		};
		//Run on swing's event dispatch thread
		SwingUtilities.invokeLater(notify);
	}
}
