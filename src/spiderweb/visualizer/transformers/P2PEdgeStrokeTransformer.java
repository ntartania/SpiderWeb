/*
 * File:         P2PEdgeStrokeTransformer.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.transformers;

import spiderweb.graph.P2PConnection;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

public class P2PEdgeStrokeTransformer implements
		Transformer<P2PConnection, Stroke> {

	@Override
	public Stroke transform(P2PConnection edge) {
		if(edge.isP2P())
			if (edge.isQuerying())
				return new BasicStroke(2.5f); //make the stroke twice as wide if there is a query going through this edge
			else
				return new BasicStroke(1.5f); // if it's a regular connection between peers, make it wide
		else
			return new BasicStroke(0.5f); // if it's to a document, make it narrow
			
	}

}
