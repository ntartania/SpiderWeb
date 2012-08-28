package spiderweb.visualizer;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import spiderweb.graph.P2PNetworkGraph;
import spiderweb.graph.P2PVertex;
import spiderweb.graph.PeerVertex;

public class GraphStatisticsPanel extends JPanel{

	/**eclipse generated Serial UID*/
	private static final long serialVersionUID = 8407256043023483988L;
	
	private int numberPeersOnline;
	private int numberPeersTotal;
	private JLabel numberPeersOnlineLabel;
	private JLabel numberPeersTotalLabel;
	
	public GraphStatisticsPanel(P2PNetworkGraph g) {
		
		numberPeersOnline = 0;
		numberPeersTotal = 0;
		
		for(P2PVertex vertex : g.getVertices()) {
			if(vertex.getClass() == PeerVertex.class) {
				numberPeersTotal++;
			}
		}
		
		this.setLayout(new BorderLayout());
		
		JPanel p = new JPanel(new GridLayout(2,1));
		
		numberPeersOnlineLabel = new JLabel("Number of Peers online: "+numberPeersOnline);
		numberPeersOnlineLabel.setToolTipText("Number of Peers online.");
		p.add(numberPeersOnlineLabel);

		numberPeersTotalLabel = new JLabel("Number of Peers online: "+numberPeersTotal);
		numberPeersTotalLabel.setToolTipText("Number of Peers Total.");
		p.add(numberPeersTotalLabel);

		this.add(p,BorderLayout.NORTH);
	}

}
