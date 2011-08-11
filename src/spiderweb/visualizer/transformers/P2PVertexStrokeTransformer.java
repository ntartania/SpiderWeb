/*
 * File:         P2PVertexStrokeTransformer.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.transformers;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

import spiderweb.graph.P2PVertex;
import spiderweb.graph.PeerVertex;

public class P2PVertexStrokeTransformer implements
		Transformer<P2PVertex, Stroke> {

	@Override
	public Stroke transform(P2PVertex v) {
		if(v instanceof PeerVertex) { // if the vertex is a peer
			if( ((PeerVertex)v).hasIncomingQueries() ) {
				return new BasicStroke(2.5f); // make the stroke twice as wide if the vertex is getting a query
			}
		}
		return new BasicStroke(1.0f); // if it's to a document, make it narrow
	}

}
