/*
 * File:         P2PNetEdgeLengthFunction.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer;

//import edu.uci.ics.jung.algorithms.layout.SpringLayout.LengthFunction;
import org.apache.commons.collections15.Transformer;

import spiderweb.graph.P2PConnection;

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
