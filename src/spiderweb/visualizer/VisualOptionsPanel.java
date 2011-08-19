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

import spiderweb.visualizer.predicates.PredicateChangeListener;
import spiderweb.visualizer.transformers.TransformerChangeListener;

public class VisualOptionsPanel extends JPanel implements ActionListener, ChangeListener {

	/**eclipse generated Serial UID*/
	private static final long serialVersionUID = 3026513624881148363L;
	
	private JRadioButton collapsedPeerAndDocumentView;
	private JRadioButton collapsedDocumentView;
	private JRadioButton collapsedPeerView;
	private JRadioButton fullView;
	
	/*List of Predicate Change Listeners for notifying*/
	private List<PredicateChangeListener> predicateListeners;
	
	public void addPredicateListener(PredicateChangeListener listener) {
		predicateListeners.add(listener);
	}
	public void removePredicateListener(PredicateChangeListener listener) {
		predicateListeners.remove(listener);
	}
	
	/*List of Transformer Change Listeners for notifying*/
	private List<TransformerChangeListener> transformerListeners;
	
	public void addTransformerListener(TransformerChangeListener listener) {
		transformerListeners.add(listener);
	}
	public void removeTransformerListener(TransformerChangeListener listener) {
		transformerListeners.remove(listener);
	}
	
	/*List of View Change Listeners for notifying*/
	private List<ViewChangeListener> viewListeners;
	
	public void addViewListener(ViewChangeListener listener) {
		viewListeners.add(listener);
	}
	/**
	 * 
	 * @param listener
	 */
	public void removeViewListener(ViewChangeListener listener) {
		viewListeners.remove(listener);
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

	public VisualOptionsPanel() {
		super(new BorderLayout());
		buildPanel();
		
		predicateListeners = new LinkedList<PredicateChangeListener>();
		transformerListeners = new LinkedList<TransformerChangeListener>();
		viewListeners = new LinkedList<ViewChangeListener>();
	}
	
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
	
	private JPanel getTransformerOptions() {
		JPanel transformerOptions = new JPanel(new BorderLayout()); 
		transformerOptions.setBorder(BorderFactory.createTitledBorder("Transformer Options"));
		
		JPanel panelForProperLayout = new JPanel(new BorderLayout());
		panelForProperLayout.add(getScalePanel(), BorderLayout.NORTH);
		
		transformerOptions.add(panelForProperLayout,BorderLayout.NORTH);
		
		return transformerOptions;
	}
	
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
	
	private JPanel getHiddenAndTimelessOptions() {
		JPanel p = new JPanel(new GridLayout(2,1));
		
		JCheckBox showHiddenVertices = new JCheckBox("Show Hidden Vertices");
		showHiddenVertices.setToolTipText("Show Vertices hidden from the right click menu.");
		p.add(showHiddenVertices);
		
		JCheckBox disregardTime = new JCheckBox("Show Vertices Regardless of Time");
		disregardTime.setToolTipText("Show all vertices regardless of the time through playback");
		p.add(disregardTime);
		
		return p;
	}
	
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
	
	private JPanel getVertexVisibilityPanel() {
		JPanel vertexVisiblityPanel = new JPanel(new GridLayout(4,1));
		vertexVisiblityPanel.setBorder(BorderFactory.createTitledBorder("Vertex Degree Cutoff"));
		vertexVisiblityPanel.setToolTipText("Hide vertices which do not meet specified parameters");
		
		JCheckBox hidePeers = new JCheckBox("Hide Peers");
		hidePeers.setToolTipText("Hide peer vertices on select parameters");
		vertexVisiblityPanel.add(hidePeers);

		JCheckBox hideDocuments = new JCheckBox("Hide Documents");
		hideDocuments.setToolTipText("Hide document vertices on select parameters");
		vertexVisiblityPanel.add(hideDocuments);
		
		{
			JPanel inDegreePanel = new JPanel(new BorderLayout());
			vertexVisiblityPanel.add(inDegreePanel);

			JCheckBox inEdgeCutoff = new JCheckBox("In Edge Count     X");
			inEdgeCutoff.setToolTipText("Hide Vertices on the number of edges entering");
			inDegreePanel.add(inEdgeCutoff, BorderLayout.WEST);

			JSpinner minimumInEdgeCutoff = new JSpinner();
			inDegreePanel.add(minimumInEdgeCutoff, BorderLayout.CENTER);
			minimumInEdgeCutoff.setToolTipText("minimum number of edges cutoff");
			minimumInEdgeCutoff.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		}
		
		{
			JPanel outDegreePanel = new JPanel(new BorderLayout());
			vertexVisiblityPanel.add(outDegreePanel);

			JCheckBox outEdgeCutoff = new JCheckBox("Out Edge Count  X");
			outEdgeCutoff.setToolTipText("Hide Vertices on the number of edges leaving");
			outDegreePanel.add(outEdgeCutoff, BorderLayout.WEST);

			JSpinner minimumOutEdgeCutoff = new JSpinner();
			outDegreePanel.add(minimumOutEdgeCutoff, BorderLayout.CENTER);
			minimumOutEdgeCutoff.setToolTipText("minimum number of edges cutoff");
			minimumOutEdgeCutoff.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		}
		
		return vertexVisiblityPanel;
	}

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
		collapsedPeerAndDocumentView.addActionListener(this);
		collapsedPeerAndDocumentView.setToolTipText("Display Peers and root documents which are not set to be hidden");
		viewPanel.add(collapsedPeerAndDocumentView);
		buttonGroup.add(collapsedPeerAndDocumentView);

		return viewPanel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		AbstractButton source = (AbstractButton) ae.getSource();
		String text = source.getText();
		
		if(text.contains("View")) {
			notifyViewListeners(ViewState.getFromString(text));
		}
	}
	
	private void notifyViewListeners(ViewState view) {
		for(ViewChangeListener l : viewListeners) {
			l.viewChanged(view);
		}
	}
}
