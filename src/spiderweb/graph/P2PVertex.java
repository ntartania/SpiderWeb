/*
 * File:         P2PVertex.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph;

/**
 * <p>A class for a vertex in the P2P network visualization.</p> <ul> <li>A vertex has a label which is drawn over the vertex on the graph.</li> <li>A vertex has a key which is its identifier, <code>equals(Object other)</code> compares the key value.</li> </ul>
 * @author  Matthew Smith
 * @author  Alan Davoust
 * @version  May 12, 2011
 */
public abstract class P2PVertex implements Comparable<P2PVertex> {
	
	//[start] Private Attributes
	protected boolean hide; //determines whether to show this vertex or not (used in the right click show/hide)
	protected Integer label; //the label will be drawn over the vertex on the graph
	protected Integer key; //the identifier which defines this vertex
	//[end] Private Attributes	
	
	//[start] Constructors
	/**
	 * Creates a vertex in the network Graph
	 * @param key The identifier which defines this vertex.
	 */
	public P2PVertex(Integer key){
		this.key = key;
		this.label = key;
	}
	
	/**
	 * Copy Constructor
	 * @param vertex the P2PVertex to copy
	 */
	protected P2PVertex(P2PVertex vertex) {
		this.key = vertex.key;
		this.label = vertex.label;
	}
	//[end] Constructors
	
	//[start] Getters and Setters
	/**
	 * Gets the label for the vertex.
	 * @return  Returns the label of this vertex.
	 */
	public Integer getLabel(){
		return label;
	}
	
	/**
	 * Gets the key for the vertex.
	 * @return  Returns the key(identifier) of this vertex.
	 */
	public Integer getKey(){
		return key;
	}
	
	/**
	 * Sets whether this vertex should be hidden or not.
	 * @param hidden <code>true</code> if the vertex should be hidden.
	 */
	public void setHidden(boolean hidden) {
		this.hide = hidden;
	}
	
	/**
	 * Returns whether or not this vertex should show in the visualizer.
	 * @return <code>true</code> if the vertex is hidden.
	 */
	public boolean isHidden() {
		return hide;
	}
	//[end] Getters and Setters
	
	//[start] Overridden Methods
	@Override
	public String toString() {
		return label.toString();
	}
	
	//Important : most Graph classes seem to rely on equals() to find vertices in their collection.
	@Override
	public boolean equals(Object other){
		if(other instanceof P2PVertex)
			return (key.equals(((P2PVertex)other).getKey()));
			else 
		return false; 
	}

	@Override
	public int hashCode(){
		return key.hashCode();	
	}
	
	@Override
	public int compareTo(P2PVertex other) {
		if(other instanceof P2PVertex)
			return (key.compareTo(((P2PVertex)other).getKey()));
		else 
		return 0; // there's a problem anyway : can only compare two P2PVertices
	}
	//[end] Overridden Methods
}