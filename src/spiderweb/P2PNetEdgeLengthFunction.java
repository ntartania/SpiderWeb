package spiderweb;

//import edu.uci.ics.jung.algorithms.layout.SpringLayout.LengthFunction;
import org.apache.commons.collections15.Transformer;

public class P2PNetEdgeLengthFunction implements Transformer<P2PConnection,Integer> {
	
	@Override
	public Integer transform(P2PConnection edge) {
		// TODO Auto-generated method stub
		
		if( edge.isP2P())
			return new Integer(75); //distance between two peers
		else
			return new Integer(20); //distance between a peer and a document
	}

}
