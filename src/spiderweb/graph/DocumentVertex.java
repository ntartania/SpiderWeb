/*
 * File:         DocumentVertex.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 20/07/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph;

/**
 * <p>A class which represents a Document as a vertex in the P2P network visualization.</p>
 * <ul>
 * <li>A document is drawn differently than a peer, as well, it has fewer states.</li>
 * <li>Has a state which describes if it was hit by a query or not.</li>
 * <li>Stores the key of it's publisher so it knows where it came from.</li>
 * </ul>
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 21/07/2011 
 */
public class DocumentVertex extends P2PVertex {
	
	/** 
	 * The name which represents this document 
	 * (so no concatination has to take place when calling <code>toString()</code>
	 */
	protected String name;
	
	/**
	 * Creates a vertex which represents a Document in the P2P network visualization.
	 * @param key	The key value of this document.
	 */
	public DocumentVertex(Integer key) {
		super(1000+key.intValue());
		//this.publisher = null; //remove this line
		this.label = new Integer(key.intValue() % 1000);
		//this.queryHit = false;
		name = "D"+super.toString();
	}
	
	/**
	 * Copy Constructor
	 * @param vertex The DocumentVertex to copy
	 */
	public DocumentVertex(P2PVertex vertex) {
		super((vertex.getClass().equals(DocumentVertex.class) ? vertex.getKey().intValue():1000+vertex.getKey().intValue()));
		this.label = new Integer(vertex.getKey()%1000);
	}
	
	/**
	 * Gets Document's number as an Integer.
	 * @return The Integer of the Document Number.
	 */
	public Integer getDocumentNumber() {
		return key % 1000;
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof DocumentVertex) {
			return ( super.equals(other) );
		}
		return false; 
	}
	
	@Override
	public int compareTo(P2PVertex other) {
		if(other instanceof DocumentVertex)
			return ( super.compareTo(other) );
		else 
		return 0;
	}
	
	@Override
	public String toString() {
		return name;
	}
}